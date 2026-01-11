package org.example.interfaces;

import org.example.dto.NodeRole;

import java.util.concurrent.atomic.AtomicInteger;

public interface DbNode {

    boolean isAvailable();

    DbConnection acquire();

    void release(DbConnection connection);

    String id();
    NodeRole role(); // PRIMARY / REPLICA

}
