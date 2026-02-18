package org.example.objects;

import java.util.*;
import java.util.concurrent.*;

import org.example.interfaces.*;
import org.example.observer.LoadObserver;

import java.util.concurrent.ConcurrentHashMap;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public final class DefaultExecutionEngine implements ExecutionEngine {

    private final TimeoutManager timeoutManager;

    private final List<DbNode> nodes = new ArrayList<>();
    private final List<LoadObserver> observers = new CopyOnWriteArrayList<>();

    public DefaultExecutionEngine addNode(DbNode node) {
        this.nodes.add(node);
        return this;
    }

    public DefaultExecutionEngine build() {
        this.nodes.forEach(n -> ((SimpleDbNode) n).init());
        System.out.print("engine builded");
        return this;
    }

    Map<DbNode, AtomicInteger> loaded = new ConcurrentHashMap<>();
    private final Map<String, DbNode> sessionBindings = new ConcurrentHashMap<>();


    public DefaultExecutionEngine(TimeoutManager timeoutManager) {
        this.timeoutManager = timeoutManager;
    }

    public DbNode getMinLoadedNode() {
        if (nodes.isEmpty()) return null;

        // Выбираем ноду с минимальной нагрузкой
        return nodes.stream()
                .min(Comparator.comparingInt(
                        n -> loaded.getOrDefault(n, new AtomicInteger(0)).get()
                ))
                .orElse(null); // на случай пустого списка
    }

    // Метод уведомления
    private void notifyObservers(DbNode node) {
        int load = loaded.getOrDefault(node, new AtomicInteger(0)).get();
        for (LoadObserver observer : observers) {
            observer.onLoadChanged(node, load);
        }
    }

    public DefaultExecutionEngine withObserver(LoadObserver observer) {
        observers.add(observer);
        return this;
    }

    private DbNode getMaster() {
        return nodes.stream()
                .filter(n -> "nodeMaster".equals(n.id()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("Master node not found")
                );
    }

    private DbNode resolveTargetNode(DbRequest request) {

        String sessionId = request.getSessionId();
        String sql = request.getSql().trim().toLowerCase();

        // Если сессия уже закреплена — всегда используем её ноду
        if (sessionId != null && sessionBindings.containsKey(sessionId)) {
            return sessionBindings.get(sessionId);
        }

        // BEGIN → закрепляем master
        if (sql.startsWith("begin")) {
            DbNode master = getMaster();
            if (sessionId != null) {
                sessionBindings.put(sessionId, master);
            }
            return master;
        }

        // COMMIT / ROLLBACK → освобождаем биндинг
        if (sql.startsWith("commit") || sql.startsWith("rollback")) {
            DbNode node = sessionBindings.remove(sessionId);
            return node != null ? node : getMaster();
        }

        // Write → master
        if (isWriteQuery(sql)) {
            return getMaster();
        }

        // Read → replica
        return getMinLoadedNode();

    }

    private boolean isWriteQuery(String sql) {
        return sql.startsWith("insert")
                || sql.startsWith("update")
                || sql.startsWith("delete")
                || sql.startsWith("create")
                || sql.startsWith("alter")
                || sql.startsWith("drop")
                || sql.startsWith("truncate")
                || sql.startsWith("begin")
                || sql.startsWith("commit")
                || sql.startsWith("rollback")
                || sql.contains("for update");
    }

    @Override
    public boolean tryExecute(DbRequest request) {

        if (!request.markExecuting()) {
            return false;
        }

        //DbNode node = getMinLoadedNode();
        DbNode node = resolveTargetNode(request);
        assert node != null;
        DbConnection connection = node.acquire();

        if (connection == null) {
            request.requeue();
            return false;
        }

        loaded.compute(node, (n, counter) -> {
            if (counter == null) counter = new AtomicInteger();
            counter.incrementAndGet();
            return counter;
        });
        notifyObservers(node);

        CompletableFuture
                .runAsync(() -> execute(request, connection))
                .whenComplete((v, ex) -> {

                    loaded.compute(node, (n, counter) -> {
                        if (counter == null) return new AtomicInteger(0);
                        counter.decrementAndGet();
                        return counter;
                    });

                    node.release(connection);
                    notifyObservers(node);

                    if (ex != null || request.getState().isTimedOut()) {
                        // Если истёк таймаут или упало исключение — возвращаем в очередь
                        request.requeue();
                    }
                });

        return true;
    }

    private void execute(DbRequest request, DbConnection connection) {
        try (connection) {
            Object result = connection.execute(request.getSql());
            request.promise().completeSuccess(result);
            request.transition(
                    request.getState(),
                    request.getState().onSuccess()
            );
        } catch (Exception e) {
            request.promise().completeFailure(e);
            request.transition(
                    request.getState(),
                    request.getState().onFailure()
            );
        } finally {
            timeoutManager.unregister(request);
        }
    }


    // Добавляем подписчика
    @Override
    public void addObserver(LoadObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(LoadObserver observer) {
        observers.remove(observer);
    }

    @Override
    public int nodeCount() {
        return nodes.size();
    }


}

