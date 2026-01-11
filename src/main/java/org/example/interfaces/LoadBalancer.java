package org.example.interfaces;

import org.example.objects.DbRequest;

public interface LoadBalancer {

    void submit(DbRequest request);

    void shutdown();
}

