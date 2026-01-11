package org.example.objects;

import org.example.interfaces.RequestQueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class FifoRequestQueue implements RequestQueue {

    private final BlockingQueue<DbRequest> queue = new LinkedBlockingQueue<>();

    @Override
    public boolean offer(DbRequest request) {
        return queue.offer(request);
    }

    @Override
    public DbRequest poll() {
        return queue.poll();
    }

    @Override
    public int size() {
        return queue.size();
    }
}
