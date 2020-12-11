package com.github.sczero.java.rpc.sample;

import com.github.sczero.java.rpc.client.RpcClient;
import com.github.sczero.java.rpc.sample.model.Person;
import com.github.sczero.java.rpc.sample.service.HelloService;
import com.github.sczero.java.rpc.spring.annotation.EnableJavaRpc;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@EnableJavaRpc(basePackages = "com.github.sczero.java.rpc.sample.service")
@ComponentScan("com.github.sczero.java.rpc.sample")
public class SampleClient {
    public static void main(String[] args) {
        //local();
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(SampleClient.class);
        applicationContext.refresh();
        HelloService bean = applicationContext.getBean(HelloService.class);
        System.out.println(bean);
    }

    private static void local() {
        RpcClient rpcClient = new RpcClient("127.0.0.1", 8081);
        HelloService helloService = rpcClient.getService(HelloService.class);
//        for (int i = 0; i < 100; i++) {
//            long start = System.currentTimeMillis();
//            String world = helloService.say("this is test from client", i);
//            double end = (System.currentTimeMillis() - start);
//            System.out.println("耗时:" + end + "ms");
//        }
        Person person = helloService.sayNothing();
        System.out.println(person);
        person = helloService.sayPerson(person);
        System.out.println(person);
//        helloService.say("123");
    }
}
