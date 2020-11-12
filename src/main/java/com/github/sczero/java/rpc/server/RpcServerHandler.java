package com.github.sczero.java.rpc.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.StandardCharsets;

public class RpcServerHandler extends SimpleChannelInboundHandler<ByteBuf> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        System.out.println("--------");
        System.out.println(this);
        System.out.println("总长度：" + msg.readableBytes());
        int length = msg.readInt();
        System.out.println("负载长度：" + length);
        System.out.printf("消息:'%s'%n", msg.readBytes(length).toString(StandardCharsets.UTF_8));
        System.out.println("--------");
    }
}
