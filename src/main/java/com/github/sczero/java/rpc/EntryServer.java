package com.github.sczero.java.rpc;

import com.github.sczero.java.rpc.server.RpcServer;

public class EntryServer {
    public static void main(String[] args) throws InterruptedException {
        new RpcServer().start(8080);
    }
}
