package org.example;

import org.example.facade.FacadeMetrics;
import org.example.utils.DatabaseServerBuilder;
//
//public class Main {
//    public static void main(String[] args) {
//        try {
//            Class.forName("org.postgresql.Driver");
//            FacadeMetrics facade = new FacadeMetrics();
//
//            new DatabaseServerBuilder()
//                    .withPort(8080)
//                    .withTimeoutThreads(1)
//                    .addNode("node1", 4, "jdbc:postgresql://localhost/postgres", "postgres", "postgres")
//                    .addNode("node2", 4, "jdbc:postgresql://localhost/postgres", "postgres", "postgres")
//                    .withObserver((n, load) -> System.out.println("Node " + n.id() + " load: " + load))
//                    .withMetrics(facade)
//                    .build()
//                    .start();
//            System.out.printf(String.valueOf(facade.getDBNodeCount()));
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException("Failed to start server", e);
//        }
//    }
//}
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main {

    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");

            FacadeMetrics facade = new FacadeMetrics();
            System.out.println("facade ok");
            QueryGateway gateway = new DatabaseServerBuilder()
                    .withTimeoutThreads(1)
                    .addNode("nodeMaster", 4, "jdbc:postgresql://127.0.0.1:5432/postgres", "postgres", "postgres")
                    .addNode("node2", 4, "jdbc:postgresql://127.0.0.1:5433/postgres", "postgres", "postgres")
                    .addNode("node3", 4, "jdbc:postgresql://127.0.0.1:5434/postgres", "postgres", "postgres")
                    .withMetrics(facade)
                    .withObserver((n, load) ->
                            System.out.println("Node " + n.id() + " load: " + load)
                            //System.out.println("observer detect")
                    )

                    .build();
            System.out.println("gateway ok");

            DriverManager.registerDriver(new ProxyDriver(gateway));
            System.out.println("proxy ok");
            // теперь используем как обычный JDBC
            Connection conn =
                    DriverManager.getConnection("jdbc:proxy://localhost");
            System.out.println("proxy JDCB");
            Statement stmt = conn.createStatement();


            System.out.println(stmt.execute("SELECT 1"));
            boolean hasResultSet = stmt.execute("SELECT 1");

            if (hasResultSet) {
                ResultSet rs = stmt.getResultSet();
                while (rs.next()) {
                    int value = rs.getInt(1); // первый столбец
                    System.out.println("Result: " + value);
                }
                rs.close();
            } else {
                int updateCount = stmt.getUpdateCount();
                System.out.println("Update count: " + updateCount);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


