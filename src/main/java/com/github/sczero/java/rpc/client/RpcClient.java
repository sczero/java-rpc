package com.github.sczero.java.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.lang.reflect.Proxy;
import java.util.Arrays;

public class RpcClient {
    private final String addr;
    private final int port;

    public RpcClient(String addr, int port) {
        this.addr = addr;
        this.port = port;
    }

    public <T> T getService(Class<T> clazz) {
        Object instance = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            System.out.println("方法:" + method);
            System.out.println("参数" + Arrays.toString(args));

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
                ChannelFuture channelFuture = bootstrap.connect(addr, port).sync();
                channelFuture.channel().closeFuture().sync();
            } finally {
                eventLoopGroup.shutdownGracefully();
            }
            return rpcClientHandler.getResult();
        });
        return (T) instance;
    }
}
