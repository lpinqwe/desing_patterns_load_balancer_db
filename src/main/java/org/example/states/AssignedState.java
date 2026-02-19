package org.example.states;

import java.util.Set;


public final class AssignedState implements RequestState {

    public static final AssignedState INSTANCE = new AssignedState();
    private AssignedState() {}

    @Override
    public boolean canTransitionTo(RequestState next) {
        return next instanceof ExecutingState
                || next instanceof QueuedState
                || next instanceof TimedOutState;
    }

    @Override
    public RequestState onExecute() {
        return ExecutingState.INSTANCE;
    }

    @Override
    public RequestState onQueueBack() {
        return QueuedState.INSTANCE;
    }

    @Override
    public boolean isTimedOut() {
        return false;
    }
}
