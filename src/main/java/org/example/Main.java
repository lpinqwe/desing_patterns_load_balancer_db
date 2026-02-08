package org.example;

import org.example.facade.FacadeMetrics;
import org.example.utils.DatabaseServerBuilder;

public class Main {
    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
            FacadeMetrics facade = new FacadeMetrics();

            new DatabaseServerBuilder()
                    .withPort(8080)
                    .withTimeoutThreads(1)
                    .addNode("node1", 4, "jdbc:postgresql://localhost/postgres", "postgres", "postgres")
                    .addNode("node2", 4, "jdbc:postgresql://localhost/postgres", "postgres", "postgres")
                    .withObserver((n, load) -> System.out.println("Node " + n.id() + " load: " + load))
                    .withMetrics(facade)
                    .build()
                    .start();
            System.out.printf(String.valueOf(facade.getDBNodeCount()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to start server", e);
        }
    }
}

