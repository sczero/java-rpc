package com.github.sczero.java.rpc.client;

import com.caucho.hessian.io.Hessian2Output;
import com.github.sczero.java.rpc.exception.RpcException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class RpcClientHandler extends ChannelInboundHandlerAdapter {

    private final Class<?> clazz;
    private final Method method;
    private final Object[] args;
    private Object result;

    private static int INT_SIZE = 4;

    public RpcClientHandler(Class<?> clazz, Method method, Object[] args) {
        this.clazz = clazz;
        this.method = method;
        this.args = args;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("RpcClientHandler#channelActive");

        ByteBuf buf = ctx.alloc().buffer();

        int totalLength = 0;
        buf.writeInt(0);//占位

        int version = 1;
        buf.writeInt(version);//版本
        totalLength += (INT_SIZE);

        byte[] clazzContent = clazz.getName().getBytes();
        buf.writeInt(clazzContent.length);
        totalLength += (INT_SIZE);
        buf.writeBytes(clazzContent);
        totalLength += (clazzContent.length);

        byte[] methodContent = method.getName().getBytes();
        buf.writeInt(methodContent.length);
        totalLength += (INT_SIZE);
        buf.writeBytes(methodContent);
        totalLength += (methodContent.length);

        int paramCount = args.length;
        buf.writeInt(paramCount);
        totalLength += (INT_SIZE);

        for (Object arg : args) {
            byte[] argContent = arg.getClass().getName().getBytes();
            buf.writeInt(argContent.length);
            totalLength += (INT_SIZE);
            buf.writeBytes(argContent);
            totalLength += (argContent.length);

            byte[] objectBytes = convertObject2Byte(arg);
            buf.writeInt(objectBytes.length);
            totalLength += (INT_SIZE);
            buf.writeBytes(objectBytes);
            totalLength += (objectBytes.length);
        }

        buf.setInt(0, totalLength);
        ChannelFuture channelFuture = ctx.writeAndFlush(buf);
        //channelFuture.addListener(ChannelFutureListener.CLOSE);
    }


    public byte[] convertObject2Byte(Object obj) {
        byte[] bytes;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Hessian2Output output = new Hessian2Output(outputStream);
            output.writeObject(obj);
            output.flush();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RpcException("无法转换对象2Byte:" + obj);
        }
    }

    public CompletableFuture getResult() {
        return null;
    }
}
