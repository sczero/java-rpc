package com.github.sczero.java.rpc;

import com.github.sczero.java.rpc.server.RpcServer;
import com.github.sczero.java.rpc.service.HelloService;
import com.github.sczero.java.rpc.service.HelloServiceImpl;

public class EntryServer {
    public static void main(String[] args) throws InterruptedException {
        new RpcServer()
                .register(HelloService.class, new HelloServiceImpl())
                .start(8081);
    }
}
