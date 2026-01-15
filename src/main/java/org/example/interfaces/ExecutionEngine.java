package org.example.interfaces;

import org.example.observer.LoadObservable;
import org.example.observer.LoadObserver;

public interface ExecutionEngine extends LoadObservable {

    boolean tryExecute(DbRequest request);
    void addObserver(LoadObserver observer);
    void removeObserver(LoadObserver observer);

}
