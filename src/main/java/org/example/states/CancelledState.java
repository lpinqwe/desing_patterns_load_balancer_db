package org.example.states;

@Deprecated
public final class CancelledState implements RequestState {

    public static final CancelledState INSTANCE = new CancelledState();

    private CancelledState() {
        //  super("CANCELLED", Set.of());
    }

    @Override
    public boolean canTransitionTo(RequestState next) {
        return false;
    }

    @Override
    public boolean isTerminal() {
        return true;
    }
}
