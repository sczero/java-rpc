package com.github.sczero.java.rpc.sample;

import com.github.sczero.java.rpc.server.RpcServer;
import com.github.sczero.java.rpc.service.HelloService;
import com.github.sczero.java.rpc.service.HelloServiceImpl;

public class SampleServer {
    public static void main(String[] args) throws InterruptedException {
        new RpcServer()
                .register(HelloService.class, new HelloServiceImpl())
                .start(8081);
    }
}
