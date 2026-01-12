package org.example.states;

import java.util.Set;


public final class TimedOutState implements RequestState {

    public static final TimedOutState INSTANCE = new TimedOutState();
    private TimedOutState() {}

    @Override
    public boolean canTransitionTo(RequestState next) {
        return false;
    }

    @Override
    public boolean isTerminal() {
        return true;
    }
}
