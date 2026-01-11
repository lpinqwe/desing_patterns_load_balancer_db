package org.example.utils;

import org.example.interfaces.DbResult;

public final class DbFailure implements DbResult {
    private final Throwable error;

    public DbFailure(Throwable error) {
        this.error = error;
    }

    public Throwable getError() {
        return error;
    }
}
