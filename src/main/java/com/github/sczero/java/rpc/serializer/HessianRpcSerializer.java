package com.github.sczero.java.rpc.serializer;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.github.sczero.java.rpc.exception.RpcException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HessianRpcSerializer implements RpcSerializer {
    @Override
    public byte[] serialize(Object obj) {
        byte[] bytes;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Hessian2Output output = new Hessian2Output(outputStream);
            output.writeObject(obj);
            output.flush();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RpcException("hessian serialize error:" + e.getMessage());
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        Hessian2Input in = new Hessian2Input(new ByteArrayInputStream(bytes));
        try {
            return (T) in.readObject(clazz);
        } catch (IOException e) {
            throw new RpcException("hessian deserialize error:" + e.getMessage());
        }
    }
}
