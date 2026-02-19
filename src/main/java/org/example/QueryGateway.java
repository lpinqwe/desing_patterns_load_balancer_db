package org.example;

import org.example.factory.DbRequestFactory;
import org.example.interfaces.DbRequest;
import org.example.interfaces.DbResult;
import org.example.interfaces.LoadBalancer;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class QueryGateway {

    private final DbRequestFactory factory;
    private final LoadBalancer loadBalancer;

    public QueryGateway(DbRequestFactory factory,
                        LoadBalancer loadBalancer) {
        this.factory = factory;
        this.loadBalancer = loadBalancer;
    }

    public CompletableFuture<DbResult> execute(String sql,
                                               String sessionId,
                                               Duration timeout,
                                               ProxyStatement.ProxyStatementSettings pss,
                                               ProxyConnection.PCS pcs
    ) {

        DbRequest request = factory.create(sql, timeout);
        request.setSessionId(sessionId); //note
        request.setProxySS(pss);
        request.setProxyCS(pcs);
        loadBalancer.submit(request);

        return request.future();
    }
}
