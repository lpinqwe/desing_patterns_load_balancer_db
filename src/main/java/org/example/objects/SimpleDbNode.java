package org.example.objects;

import org.example.dto.NodeRole;
import org.example.interfaces.ConnectionPool;
import org.example.interfaces.DbConnection;
import org.example.interfaces.DbNode;

public final class SimpleDbNode implements DbNode {

    private final String id;
    private final ConnectionPool pool;

    public SimpleDbNode(String id, ConnectionPool pool) {
        this.id = id;
        this.pool = pool;
    }

    @Override
    public boolean isAvailable() {
        return pool.available() > 0;
    }

    @Override
    public DbConnection acquire() {
        return pool.tryAcquire();
    }

    @Override
    public void release(DbConnection connection) {
        pool.release(connection);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public NodeRole role() {
        return null;
    }
}
