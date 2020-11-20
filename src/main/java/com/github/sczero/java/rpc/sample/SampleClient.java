package com.github.sczero.java.rpc.sample;

import com.github.sczero.java.rpc.client.RpcClient;
import com.github.sczero.java.rpc.service.HelloService;

public class SampleClient {
    public static void main(String[] args) throws InterruptedException, ClassNotFoundException {
        RpcClient rpcClient = new RpcClient("127.0.0.1", 8081);
        HelloService helloService = rpcClient.getService(HelloService.class);
        for (int i = 0; i < 100; i++) {
            String world = helloService.say("this is test from client", 0);
        }
    }
}
