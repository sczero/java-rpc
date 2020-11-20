package com.github.sczero.java.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RpcClient {
    private final ConcurrentHashMap<Class<?>, Object> serviceMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, Set<Method>> methodMap = new ConcurrentHashMap<>();

    private final String addr;
    private final int port;

    public RpcClient(String addr, int port) {
        this.addr = addr;
        this.port = port;
    }

    public synchronized <T> T getService(Class<T> clazz) {
        Object instance = serviceMap.get(clazz);
        if (instance == null) {
            methodMap.put(clazz, new HashSet<>(Arrays.asList(clazz.getDeclaredMethods())));
            serviceMap.put(clazz, newProxy(clazz));
        }
        return (T) serviceMap.get(clazz);
    }

    private <T> Object newProxy(Class<T> clazz) {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            args = args != null ? args : new Object[0];
            if (!methodMap.get(clazz).contains(method)) {
                return clazz.getName() + "#" + method.getName();
            }
            RpcClientHandler rpcClientHandler = new RpcClientHandler(clazz, method, args);
            EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(eventLoopGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline()
                                        .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4))
                                        .addLast(rpcClientHandler);
                            }
                        });
                bootstrap.connect(addr, port)
                        .sync()
                        .channel()
                        .closeFuture()
                        .sync();
            } finally {
                eventLoopGroup.shutdownGracefully();
            }
            return rpcClientHandler.getResult();
        });
    }
}
