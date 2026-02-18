package org.example.utils;

import org.example.ProxyConnection;
import org.example.ProxyStatement;
import org.example.interfaces.ConnectionPool;
import org.example.interfaces.DbConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
@Deprecated
public final class JdbcDbConnection implements DbConnection {

    private final Connection conn;
    private final ConnectionPool pool;
    private boolean closed = false;

    public JdbcDbConnection(String jdbcUrl, String user, String password, ConnectionPool pool) throws Exception {
        this.conn = DriverManager.getConnection(jdbcUrl, user, password);
        this.pool = pool;
    }

    @Override
    public Object execute(String sql) throws Exception {
        if (closed) throw new IllegalStateException("Connection closed");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            boolean hasResultSet = stmt.execute();
            if (hasResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    if (rs.next()) {
                        return rs.getObject(1); // простой пример, берем первый столбец первой строки
                    }
                }
            }
            return null;
        }
    }

    @Override
    public void setPCS_PSS(ProxyConnection.PCS pcs, ProxyStatement.ProxyStatementSettings pss) {

    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            pool.release(this);
        }
    }
}
