package com.github.sczero.java.rpc;

import com.github.sczero.java.rpc.service.HelloService;

public class EntryClient {
    public static void main(String[] args) throws InterruptedException, ClassNotFoundException {
//        RpcClient rpcClient = new RpcClient("127.0.0.1", 8081);
//        HelloService helloService = rpcClient.getService(HelloService.class);
//        String world = helloService.say("World是操作宋诚中\\阿斯顿发失落迷迭;方面萨拉;多方面", 2);
//        System.out.println(world);

        System.out.println(Class.forName(HelloService.class.getName()+"1123"));


    }
}
