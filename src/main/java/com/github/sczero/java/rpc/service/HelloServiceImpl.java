package com.github.sczero.java.rpc.service;

public class HelloServiceImpl implements HelloService {
    @Override
    public String say(String sth, int times) {
        StringBuilder sb = new StringBuilder("Hello ");
        for (int i = 0; i < times; i++) {
            sb.append(sth).append(" ");
        }
        return sb.toString();
    }
}
