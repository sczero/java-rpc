package com.github.sczero.java.rpc.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import java.net.SocketAddress;

public class RpcClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("RpcClientHandler#channelActive");
        byte[] bytes = "abcdefg".getBytes();
        ByteBuf buf = ctx.alloc().buffer();
        buf.writeInt(bytes.length).writeBytes(bytes);
        ChannelFuture channelFuture = ctx.writeAndFlush(buf);
        channelFuture.addListener(ChannelFutureListener.CLOSE);
    }
}
