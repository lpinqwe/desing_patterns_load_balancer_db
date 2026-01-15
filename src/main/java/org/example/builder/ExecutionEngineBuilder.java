package org.example.builder;

import org.example.interfaces.*;
import org.example.objects.DefaultExecutionEngine;
import org.example.observer.LoadObserver;

import java.util.ArrayList;
import java.util.List;

public final class ExecutionEngineBuilder {

    private TimeoutManager timeoutManager;
    private final List<DbNode> nodes = new ArrayList<>();
    private final List<LoadObserver> observers = new ArrayList<>();

    // 1️⃣ Устанавливаем TimeoutManager (обязательный)
    public ExecutionEngineBuilder withTimeoutManager(TimeoutManager tm) {
        this.timeoutManager = tm;
        return this;
    }

    // 2️⃣ Добавляем один узел
    public ExecutionEngineBuilder addNode(DbNode node) {
        this.nodes.add(node);
        return this;
    }

    // 3️⃣ Добавляем Observer
    public ExecutionEngineBuilder addObserver(LoadObserver observer) {
        this.observers.add(observer);
        return this;
    }

    // 4️⃣ Собираем окончательный ExecutionEngine
    public DefaultExecutionEngine build() {
        if (timeoutManager == null) {
            throw new IllegalStateException("TimeoutManager must be provided");
        }

        DefaultExecutionEngine engine = new DefaultExecutionEngine(timeoutManager);

        // добавляем узлы
        nodes.forEach(engine::addNode);

        // добавляем observers
        observers.forEach(engine::addObserver);

        // финальная инициализация (если нужно)
        engine.build();

        return engine;
    }
}
