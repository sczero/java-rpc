package com.github.sczero.java.rpc.sample;

import com.github.sczero.java.rpc.client.RpcClient;
import com.github.sczero.java.rpc.sample.service.HelloService;

public class SampleClient {
    public static void main(String[] args) throws InterruptedException, ClassNotFoundException {
        RpcClient rpcClient = new RpcClient("127.0.0.1", 8081);
        HelloService helloService = rpcClient.getService(HelloService.class);
        for (int i = 0; i < 100; i++) {
            long start = System.currentTimeMillis();
            String world = helloService.say("this is test from client", i);
            double end = (System.currentTimeMillis() - start);
            //System.out.println("返回:" + world);
            System.out.println("耗时:" + end + "ms");
        }
    }
}
