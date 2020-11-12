package com.github.sczero.java.rpc;

import com.github.sczero.java.rpc.client.RpcClient;
import com.github.sczero.java.rpc.server.RpcServer;

public class EntryClient {
    public static void main(String[] args) throws InterruptedException {
        new RpcClient().send("127.0.0.1",8080);
    }
}
