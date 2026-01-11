package org.example.interfaces;

import org.example.objects.DbRequest;

public interface TimeoutManager {

    void register(DbRequest request);

    void unregister(DbRequest request);

    void shutdown();
}

