


import org.example.ProxyDriver;
import org.example.QueryGateway;
import org.example.facade.FacadeMetrics;
import org.example.utils.DatabaseServerBuilder;

import java.sql.*;

public class JDCBtest {

    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");

            FacadeMetrics facade = new FacadeMetrics();

            QueryGateway gateway = new DatabaseServerBuilder()
                    .withTimeoutThreads(1)
                    .addNode("nodeMaster", 4,
                            "jdbc:postgresql://127.0.0.1/postgres",
                            "postgres",
                            "postgres")
                    .addNode("nodeReplica", 4,
                            "jdbc:postgresql://127.0.0.1/postgres",
                            "postgres",
                            "postgres")
                    .withObserver((n, load) ->
                            System.out.println("Node " + n.id() + " load: " + load))
                    .withMetrics(facade)
                    .build();

            DriverManager.registerDriver(new ProxyDriver(gateway));

            Connection conn = DriverManager.getConnection("jdbc:proxy://localhost");
            Statement stmt = conn.createStatement();

            System.out.println("=== TEST 1: SELECT 1 ===");
            ResultSet rs1 = stmt.executeQuery("SELECT 1");
            while (rs1.next()) {
                System.out.println("Result: " + rs1.getInt(1));
            }
            rs1.close();

            System.out.println("\n=== TEST 2: CREATE TABLE ===");
            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS test_lb (
                        id SERIAL PRIMARY KEY,
                        name TEXT
                    )
                    """);
            System.out.println("Table created");

            System.out.println("\n=== TEST 3: INSERT ===");
            int rows1 = stmt.executeUpdate(
                    "INSERT INTO test_lb (name) VALUES ('Alice')");
            int rows2 = stmt.executeUpdate(
                    "INSERT INTO test_lb (name) VALUES ('Bob')");
            System.out.println("Inserted rows: " + (rows1 + rows2));

            System.out.println("\n=== TEST 4: SELECT inserted data ===");
            ResultSet rs2 = stmt.executeQuery(
                    "SELECT id, name FROM test_lb ORDER BY id");
            while (rs2.next()) {
                System.out.println(
                        "Row -> id=" + rs2.getInt("id") +
                                ", name=" + rs2.getString("name"));
            }
            rs2.close();

            System.out.println("\n=== TEST 5: TRANSACTION ===");
            conn.setAutoCommit(false);

            stmt.executeUpdate(
                    "INSERT INTO test_lb (name) VALUES ('TransactionUser')");
            stmt.execute("COMMIT");

            conn.setAutoCommit(true);

            ResultSet rs3 = stmt.executeQuery(
                    "SELECT name FROM test_lb WHERE name='TransactionUser'");
            while (rs3.next()) {
                System.out.println("Transaction row: " + rs3.getString(1));
            }
            rs3.close();

            stmt.close();
            conn.close();

            System.out.println("\n=== DONE ===");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
