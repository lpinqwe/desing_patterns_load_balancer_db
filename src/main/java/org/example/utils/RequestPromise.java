package org.example.utils;

import org.example.interfaces.DbResult;

import java.util.concurrent.CompletableFuture;

public final class RequestPromise {

    private final CompletableFuture<DbResult> future = new CompletableFuture<>();

    public CompletableFuture<DbResult> future() {
        return future;
    }

    public boolean completeSuccess(Object data) {
        return future.complete(new DbSuccess(data));
    }

    public boolean completeFailure(Throwable error) {
        return future.complete(new DbFailure(error));
    }

    public boolean completeTimeout() {
        return future.complete(
                new DbFailure(new RuntimeException("Request timeout"))
        );
    }

    boolean cancel() {
        return future.cancel(true);
    }
}
