package org.example.interfaces;

import org.example.interfaces.DbRequest;

public interface RequestQueue {

    boolean offer(DbRequest request);

    DbRequest poll();

    int size();
}
