package org.example.interfaces;

import org.example.interfaces.DbRequest;

public interface LoadBalancer {

    void submit(DbRequest request);

    void shutdown();
}

