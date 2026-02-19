package org.example.interfaces;

import org.example.ProxyConnection;
import org.example.ProxyStatement;

public interface DbConnection extends AutoCloseable {


    Object execute(String sql) throws Exception;
    void setPCS_PSS(ProxyConnection.PCS pcs, ProxyStatement.ProxyStatementSettings pss);
    @Override
    void close();
}
