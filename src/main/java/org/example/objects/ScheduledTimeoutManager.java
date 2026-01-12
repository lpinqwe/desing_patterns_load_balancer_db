package org.example.objects;

import org.example.dto.RequestState;
import org.example.interfaces.DbRequest;
import org.example.interfaces.TimeoutManager;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;

public final class ScheduledTimeoutManager implements TimeoutManager {

    private final ScheduledExecutorService scheduler;
    private final Map<DbRequest, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();

    public ScheduledTimeoutManager(int threads) {
        this.scheduler = Executors.newScheduledThreadPool(threads);
    }

    @Override
    public void register(DbRequest request) {
        long delayMillis = millisUntilDeadline(request);
        if (delayMillis <= 0) {
            request.timeout();
            return;
        }

        ScheduledFuture<?> task = scheduler.schedule(
                () -> {
                    tasks.remove(request);
                    request.timeout();
                },
                delayMillis,
                TimeUnit.MILLISECONDS
        );

        tasks.put(request, task);
    }


    @Override
    public void unregister(DbRequest request) {
        ScheduledFuture<?> task = tasks.remove(request);
        if (task != null) {
            task.cancel(false);
        }
    }

    @Override
    public void shutdown() {
        scheduler.shutdownNow();
    }

    private void onTimeout(DbRequest request) {
        tasks.remove(request);
        timeoutNow(request);
    }

    private void timeoutNow(DbRequest request) {
        request.timeout();
    }


    private long millisUntilDeadline(DbRequest request) {
        return request.getDeadline().toEpochMilli() - Instant.now().toEpochMilli();
    }
}
