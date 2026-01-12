import java.io.OutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class LoadTestClient {

    public static void main(String[] args) throws InterruptedException {
        //5 connections in db ==> Requests: 99980, Avg: 71,73 ms, Min: 13 ms, Max: 5017 ms
        //20 connections in db ==> Requests: 100000, Avg: 49,90 ms, Min: 3 ms, Max: 1140 ms
        //40 connections in db ==> Requests: 100000, Avg: 46,42 ms, Min: 3 ms, Max: 558 ms
        int totalRequests = 1000; // общее количество запросов
        int threads = 10;        // количество параллельных потоков
        String urlString = "http://localhost:8080/query";

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<Long>> futures = new ArrayList<>();

        for (int i = 0; i < totalRequests; i++) {
            final int idx = i + 1;
            futures.add(executor.submit(() -> sendRequest(urlString, "SELECT " + idx)));
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);

        // собираем результаты
        List<Long> times = new ArrayList<>();
        for (Future<Long> f : futures) {
            try {
                Long t = f.get();
                if (t != null) times.add(t);
            } catch (Exception e) {
                System.err.println("Request failed: " + e.getMessage());
            }
        }

        // статистика
        if (!times.isEmpty()) {
            long sum = times.stream().mapToLong(Long::longValue).sum();
            long min = times.stream().mapToLong(Long::longValue).min().orElse(0);
            long max = times.stream().mapToLong(Long::longValue).max().orElse(0);
            double avg = sum * 1.0 / times.size();

            System.out.printf("Requests: %d, Avg: %.2f ms, Min: %d ms, Max: %d ms%n",
                    times.size(), avg, min, max);
        } else {
            System.out.println("No successful requests.");
        }
    }

    private static Long sendRequest(String urlString, String sql) {
        long start = System.nanoTime();
        try {
            URL url = new URL(urlString);
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
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            is.readAllBytes(); // не обязательно парсить тело

            long durationMs = (System.nanoTime() - start) / 1_000_000;
            return durationMs;

        } catch (Exception e) {
            System.err.println("Request failed for SQL: " + sql + " -> " + e.getMessage());
            return null;
        }
    }
}
