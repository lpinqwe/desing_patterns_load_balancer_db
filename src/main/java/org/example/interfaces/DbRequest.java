package org.example.interfaces;
import org.example.ProxyConnection;
import org.example.ProxyStatement;
import org.example.states.RequestState;
import org.example.utils.RequestPromise;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public interface DbRequest {
    RequestPromise promise = new RequestPromise();
    void setProxySS(ProxyStatement.ProxyStatementSettings pss);
    void setProxyCS(ProxyConnection.PCS pcs);
    ProxyStatement.ProxyStatementSettings getPSS();
    ProxyConnection.PCS getPCS();
    String getSql();
    boolean markExecuting();
    boolean requeue();
    void timeout();
    boolean enqueue();
    boolean assign();

    String getSessionId();

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


    void setSessionId(String sessionId);
}
