package com.github.sczero.java.rpc.sample.service;

import com.github.sczero.java.rpc.sample.model.Person;

public interface HelloService {

    String say(String sth, int times) throws InterruptedException;

    String say(String sth);

    Person sayNothing();
}
