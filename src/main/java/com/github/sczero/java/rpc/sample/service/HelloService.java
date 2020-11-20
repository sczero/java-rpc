package com.github.sczero.java.rpc.sample.service;

public interface HelloService {
    
    String say(String sth, int times) throws InterruptedException;

    String say(String sth);
}
