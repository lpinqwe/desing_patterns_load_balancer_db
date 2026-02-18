package org.example;

import org.example.interfaces.DbResult;
import org.example.utils.DbFailure;
import org.example.utils.DbSuccess;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ProxyStatement implements Statement {
    private final Statement delegate; // реальный Statement

    private final QueryGateway gateway;
    private final String sessionId;
    private CachedRowSet cachedResultSet;

    public ProxyStatement(Statement delegate, QueryGateway gateway, String sessionId) {
        this.delegate = delegate;
        this.gateway = gateway;
        this.sessionId = sessionId;
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        // 1. Перехватываем SQL и отправляем через LoadBalancer/QueryGateway

        DbResult result = gateway.execute(sql, sessionId, Duration.ofSeconds(5)).join(); // блокируем для примера

        // 2. Если запрос успешный
        if (result instanceof DbSuccess success) {
            Object rawData = success.getData();

            // 2a. Если это ResultSet, просто используем
            if (rawData instanceof ResultSet rs) {
                CachedRowSet rowSet = RowSetProvider.newFactory().createCachedRowSet();
                rowSet.populate(rs);
                this.cachedResultSet = rowSet;
                return true;
            }

            // 2b. Если это List<Map<String,Object>>, строим CachedRowSet вручную
            if (rawData instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> data = (List<Map<String, Object>>) rawData;

                CachedRowSet rowSet = RowSetProvider.newFactory().createCachedRowSet();

                // создаём метаданные: берем ключи первой строки
                Map<String,Object> first = data.get(0);
                RowSetMetaDataImpl meta = new RowSetMetaDataImpl();
                meta.setColumnCount(first.size());

                int idx = 1;
                for (String colName : first.keySet()) {
                    meta.setColumnName(idx, colName);
                    meta.setColumnType(idx, Types.VARCHAR); // можно улучшить по типу значения
                    idx++;
                }
                rowSet.setMetaData(meta);

                // добавляем строки
                for (Map<String,Object> row : data) {
                    rowSet.moveToInsertRow();
                    idx = 1;
                    for (String colName : first.keySet()) {
                        rowSet.updateObject(idx, row.get(colName));
                        idx++;
                    }
                    rowSet.insertRow();
                }
                rowSet.moveToCurrentRow();

                this.cachedResultSet = rowSet;
                return true;
            }

            // иначе — пустой результат
            this.cachedResultSet = null;
            return false;

        } else if (result instanceof DbFailure failure) {
            throw new SQLException("Failed to execute: " + failure.getError().getMessage());
        } else {
            throw new SQLException("Unknown DbResult type: " + result);
        }
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        execute(sql);
        return getResultSet();
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        DbResult result = gateway.execute(sql, sessionId, Duration.ofSeconds(5)).join();
        if (result instanceof DbSuccess success) {
            Object data = success.getData();
            if (data instanceof Integer count) {
                return count;
            } else {
                // если LB возвращает ResultSet или null для write-запроса
                return 1; // просто возвращаем 1 как заглушку
            }
        } else if (result instanceof DbFailure failure) {
            throw new SQLException("Failed to boolean execute: " + failure.getError().getMessage());
        } else {
            throw new SQLException("Unknown DbResult type: " + result);
        }
    }


    @Override
    public ResultSet getResultSet() throws SQLException {
        return cachedResultSet;
    }
//    @Override
//    public ResultSet executeQuery(String sql) throws SQLException {
//        return delegate.executeQuery(sql);
//    }
//
//    @Override
//    public int executeUpdate(String sql) throws SQLException {
//        return delegate.executeUpdate(sql);
//    }

    @Override
    public void close() throws SQLException {
        delegate.close();
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return delegate.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        delegate.setMaxFieldSize(max);
    }

    @Override
    public int getMaxRows() throws SQLException {
        return delegate.getMaxRows();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        delegate.setMaxRows(max);
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        delegate.setEscapeProcessing(enable);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return delegate.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        delegate.setQueryTimeout(seconds);
    }

    @Override
    public void cancel() throws SQLException {
        delegate.cancel();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        delegate.setCursorName(name);
    }

//    @Override
//    public ResultSet getResultSet() throws SQLException {
//        return delegate.getResultSet();
//    }

    @Override
    public int getUpdateCount() throws SQLException {
        return delegate.getUpdateCount();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return delegate.getMoreResults();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        delegate.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return delegate.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        delegate.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return delegate.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return delegate.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return delegate.getResultSetType();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        delegate.addBatch(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        delegate.clearBatch();
    }

    @Override
    public int[] executeBatch() throws SQLException {
        return delegate.executeBatch();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return delegate.getConnection();
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return delegate.getMoreResults(current);
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return delegate.getGeneratedKeys();
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return delegate.executeUpdate(sql, autoGeneratedKeys);
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return delegate.executeUpdate(sql, columnIndexes);
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return delegate.executeUpdate(sql, columnNames);
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return delegate.execute(sql, autoGeneratedKeys);
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return delegate.execute(sql, columnIndexes);
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return delegate.execute(sql, columnNames);
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return delegate.getResultSetHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        delegate.setPoolable(poolable);
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return delegate.isPoolable();
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        delegate.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return delegate.isCloseOnCompletion();
    }

    @Override
    public long getLargeUpdateCount() throws SQLException {
        return delegate.getLargeUpdateCount();
    }

    @Override
    public void setLargeMaxRows(long max) throws SQLException {
        delegate.setLargeMaxRows(max);
    }

    @Override
    public long getLargeMaxRows() throws SQLException {
        return delegate.getLargeMaxRows();
    }

    @Override
    public long[] executeLargeBatch() throws SQLException {
        return delegate.executeLargeBatch();
    }

    @Override
    public long executeLargeUpdate(String sql) throws SQLException {
        return delegate.executeLargeUpdate(sql);
    }

    @Override
    public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return delegate.executeLargeUpdate(sql, autoGeneratedKeys);
    }

    @Override
    public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return delegate.executeLargeUpdate(sql, columnIndexes);
    }

    @Override
    public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
        return delegate.executeLargeUpdate(sql, columnNames);
    }

    @Override
    public String enquoteLiteral(String val) throws SQLException {
        return delegate.enquoteLiteral(val);
    }

    @Override
    public String enquoteIdentifier(String identifier, boolean alwaysQuote) throws SQLException {
        return delegate.enquoteIdentifier(identifier, alwaysQuote);
    }

    @Override
    public boolean isSimpleIdentifier(String identifier) throws SQLException {
        return delegate.isSimpleIdentifier(identifier);
    }

    @Override
    public String enquoteNCharLiteral(String val) throws SQLException {
        return delegate.enquoteNCharLiteral(val);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }
}

