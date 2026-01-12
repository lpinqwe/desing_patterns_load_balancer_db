package org.example.states;


public final class CompletedState implements RequestState {

    public static final CompletedState INSTANCE = new CompletedState();
    private CompletedState() {}

    @Override
    public boolean canTransitionTo(RequestState next) {
        return false;
    }

    @Override
    public boolean isTerminal() {
        return true;
    }
}
