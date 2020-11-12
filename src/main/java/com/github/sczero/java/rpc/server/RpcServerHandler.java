package com.github.sczero.java.rpc.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetAddress;
import java.util.Date;

public class RpcServerHandler extends SimpleChannelInboundHandler<String> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Send greeting for a new connection.
        ctx.pipeline().write("Welcome to " + InetAddress.getLocalHost().getHostName() + "!\r\n");
        ctx.pipeline().write("It is " + new Date() + " now.\r\n");
        ctx.pipeline().flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String content) {
        System.out.println("channelRead0");
        //System.out.println(request.readableBytes());
        //System.out.println(request.toString(StandardCharsets.UTF_8));
        String response;
        boolean close = false;
        if (content.isEmpty()) {
            response = "Please type something.\r\n";
        } else if ("bye".equals(content.toLowerCase())) {
            response = "Have a good day!\r\n";
            close = true;
        } else {
            response = "Did you say '" + content + "'?\r\n";
        }

        // We do not need to write a ChannelBuffer here.
        // We know the encoder inserted at TelnetPipelineFactory will do the conversion.
        ChannelFuture future = ctx.pipeline().write(response);
        // Close the connection after sending 'Have a good day!'
        // if the client has sent 'bye'.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.pipeline().flush();
    }
}
