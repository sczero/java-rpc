package com.github.sczero.java.rpc.client;

import com.github.sczero.java.rpc.constant.RpcConstant;
import com.github.sczero.java.rpc.utils.HessianUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

public class RpcClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final Class<?> clazz;
    private final Method method;
    private final Object[] paramObjects;
    private final Class<?>[] paramTypes;
    private volatile Object result;

    public RpcClientHandler(Class<?> clazz, Method method, Object[] args) {
        this.clazz = clazz;
        this.method = method;
        this.paramObjects = args;
        this.paramTypes = method.getParameterTypes();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("RpcClientHandler#channelActive");

        ByteBuf buf = ctx.alloc().buffer();

        int payloadLength = 0;
        buf.writeInt(0);//占位

        int version = 1;
        buf.writeInt(version);//版本
        payloadLength += (RpcConstant.INT_SIZE);

        byte[] clazzContent = clazz.getName().getBytes();//类
        buf.writeInt(clazzContent.length);
        payloadLength += (RpcConstant.INT_SIZE);
        buf.writeBytes(clazzContent);
        payloadLength += (clazzContent.length);

        byte[] methodContent = method.getName().getBytes();//方法
        buf.writeInt(methodContent.length);
        payloadLength += (RpcConstant.INT_SIZE);
        buf.writeBytes(methodContent);
        payloadLength += (methodContent.length);

        buf.writeInt(paramObjects.length);//参数个数
        payloadLength += (RpcConstant.INT_SIZE);

        for (int i = 0; i < paramObjects.length; i++) {
            String paramName = paramTypes[i].getName();
            byte[] paramNameContent = paramName.getBytes();
            buf.writeInt(paramNameContent.length);
            payloadLength += (RpcConstant.INT_SIZE);
            buf.writeBytes(paramNameContent);
            payloadLength += (paramNameContent.length);

            byte[] objectBytes = HessianUtil.convertObject2Bytes(paramObjects[i]);
            buf.writeInt(objectBytes.length);
            payloadLength += (RpcConstant.INT_SIZE);
            buf.writeBytes(objectBytes);
            payloadLength += (objectBytes.length);
        }

        buf.setInt(0, payloadLength);
        ctx.writeAndFlush(buf);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        //解析数据
        int payloadLength = msg.readInt();
        result = msg.readBytes(payloadLength).toString(StandardCharsets.UTF_8);
    }

    public Object getResult() {
        return result;
    }
}
