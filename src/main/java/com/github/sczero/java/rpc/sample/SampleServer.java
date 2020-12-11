package com.github.sczero.java.rpc.sample;

import com.github.sczero.java.rpc.spring.EnableJavaRpc;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@EnableJavaRpc(basePackages = "com.github.sczero.java.rpc.sample.service")
@ComponentScan("com.github.sczero.java.rpc.sample")
public class SampleServer {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(SampleServer.class);
        applicationContext.refresh();
//        new RpcServer()
//                .register(HelloService.class, new HelloServiceImpl())
//                .start(8081);
    }
}
