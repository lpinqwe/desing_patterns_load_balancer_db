package org.example;

import org.example.factory.DbRequestFactory;
import org.example.interfaces.*;
import org.example.objects.*;
import org.example.utils.JdbcDbConnection;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        try {
            // 1. Загружаем драйвер PostgreSQL
            Class.forName("org.postgresql.Driver");

            // 2. Создаём пул JDBC соединений
            JdbcConnectionPool pool = new JdbcConnectionPool(
                    40,                              // размер пула
                    "jdbc:postgresql://localhost/postgres",
                    "postgres",
                    "postgres"
            );

            // 3. Создаём DbNode на этом пуле
            DbNode node = new SimpleDbNode("node1", pool);

            // 4. TimeoutManager
            TimeoutManager tm = new ScheduledTimeoutManager(1);

            // 5. ExecutionEngine
            ExecutionEngine engine = new DefaultExecutionEngine(node, tm);

            // 6. Очередь и LoadBalancer
            RequestQueue queue = new FifoRequestQueue();
            LoadBalancer lb = new DefaultLoadBalancer(queue, engine, tm);

            // 7. Factory для запросов
            DbRequestFactory factory = new DbRequestFactory();

            // 8. HTTP сервер
            SimpleHttpServer server = new SimpleHttpServer(factory, lb);
            server.start(8080);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to start server", e);
        }
    }
}
