import java.io.OutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpTestClient {

    public static void main(String[] args) throws Exception {

        // Пример SQL-запросов
        String[] queries = new String[] {
                "SELECT 1",
                "SELECT 2",
                "SELECT 3",
                "SELECT 4",
                "SELECT 5"
        };

        for (String sql : queries) {
            sendRequest(sql);
        }
    }

    private static void sendRequest(String sql) {
        try {
            URL url = new URL("http://localhost:8080/query");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            byte[] payload = sql.getBytes(StandardCharsets.UTF_8);

            conn.setRequestProperty("Content-Type", "text/plain");
            conn.setRequestProperty("Content-Length", String.valueOf(payload.length));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload);
            }

            int code = conn.getResponseCode();

            InputStream is = (code >= 200 && code < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            byte[] responseBytes = is.readAllBytes();
            String response = new String(responseBytes, StandardCharsets.UTF_8);

            System.out.printf("SQL: %s → Response: %s%n", sql, response);

        } catch (Exception e) {
            System.err.printf("SQL: %s → Exception: %s%n", sql, e.getMessage());
        }
    }
}
