package org.example.interfaces;

import org.example.interfaces.DbRequest;

public interface ExecutionEngine {

    boolean tryExecute(DbRequest request);
}
