package org.example.objects;

public final class MetricsContext {

    private final long createdAt = System.nanoTime();
    private volatile long queuedAt;
    private volatile long executingAt;
    private volatile long completedAt;

    public void markQueued() {
        queuedAt = System.nanoTime();
    }

    public void markExecuting() {
        executingAt = System.nanoTime();
    }

    public void markCompleted() {
        completedAt = System.nanoTime();
    }

    public long queueTimeNanos() {
        return executingAt - queuedAt;
    }

    public long executionTimeNanos() {
        return completedAt - executingAt;
    }

    public long totalTimeNanos() {
        return completedAt - createdAt;
    }
}
