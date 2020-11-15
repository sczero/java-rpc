package com.github.sczero.java.rpc;

import com.github.sczero.java.rpc.client.RpcClient;
import com.github.sczero.java.rpc.service.HelloService;

public class EntryClient {
    public static void main(String[] args) throws InterruptedException, ClassNotFoundException {
        RpcClient rpcClient = new RpcClient("127.0.0.1", 8081);
        HelloService helloService = rpcClient.getService(HelloService.class);
        if (helloService != null) {
            String world = helloService.say("this is test from client", 2);
            System.out.println("远程调用结果:" + world);
        }


//        System.out.println(Class.forName(HelloService.class.getName()+"1123"));


    }
}
