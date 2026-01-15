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

    @Override
    public boolean tryExecute(DbRequest request) {

        if (!request.markExecuting()) {
            return false;
        }

        DbNode node = getMinLoadedNode();
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

                    if (ex != null) {
                        request.timeout();
                    }
                });

        return true;
    }

    //NOTE: prewious realisation
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

//    private void execute(DbRequest request, DbConnection connection) {
//        try (connection) { // безопасно: connection закроется и вернется в пул после выполнения
//            Object result = connection.execute(request.getSql());
//            request.promise().completeSuccess(result);
//            request.transition(RequestState.EXECUTING, RequestState.COMPLETED);
//        } catch (Exception e) {
//            request.promise().completeFailure(e);
//            request.transition(RequestState.EXECUTING, RequestState.COMPLETED);
//        } finally {
//            timeoutManager.unregister(request);
//        }
//    }
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



}

