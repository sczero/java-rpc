package com.github.sczero.java.rpc.serializer;

public class RpcSerializerFactory {
    private static final RpcSerializer DEFAULT = new KryoRpcSerializer();

    public RpcSerializerFactory() {
    }

    public static RpcSerializer getInstance() {
        return DEFAULT;
    }
}
