package org.example.utils;

import org.example.QueryGateway;
import org.example.SimpleHttpServer;
import org.example.builder.ExecutionEngineBuilder;
import org.example.facade.FacadeMetrics;
import org.example.factory.DbRequestFactory;
import org.example.interfaces.*;
import org.example.objects.*;
import org.example.observer.LoadObserver;

import java.util.ArrayList;
import java.util.List;

public class DatabaseServerBuilder {

    private final List<DbNode> nodes = new ArrayList<>();
    private TimeoutManager timeoutManager;
    private ExecutionEngine engine;
    private LoadBalancer loadBalancer;
    private RequestQueue queue;
    private DbRequestFactory factory;
    private SimpleHttpServer server;
    private FacadeMetrics facade;
    private int port = 8080; // default port
    int countThreads;

    public DatabaseServerBuilder withMetrics(FacadeMetrics facade) {
        this.facade = facade;
        //facade.init(loadBalancer, engine);
        return this;
    }


    public DatabaseServerBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    public DatabaseServerBuilder withTimeoutThreads(int threads) {

        this.countThreads = threads;
        return this;
    }

    //todo: contain it before buider build()
    static private class NodeInfo {
        String id;
        int poolSize;
        String jdbcUrl;
        String user;
        String pass;

        public NodeInfo(String id, int poolSize, String jdbcUrl, String user, String pass) {
            this.id = id;
            this.poolSize = poolSize;
            this.jdbcUrl = jdbcUrl;
            this.user = user;
            this.pass = pass;
        }

    }

    private List<NodeInfo> LST = new ArrayList<NodeInfo>();

    public DatabaseServerBuilder addNode(String id, int poolSize, String jdbcUrl, String user, String pass) throws Exception {

        LST.add(new NodeInfo(id, poolSize, jdbcUrl, user, pass));

        return this;

    }

    private void buildNode() {

        LST.forEach(it -> {
            JdbcConnectionPool pool = null;
            try {
                pool = new JdbcConnectionPool(it.poolSize, it.jdbcUrl, it.user, it.pass);

                DbNode node = new SimpleDbNode(it.id, pool);
                nodes.add(node);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    private List<LoadObserver> observers = new ArrayList<>();

    public DatabaseServerBuilder withObserver(LoadObserver observer) {
        if (observer != null) {
            observers.add(observer);
        }
        return this;
    }

//    public DatabaseServerBuilder withObserver(LoadObserver observer) {
//        if (engine != null) {
//            engine.addObserver(observer);
//        }
//        return this;
//    }

    // --- Build all components ---
    public QueryGateway build() {

        if (countThreads < 1) {
            throw new IllegalStateException("countThreads must be >= 1");
        }

        this.buildNode();

        this.timeoutManager = new ScheduledTimeoutManager(countThreads);

        ExecutionEngineBuilder engineBuilder = new ExecutionEngineBuilder()
                .withTimeoutManager(timeoutManager);

        nodes.forEach(engineBuilder::addNode);
        engine = engineBuilder.build();
        observers.forEach(engine::addObserver);

        queue = new FifoRequestQueue();
        loadBalancer = new DefaultLoadBalancer(queue, engine, timeoutManager);
        factory = new DbRequestFactory();

        if (facade != null) {
            this.facade.init(loadBalancer, engine);
        }

        return new QueryGateway(factory, loadBalancer);  // <- возвращаем gateway
    }


    public void start() throws Exception {
        if (server == null) {
            throw new IllegalStateException("Server not built. Call build() first.");
        }
        server.start();
    }
}

