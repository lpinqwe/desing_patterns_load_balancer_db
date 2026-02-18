package org.example.states;

import java.util.Set;


public final class QueuedState implements RequestState {

    public static final QueuedState INSTANCE = new QueuedState();
    private QueuedState() {}
    @Override
    public RequestState onAssign() {
        return AssignedState.INSTANCE;
    }

    @Override
    public boolean isTimedOut() {
        return false;
    }

    @Override
    public boolean canTransitionTo(RequestState next) {
        return next instanceof AssignedState
                || next instanceof TimedOutState;
    }

    @Override
    public RequestState onAssigned() {
        return AssignedState.INSTANCE;
    }
}
