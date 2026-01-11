package org.example.interfaces;

public interface DbConnection extends AutoCloseable {

    Object execute(String sql) throws Exception;

    @Override
    void close();
}
