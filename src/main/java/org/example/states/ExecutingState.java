package org.example.states;

import java.util.Set;


public final class ExecutingState implements RequestState {

    public static final ExecutingState INSTANCE = new ExecutingState();
    private ExecutingState() {}

    @Override
    public boolean canTransitionTo(RequestState next) {
        return next instanceof CompletedState;
    }

    @Override
    public RequestState onSuccess() {
        return CompletedState.INSTANCE;
    }

    @Override
    public RequestState onFailure() {
        return CompletedState.INSTANCE;
    }

    @Override
    public boolean isTimedOut() {
        return false;
    }
}
