package com.github.sczero.java.rpc;

import com.github.sczero.java.rpc.server.RpcServer;

public class Entry {
    public static void main(String[] args) throws InterruptedException {
        new RpcServer().start(8080);
    }
}
