package org.example.interfaces;

import org.example.objects.DbRequest;

public interface ExecutionEngine {

    boolean tryExecute(DbRequest request);
}
