package org.example.objects;

import org.example.dto.RequestState;
import org.example.interfaces.ExecutionEngine;
import org.example.interfaces.LoadBalancer;
import org.example.interfaces.RequestQueue;
import org.example.interfaces.TimeoutManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class DefaultLoadBalancer implements LoadBalancer {

    private final RequestQueue queue;
    private final ExecutionEngine engine;
    private final TimeoutManager timeoutManager;
    private final ExecutorService dispatcher;

    public DefaultLoadBalancer(
            RequestQueue queue,
            ExecutionEngine engine,
            TimeoutManager timeoutManager
    ) {
        this.queue = queue;
        this.engine = engine;
        this.timeoutManager = timeoutManager;
        this.dispatcher = Executors.newSingleThreadExecutor();
    }

    @Override
    public void submit(DbRequest request) {
        if (!request.transition(RequestState.CREATED, RequestState.QUEUED)) {
            return;
        }

        timeoutManager.register(request);
        queue.offer(request);

        dispatch();
    }

    private void dispatch() {
        dispatcher.execute(this::drainQueue);
    }

    private void drainQueue() {
        DbRequest request;
        while ((request = queue.poll()) != null) {

            if (!request.transition(RequestState.QUEUED, RequestState.ASSIGNED)) {
                continue;
            }

            boolean accepted = engine.tryExecute(request);

            if (!accepted) {
                // нет ресурсов → вернуть в очередь
                request.transition(RequestState.ASSIGNED, RequestState.QUEUED);
                queue.offer(request);
                return;
            }
        }
    }

    @Override
    public void shutdown() {
        dispatcher.shutdownNow();
    }
}
