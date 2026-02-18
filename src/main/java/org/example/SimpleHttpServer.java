package org.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import org.example.interfaces.DbResult;
import org.example.utils.DbSuccess;
import org.example.utils.DbFailure;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
@Deprecated
public class SimpleHttpServer {

    private final QueryGateway gateway;
    private final int port;

    public SimpleHttpServer(QueryGateway gateway, int port) {
        this.gateway = gateway;
        this.port = port;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/query", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }

                // читаем SQL из тела запроса
                String sql = new String(exchange.getRequestBody().readAllBytes());

                // sessionId можно передавать из заголовка, например "X-Session-Id"
                String sessionId = exchange.getRequestHeaders().getFirst("X-Session-Id");

                CompletableFuture<DbResult> future =null;
                       // gateway.execute(sql, sessionId,Duration.ofSeconds(5));

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
        System.out.println("HTTP Gateway Server started on port " + port);
    }
}
