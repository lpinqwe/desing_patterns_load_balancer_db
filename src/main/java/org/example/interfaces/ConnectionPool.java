package org.example.interfaces;

public interface ConnectionPool {

    DbConnection tryAcquire();

    void release(DbConnection connection);

    int available();
}
