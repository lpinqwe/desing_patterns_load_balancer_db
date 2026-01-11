package org.example.interfaces;

import org.example.interfaces.DbRequest;

public interface TimeoutManager {

    void register(DbRequest request);

    void unregister(DbRequest request);

    void shutdown();
}

