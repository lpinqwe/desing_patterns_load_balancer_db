package org.example.facade;

import org.example.interfaces.ExecutionEngine;
import org.example.interfaces.LoadBalancer;

public class FacadeMetrics {
    private LoadBalancer loadBalancer;
    private ExecutionEngine engine;


    public void init(LoadBalancer loadBalancer, ExecutionEngine engine) {
        this.loadBalancer = loadBalancer;
        this.engine = engine;
    }

    public int getQueueLen(){
        return loadBalancer.getQueueLen();
    }

    public int getDBNodeCount(){
        return engine.nodeCount();
    }


}
