package com.github.sczero.java.rpc.serializer;

public interface RpcSerializer {
    byte[] serialize(Object obj);

    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
