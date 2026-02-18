package org.example.objects;

import org.example.states.RequestState;
import org.example.interfaces.DbRequest;

import org.example.interfaces.DbResult;
import org.example.states.CreatedState;
import org.example.utils.RequestPromise;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

//todo интерфейс общий для разных видов запросо
//NOTE: implments DbRequest
public final class DbRequestImplementation implements DbRequest {

    private final String sql;
    RequestPromise promise;
    private final Instant deadline;
    private final AtomicReference<RequestState> state =
            new AtomicReference<>(CreatedState.INSTANCE);

    public DbRequestImplementation(String sql, RequestPromise promise, Instant deadline) {
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

    @Override
    public RequestState getState() {
        return state.get();
    }

    public boolean markExecuting() {
        RequestState current = state.get();
        return transition(current, current.onExecute());
    }

    public boolean requeue() {
        RequestState current = state.get();
        return transition(current, current.onQueueBack());
    }

    public void timeout() {
        RequestState current = state.get();
        transition(current, current.onTimeout());
        promise.completeTimeout();
    }

    /*
    public void timeout() {
RequestState current = state.get();
RequestState next = current.onTimeout();
if (transition(current, next)) {
    promise.completeTimeout();
}
}
    * */
    public boolean enqueue() {
        RequestState current = state.get();
        return transition(current, current.onAssigned());
    }


    public boolean assign() {
        RequestState current = state.get();
        return transition(current, current.onAssign());
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    private  String sessionId;


    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public boolean transition(RequestState expected, RequestState next) {
        if (!expected.canTransitionTo(next)) {
            return false;
        }
        return state.compareAndSet(expected, next);
    }


    public CompletableFuture<DbResult> future() {
        return promise.future();
    }

    public RequestPromise promise() {
        return promise;
    }
}
