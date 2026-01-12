package org.example.states;

import java.util.Set;

abstract class AbstractState implements RequestState {

    private final String name;
    private final Set<RequestState> allowed;

    protected AbstractState(String name, Set<RequestState> allowed) {
        this.name = name;
        this.allowed = allowed;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean canTransitionTo(RequestState next) {
        return allowed.contains(next);
    }
}
