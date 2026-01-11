package org.example.objects;

import org.example.interfaces.ConnectionPool;
import org.example.interfaces.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class PooledJdbcConnection implements DbConnection {

    private final Connection physicalConnection;
    private final ConnectionPool pool;
    private boolean inUse = true; // флаг текущего использования

    public PooledJdbcConnection(Connection physicalConnection, ConnectionPool pool) {
        this.physicalConnection = physicalConnection;
        this.pool = pool;
    }

    @Override
    public Object execute(String sql) throws Exception {
        if (!inUse) throw new IllegalStateException("Connection returned to pool");

        try (PreparedStatement stmt = physicalConnection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getObject(1); // простой пример: первый столбец первой строки
            }
            return null;
        }
    }

    @Override
    public void close() {
        if (inUse) {
            inUse = false;
            pool.release(this); // возвращаем соединение в пул
        }
    }

    // Метод для ExecutionEngine, чтобы пометить соединение как занятое
    public void markInUse() {
        inUse = true;
    }

    // Если нужно физически закрыть соединение при shutdown пула
    public void destroy() throws Exception {
        physicalConnection.close();
    }
}
