package org.example.objects;

import org.example.interfaces.ConnectionPool;
import org.example.interfaces.DbConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class JdbcConnectionPool implements ConnectionPool {

    private final BlockingQueue<PooledJdbcConnection> pool;

    public JdbcConnectionPool(int size, String url, String user, String password) throws Exception {
        this.pool = new LinkedBlockingQueue<>(size);

        for (int i = 0; i < size; i++) {
            Connection conn = DriverManager.getConnection(url, user, password);
            pool.add(new PooledJdbcConnection(conn, this));
        }
    }

    @Override
    public DbConnection tryAcquire() {
        PooledJdbcConnection conn = pool.poll();
        if (conn != null) conn.markInUse();
        return conn;
    }

    @Override
    public void release(DbConnection connection) {
        if (connection instanceof PooledJdbcConnection conn) {
            pool.offer(conn);
        }
    }

    @Override
    public int available() {
        return pool.size();
    }
}
