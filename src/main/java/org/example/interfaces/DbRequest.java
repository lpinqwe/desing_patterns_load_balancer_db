package org.example.interfaces;
import org.example.dto.RequestState;
import org.example.utils.RequestPromise;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public interface DbRequest {
    RequestPromise promise = new RequestPromise();

    String getSql();

    Instant getDeadline();

    RequestState getState();

    /**
     * Atomically tries to move request state from expected to next.
     *
     * @return true if transition succeeded
     */
    boolean transition(RequestState expected, RequestState next);

    CompletableFuture<DbResult> future();
    public RequestPromise promise() ;


}
