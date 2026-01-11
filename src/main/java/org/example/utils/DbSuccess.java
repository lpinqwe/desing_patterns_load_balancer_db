package org.example.utils;

import org.example.interfaces.DbResult;

public final class DbSuccess implements DbResult {
    private final Object data;

    public DbSuccess(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }
}
