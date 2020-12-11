package com.github.sczero.java.rpc.spring.initializer;

import com.github.sczero.java.rpc.spring.annotation.EnableJavaRpc;
import com.github.sczero.java.rpc.spring.annotation.RpcService;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * 扫描注册有RpcService注解的service
 */
public class RpcServiceHandler implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes mapperScanAttrs = AnnotationAttributes
                .fromMap(importingClassMetadata.getAnnotationAttributes(EnableJavaRpc.class.getName()));
        if (mapperScanAttrs != null) {
            String[] basePackages = mapperScanAttrs.getStringArray("basePackages");
            ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(RpcService.class));
            scanner.scan(basePackages);
        }
    }
}
