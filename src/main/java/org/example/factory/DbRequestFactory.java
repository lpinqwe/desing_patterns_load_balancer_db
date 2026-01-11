package org.example.factory;
import org.example.objects.DbRequest;
import org.example.utils.RequestPromise;

import java.time.Duration;
import java.time.Instant;

public final class DbRequestFactory {

    public DbRequest create(String sql, Duration timeout) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("SQL must not be empty");
        }

        RequestPromise promise = new RequestPromise();
        Instant deadline = Instant.now().plus(timeout);

        return new DbRequest(sql, promise, deadline);
    }
}

