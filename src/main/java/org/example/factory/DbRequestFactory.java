package org.example.factory;
import org.example.interfaces.DbRequest;
import org.example.objects.DbRequestImplementation;
import org.example.utils.RequestPromise;

import java.time.Duration;
import java.time.Instant;


public final class DbRequestFactory implements FactoryPattern {
    @Override
    public DbRequest create(String sql, Duration timeout) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("SQL must not be empty");
        }
        if (timeout == null || timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("Timeout must be positive");
        }

        RequestPromise promise = new RequestPromise();
        Instant deadline = Instant.now().plus(timeout);

        return new DbRequestImplementation(sql, promise, deadline);
    }


}


// Простая фабрика
//class TransportFactory {
//    public static Transport createTransport(String type) {
//        return switch (type.toLowerCase()) {
//            case "truck" -> new Truck();
//            case "ship" -> new Ship();
//            case "plane" -> new Plane();
//            default -> throw new IllegalArgumentException("Неизвестный тип транспорта: " + type);
//        };
//    }
//}
//
