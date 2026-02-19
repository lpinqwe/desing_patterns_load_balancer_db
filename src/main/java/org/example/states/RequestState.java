package org.example.states;



public interface RequestState {

    boolean canTransitionTo(RequestState next);

    default RequestState onAssigned() {
        throw new IllegalStateException("Cannot assign from " + this);
    }
    default RequestState onEnqueue() {
        throw new IllegalStateException("Cannot enqueue from " + this);
    }

    default RequestState onAssign() {
        throw new IllegalStateException("Cannot assign from " + this);
    }

    default RequestState onExecute() {
        throw new IllegalStateException("Cannot execute from " + this);
    }

    default RequestState onQueueBack() {
        throw new IllegalStateException("Cannot re-queue from " + this);
    }

    default RequestState onSuccess() {
        throw new IllegalStateException("Cannot succeed from " + this);
    }

    default RequestState onFailure() {
        throw new IllegalStateException("Cannot fail from " + this);
    }

    default RequestState onTimeout() {
        return TimedOutState.INSTANCE;
    }

    default boolean isTerminal() {
        return false;
    }

    boolean isTimedOut();
}
