package org.example.observer;

public interface LoadObservable {
    void addObserver(LoadObserver observer);
    void removeObserver(LoadObserver observer);
}