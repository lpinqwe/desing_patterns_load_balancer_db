package org.example.factory;

import org.example.interfaces.DbRequest;

import java.time.Duration;

public interface FactoryPattern {
    //public DbRequest create();

    DbRequest create(String sql, Duration timeout);
}
