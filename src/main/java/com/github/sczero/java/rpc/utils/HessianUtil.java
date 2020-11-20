package com.github.sczero.java.rpc.utils;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.github.sczero.java.rpc.exception.RpcException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HessianUtil {
    public static byte[] convertObject2Bytes(Object obj) {
        byte[] bytes;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Hessian2Output output = new Hessian2Output(outputStream);
            output.writeObject(obj);
            output.flush();
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RpcException("无法转换对象2Byte:" + obj);
        }
    }

    public static Object convertBytes2Object(byte[] bytes, Class<?> clazz) throws IOException {
        Hessian2Input in = new Hessian2Input(new ByteArrayInputStream(bytes));
        return in.readObject(clazz);
    }
}
