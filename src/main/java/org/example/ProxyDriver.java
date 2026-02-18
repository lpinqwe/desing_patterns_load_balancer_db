package org.example;


import org.example.factory.DbRequestFactory;
import org.example.interfaces.*;
import org.postgresql.Driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Properties;

public class ProxyDriver extends Driver {

    private final QueryGateway gateway;

    public ProxyDriver(QueryGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public Connection connect(String url, Properties info)
            throws SQLException {

        if (!acceptsURL(url)) return null;
        Connection real = null;


        return new ProxyConnection( gateway);
    }

    @Override
    public boolean acceptsURL(String url) {
        return url.startsWith("jdbc:proxy:");
    }
}
