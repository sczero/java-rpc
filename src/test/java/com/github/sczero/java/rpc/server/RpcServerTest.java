package com.github.sczero.java.rpc.server;

import com.github.sczero.java.rpc.sample.service.HelloService;
import junit.framework.TestCase;

import java.lang.reflect.Method;

public class RpcServerTest extends TestCase {
    public void test() throws ClassNotFoundException {
        for (Method method : HelloService.class.getDeclaredMethods()) {
            System.out.println("method: " + method.getName());
            System.out.println("method param count:" + method.getParameterCount());
            for (Class<?> parameterType : method.getParameterTypes()) {
                System.out.println(parameterType);
            }
        }
        //System.out.println(Class.forName("int.class"));
        System.out.println(Class.class);
        System.out.println(void.class);
    }
}