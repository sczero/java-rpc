package com.github.sczero.java.rpc.spring;

import com.github.sczero.java.rpc.client.RpcClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;

public class RpcServiceRefBeanPostProcessor implements BeanPostProcessor {
    private final RpcClient rpcClient = new RpcClient("127.0.0.1", 8081);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        for (Field field : bean.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            RpcServiceRef rpcServiceRef = field.getDeclaredAnnotation(RpcServiceRef.class);
            if (rpcServiceRef != null) {
                try {
                    Object ref = field.get(bean);
                    if (ref == null) {
                        field.set(bean, rpcClient.getService(bean.getClass().getInterfaces()[0]));
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        }
        System.out.println(1);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println(2);
        return bean;
    }
}
