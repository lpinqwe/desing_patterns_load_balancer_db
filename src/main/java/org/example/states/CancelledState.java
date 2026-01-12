package org.example.states;

import java.util.Set;

public final class CancelledState extends AbstractState {

    public static final CancelledState INSTANCE = new CancelledState();

    private CancelledState() {
        super("CANCELLED", Set.of());
    }

    @Override
    public boolean isTerminal() {
        return true;
    }
}
