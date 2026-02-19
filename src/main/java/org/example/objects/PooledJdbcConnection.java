package org.example.objects;

import org.example.ProxyConnection;
import org.example.ProxyStatement;
import org.example.interfaces.ConnectionPool;
import org.example.interfaces.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class PooledJdbcConnection implements DbConnection {

    private final Connection physicalConnection;
    private final ConnectionPool pool;
    private boolean inUse = true; // флаг текущего использования
    private ProxyStatement.ProxyStatementSettings pss;
    private ProxyConnection.PCS pcs;

    public PooledJdbcConnection(Connection physicalConnection, ConnectionPool pool) {
        this.physicalConnection = physicalConnection;
        this.pool = pool;
    }

    @Override
    public ResultSet execute(String sql) throws Exception {
        if (!inUse) {
            throw new IllegalStateException("Connection returned to pool");
        }
        physicalConnection.setAutoCommit(pcs.autoCommit);
        PreparedStatement stmt = physicalConnection.prepareStatement(sql);
        stmt.setCursorName(pss.cursorName);
        stmt.execute(); // универсальный вызов

        return stmt.getResultSet(); // просто возвращаем ResultSet
    }

    @Override
    public void setPCS_PSS(ProxyConnection.PCS pcs, ProxyStatement.ProxyStatementSettings pss) {
        this.pss=pss;
        this.pcs=pcs;
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
