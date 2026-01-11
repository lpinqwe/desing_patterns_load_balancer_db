import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PostgresConnectionTest {

    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost/postgres";
        String user = "postgres";
        String password = "postgres";

        try {
            // Явная загрузка драйвера (на всякий случай)
            Class.forName("org.postgresql.Driver");

            System.out.println("Trying to connect to PostgreSQL...");

            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                System.out.println("Connected successfully!");

                // Простейший тестовый запрос
                try (PreparedStatement stmt = conn.prepareStatement("SELECT 1");
                     ResultSet rs = stmt.executeQuery()) {

                    if (rs.next()) {
                        int result = rs.getInt(1);
                        System.out.println("Test query result: " + result);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Connection failed:");
            e.printStackTrace();
        }
    }
}
