package org.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.example.factory.DbRequestFactory;
import org.example.interfaces.DbResult;
import org.example.interfaces.LoadBalancer;
import org.example.interfaces.DbRequest;
import org.example.utils.DbFailure;
import org.example.utils.DbSuccess;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class SimpleHttpServer {

    private final DbRequestFactory factory;
    private final LoadBalancer loadBalancer;

    public SimpleHttpServer(DbRequestFactory factory, LoadBalancer loadBalancer) {
        this.factory = factory;
        this.loadBalancer = loadBalancer;
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/query", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if (!"POST".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }

                // читаем тело запроса как SQL строку
                String sql = new String(exchange.getRequestBody().readAllBytes());

                DbRequest request = factory.create(sql, Duration.ofSeconds(5));
                loadBalancer.submit(request);

                CompletableFuture<DbResult> future = request.future();

                // async response
                future.whenComplete((result, ex) -> {
                    try {
                        String response;
                        if (ex != null) {
                            response = "ERROR: " + ex.getMessage();
                        } else if (result instanceof DbSuccess success) {
                            response = "OK: " + success.getData();
                        } else if (result instanceof DbFailure failure) {
                            response = "FAIL: " + failure.getError().getMessage();
                        } else {
                            response = "UNKNOWN RESULT";
                        }

                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        });

        server.start();
        System.out.println("HTTP server started at port " + port);
    }
}
