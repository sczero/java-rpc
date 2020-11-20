package com.github.sczero.java.rpc.service;

import com.github.sczero.java.rpc.exception.RpcException;

public class HelloServiceImpl implements HelloService {
    @Override
    public String say(String sth, int times) throws InterruptedException {
        System.out.println(Thread.currentThread().getName());
        Thread.sleep(100);
        StringBuilder sb = new StringBuilder("Hello:\n");
        for (int i = 0; i < times; i++) {
            sb.append(sth).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String say(String sth) {
        throw new RpcException("test rpc exception:" + sth);
    }
}
