package org.example.interfaces;

import org.example.interfaces.DbRequest;

public interface LoadBalancer {

    int getQueueLen();
    void submit(DbRequest request);

    void shutdown();
}

