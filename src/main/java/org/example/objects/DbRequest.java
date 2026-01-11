package org.example.objects;

import org.example.dto.RequestState;
import org.example.interfaces.DbResult;
import org.example.utils.RequestPromise;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public final class DbRequest {

    private final String sql;
    private final RequestPromise promise;
    private final Instant deadline;
    private final AtomicReference<RequestState> state =
            new AtomicReference<>(RequestState.CREATED);

    public DbRequest(String sql, RequestPromise promise, Instant deadline) {
        this.sql = sql;
        this.promise = promise;
        this.deadline = deadline;
    }

    public String getSql() {
        return sql;
    }

    public Instant getDeadline() {
        return deadline;
    }

    public RequestState getState() {
        return state.get();
    }

    public boolean transition(RequestState expected, RequestState next) {
        return state.compareAndSet(expected, next);
    }

    public CompletableFuture<DbResult> future() {
        return promise.future();
    }

    RequestPromise promise() {
        return promise;
    }
}
