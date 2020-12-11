package com.github.sczero.java.rpc.spring.annotation;

import com.github.sczero.java.rpc.spring.initializer.RpcReferenceHandler;
import com.github.sczero.java.rpc.spring.initializer.RpcServerInitializer;
import com.github.sczero.java.rpc.spring.initializer.RpcServiceHandler;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 对于服务端只要注册service到bean里面就行了,通过RpcService自定义注解
 * 对于客户端需要扫到对应的interface,然后动态生成proxy,注册到SpringFactory中
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({RpcServiceHandler.class, RpcReferenceHandler.class, RpcServerInitializer.class})
public @interface EnableJavaRpc {
    String[] basePackages() default {};
}
