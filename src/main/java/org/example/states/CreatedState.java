package org.example.states;

import java.util.Set;


public final class CreatedState implements RequestState {

    public static final CreatedState INSTANCE = new CreatedState();
    private CreatedState() {}
    @Override
    public RequestState onEnqueue() {
        return QueuedState.INSTANCE;
    }

    @Override
    public boolean isTimedOut() {
        return false;
    }

    @Override
    public boolean canTransitionTo(RequestState next) {
        return next instanceof QueuedState
                || next instanceof TimedOutState;
    }

    @Override
    public RequestState onAssigned() {
        return QueuedState.INSTANCE;
    }
}
