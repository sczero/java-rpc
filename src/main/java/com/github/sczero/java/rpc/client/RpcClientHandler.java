package com.github.sczero.java.rpc.client;

import com.github.sczero.java.rpc.constant.RpcConstant;
import com.github.sczero.java.rpc.exception.RpcException;
import com.github.sczero.java.rpc.serializer.RpcSerializer;
import com.github.sczero.java.rpc.utils.ClassUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;

public class RpcClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final Class<?> clazz;
    private final Method method;
    private final Object[] paramObjects;
    private final Class<?>[] paramTypes;
    private volatile boolean invokeSuccess;
    private volatile Object result;
    private volatile Exception resultEx;

    public RpcClientHandler(Class<?> clazz, Method method, Object[] args) {
        this.clazz = clazz;
        this.method = method;
        this.paramObjects = args;
        this.paramTypes = method.getParameterTypes();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
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

            byte[] objectBytes = RpcSerializer.DEFAULT.serialize(paramObjects[i]);
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
        int version = msg.readInt();
        this.invokeSuccess = msg.readBoolean();
        if (this.invokeSuccess) {
            int i = msg.readInt();
            if (i == 0) {
                this.result = null;
            } else {
                String resultClazz = new String(ByteBufUtil.getBytes(msg.readBytes(i)));
                this.result = RpcSerializer.DEFAULT.deserialize(ByteBufUtil.getBytes(msg.readBytes(msg.readInt())), ClassUtil.forName(resultClazz));
            }
        } else {
            String exceptionClazz = new String(ByteBufUtil.getBytes(msg.readBytes(msg.readInt())));
            String exceptionMsg = new String(ByteBufUtil.getBytes(msg.readBytes(msg.readInt())));
            try {
                this.resultEx = (Exception) ClassUtil.forName(exceptionClazz)
                        .getConstructor(String.class)
                        .newInstance(exceptionMsg);
            } catch (ClassNotFoundException e) {
                this.resultEx = new RpcException(exceptionMsg);
            }
        }
    }

    public Object getResult() throws Exception {
        if (invokeSuccess) {
            return result;
        } else {
            throw resultEx;
        }
    }
}
