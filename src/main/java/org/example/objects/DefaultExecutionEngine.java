package org.example.objects;
import java.util.*;
import java.util.concurrent.*;

import org.example.dto.RequestState;
import org.example.interfaces.*;

import java.util.concurrent.ConcurrentHashMap;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class DefaultExecutionEngine implements ExecutionEngine {

    private final TimeoutManager timeoutManager;

    private final List<DbNode> nodes = new ArrayList<>();
    public DefaultExecutionEngine addNode(DbNode node){
        this.nodes.add(node);
        return this;
    }
//todo доделать до билдера
    //NOTE: оставил как есть
    public DefaultExecutionEngine build(){
        this.nodes.forEach(n -> ((SimpleDbNode)n).init());
        System.out.print("engine builded");
        return this;
    }

    //todo сделать балансировку
    //complete
    Map<DbNode, AtomicInteger> loaded = new ConcurrentHashMap<>();
    private final List<LoadObserver> observers = new CopyOnWriteArrayList<>();


    public DefaultExecutionEngine( TimeoutManager timeoutManager) {
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
    // Добавляем подписчика
    public DefaultExecutionEngine addObserver(LoadObserver observer) {
        this.observers.add(observer);
        return this;
    }

    // Метод уведомления
    private void notifyObservers(DbNode node) {
        int load = loaded.getOrDefault(node, new AtomicInteger(0)).get();
        for (LoadObserver observer : observers) {
            observer.onLoadChanged(node, load);
        }
    }

    @Override
    public boolean  tryExecute(DbRequest request) {

        if (!request.transition(RequestState.ASSIGNED, RequestState.EXECUTING)) {
            return false;
        }

        DbNode node = getMinLoadedNode();
        assert node != null;
        DbConnection connection = node.acquire();

        if (connection == null) {
            request.transition(RequestState.EXECUTING, RequestState.QUEUED);
            return false;
        }

        loaded.compute(node, (n, counter) -> {
            if (counter == null) counter = new AtomicInteger();
            counter.incrementAndGet(); // +1
            return counter;
        });
        notifyObservers(node); // уведомляем


        CompletableFuture
                .runAsync(() -> execute(request, connection))
                .whenComplete((v, ex) -> {

                    loaded.compute(node, (n, counter) -> {
                        if (counter == null) return new AtomicInteger(0);
                        counter.decrementAndGet(); // -1
                        return counter;
                    });

                    node.release(connection);
                    notifyObservers(node); // уведомляем

                    if (ex != null) {
                        request.transition(RequestState.EXECUTING,
                                RequestState.TIMED_OUT);
                    }
                });

        return true;
    }

//    private void execute(DbRequest request, DbConnection connection) {
//        try (connection) {
//            Object result = connection.execute(request.getSql());
//
//            request.promise().completeSuccess(result);
//            request.transition(RequestState.EXECUTING, RequestState.COMPLETED);
//
//        } catch (Exception e) {
//            request.promise().completeFailure(e);
//            request.transition(RequestState.EXECUTING, RequestState.COMPLETED);
//        } finally {
//            timeoutManager.unregister(request);
//        }
//    }

    private void execute(DbRequest request, DbConnection connection) {
        try (connection) { // безопасно: connection закроется и вернется в пул после выполнения
            Object result = connection.execute(request.getSql());
            request.promise().completeSuccess(result);
            request.transition(RequestState.EXECUTING, RequestState.COMPLETED);
        } catch (Exception e) {
            request.promise().completeFailure(e);
            request.transition(RequestState.EXECUTING, RequestState.COMPLETED);
        } finally {
            timeoutManager.unregister(request);
        }
    }


}

