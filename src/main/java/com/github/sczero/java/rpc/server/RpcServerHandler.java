package com.github.sczero.java.rpc.server;

import com.github.sczero.java.rpc.constant.RpcConstant;
import com.github.sczero.java.rpc.utils.ClassUtil;
import com.github.sczero.java.rpc.utils.ExceptionUtil;
import com.github.sczero.java.rpc.utils.HessianUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class RpcServerHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final ConcurrentHashMap<Class<?>, Object> factory;

    public RpcServerHandler(ConcurrentHashMap<Class<?>, Object> factory) {
        this.factory = factory;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        System.out.println("总长度：" + msg.readableBytes());
        //解析数据
        int payloadLength = msg.readInt();
        int version = msg.readInt();

        Class<?> clazz = Class.forName(new String(ByteBufUtil.getBytes(msg.readBytes(msg.readInt()))));
        String methodStr = new String(ByteBufUtil.getBytes(msg.readBytes(msg.readInt())));
        int paramCount = msg.readInt();
        Object[] paramObjArr = new Object[paramCount];
        Class<?>[] paramClazzArr = new Class[paramCount];
        for (int i = 0; i < paramCount; i++) {
            paramClazzArr[i] = ClassUtil.forName(new String(ByteBufUtil.getBytes(msg.readBytes(msg.readInt()))));
            paramObjArr[i] = HessianUtil.convertBytes2Object(
                    ByteBufUtil.getBytes(msg.readBytes(msg.readInt())),
                    paramClazzArr[i]);
        }
        Method method = clazz.getDeclaredMethod(methodStr, paramClazzArr);

        //调用对象和方法
        Object resultObject = null;
        Class<?> exceptionClazz = null;
        String exceptionMsg = null;
        try {
            resultObject = method.invoke(factory.get(clazz), paramObjArr);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            exceptionClazz = targetException.getClass();
            exceptionMsg = ExceptionUtil.getStackTrace(targetException);
        }
        System.out.println("调用方法结果:" + resultObject);
        System.out.println("调用方法异常:" + exceptionClazz);
        System.out.println("调用方法异常消息:" + exceptionMsg);

        ByteBuf buf = ctx.alloc().buffer();
        payloadLength = 0;
        buf.writeInt(0);//占位
        buf.writeInt(version);//版本
        payloadLength += (RpcConstant.INT_SIZE);
        if (exceptionClazz != null && exceptionMsg != null) {
            buf.writeBoolean(false);//异常
            payloadLength += (RpcConstant.BOOL_SIZE);

            byte[] exceptionClazzBytes = exceptionClazz.getName().getBytes();//异常类
            buf.writeInt(exceptionClazzBytes.length);
            payloadLength += (RpcConstant.INT_SIZE);
            buf.writeBytes(exceptionClazzBytes);
            payloadLength += (exceptionClazzBytes.length);
            byte[] exceptionMsgContent = exceptionMsg.getBytes();//异常类消息
            buf.writeInt(exceptionMsgContent.length);
            payloadLength += (RpcConstant.INT_SIZE);
            buf.writeBytes(exceptionMsgContent);
            payloadLength += (exceptionMsgContent.length);
        } else {
            //正常
            buf.writeBoolean(true);
            payloadLength += (RpcConstant.BOOL_SIZE);

            if (resultObject == null) {
                buf.writeInt(0);
                payloadLength += (RpcConstant.INT_SIZE);
            } else {
                String objName = resultObject.getClass().getName();
                byte[] objNameBytes = objName.getBytes();
                buf.writeInt(objNameBytes.length);
                payloadLength += (RpcConstant.INT_SIZE);
                buf.writeBytes(objNameBytes);
                payloadLength += (objNameBytes.length);

                byte[] objBytes = HessianUtil.convertObject2Bytes(resultObject);
                buf.writeInt(objBytes.length);
                payloadLength += (RpcConstant.INT_SIZE);
                buf.writeBytes(objBytes);
                payloadLength += (objBytes.length);
            }

        }
        buf.setInt(0, payloadLength);
        ChannelFuture channelFuture = ctx.writeAndFlush(buf);
        channelFuture.addListener(ChannelFutureListener.CLOSE);
    }
}