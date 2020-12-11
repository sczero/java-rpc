package com.github.sczero.java.rpc.sample.service;

import com.github.sczero.java.rpc.exception.RpcException;
import com.github.sczero.java.rpc.sample.model.Person;
import com.github.sczero.java.rpc.spring.RpcService;

@RpcService
public class HelloServiceImpl implements HelloService {
    @Override
    public String say(String sth, int times) throws InterruptedException {
        System.out.println(Thread.currentThread().getName());
        Thread.sleep(200);
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

    @Override
    public Person sayNothing() {
        Person person = new Person();
        person.setName("奥术大师多");
        person.setAgeInt(1);
        person.setAgeInteger(1);
        return person;
    }

    @Override
    public Person sayPerson(Person person) {
        Person person1 = new Person();
        person1.setName(person.getName() + "back");
        person1.setAgeInt(person.getAgeInt() + 1);
        person1.setAgeInteger(person.getAgeInteger() + 2);
        return person1;
    }
}
