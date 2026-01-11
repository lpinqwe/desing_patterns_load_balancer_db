package org.example.objects;

import org.example.dto.RequestState;
import org.example.interfaces.DbConnection;
import org.example.interfaces.DbNode;
import org.example.interfaces.ExecutionEngine;
import org.example.interfaces.TimeoutManager;

import java.util.concurrent.CompletableFuture;

public final class DefaultExecutionEngine implements ExecutionEngine {

    private final DbNode node;
    private final TimeoutManager timeoutManager;

    public DefaultExecutionEngine(DbNode node, TimeoutManager timeoutManager) {
        this.node = node;
        this.timeoutManager = timeoutManager;
    }


    @Override
    public boolean tryExecute(DbRequest request) {

        if (!request.transition(RequestState.ASSIGNED, RequestState.EXECUTING)) {
            return false;
        }

        DbConnection connection = node.acquire();
        if (connection == null) {
            request.transition(RequestState.EXECUTING, RequestState.QUEUED);
            return false;
        }

        // Передаем ответственность за close() в execute
        CompletableFuture.runAsync(() -> execute(request, connection));

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

