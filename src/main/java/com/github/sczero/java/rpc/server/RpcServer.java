package com.github.sczero.java.rpc.server;

import com.github.sczero.java.rpc.utils.ClassUtil;
import com.github.sczero.java.rpc.utils.ExceptionUtil;
import com.github.sczero.java.rpc.utils.HessianUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class RpcServer {
    private final ConcurrentHashMap<Class<?>, Object> factory = new ConcurrentHashMap<>();

    public RpcServer register(Class<?> clazz, Object object) {
        factory.put(clazz, object);
        return this;
    }

    public void start(int port) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup(10);
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new LoggingHandler(LogLevel.DEBUG))
                                    .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4))
                                    .addLast(new RpcServerHandler(factory));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}