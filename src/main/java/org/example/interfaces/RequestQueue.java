package org.example.interfaces;

import org.example.objects.DbRequest;

public interface RequestQueue {

    boolean offer(DbRequest request);

    DbRequest poll();

    int size();
}
