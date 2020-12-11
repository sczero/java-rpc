package com.github.sczero.java.rpc.spring.initializer;

import com.github.sczero.java.rpc.server.RpcServer;
import com.github.sczero.java.rpc.spring.annotation.RpcService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.Lifecycle;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RpcServerInitializer implements ApplicationContextAware, EnvironmentAware, Lifecycle {

    private final RpcServer rpcServer = new RpcServer();
    private ApplicationContext applicationContext;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Environment environment;

    @PostConstruct
    public void init() {
        Map<String, Object> objectMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (objectMap.size() > 0) {
            executor.execute(() -> {
                for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
                    rpcServer.register(entry.getValue().getClass().getInterfaces()[0], entry.getValue());
                }
                try {
                    rpcServer.start(8081);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void start() {
        //TODO
    }

    @Override
    public void stop() {
        //TODO
    }

    @Override
    public boolean isRunning() {
        return !executor.isShutdown();
    }
}
