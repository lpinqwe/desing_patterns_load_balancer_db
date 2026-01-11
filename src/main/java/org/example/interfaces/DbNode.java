package org.example.interfaces;

import org.example.dto.NodeRole;

public interface DbNode {

    boolean isAvailable();

    DbConnection acquire();

    void release(DbConnection connection);

    String id();
    NodeRole role(); // PRIMARY / REPLICA

}
