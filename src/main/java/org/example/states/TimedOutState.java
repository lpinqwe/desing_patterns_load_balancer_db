package org.example.states;

import java.util.Set;


public final class TimedOutState implements RequestState {

    public static final TimedOutState INSTANCE = new TimedOutState();
    private TimedOutState() {}

    @Override
    public boolean canTransitionTo(RequestState next) {
        return next instanceof QueuedState;

    }
    public boolean isTimedOut (){
        System.out.println("detected timeout,shpuld be returned to queue");

        return true;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }
}
