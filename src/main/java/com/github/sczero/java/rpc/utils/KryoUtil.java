package com.github.sczero.java.rpc.utils;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

import java.io.IOException;

public final class KryoUtil {
    private static final Kryo kryo = new Kryo();

    static {
        kryo.setRegistrationRequired(false);
        kryo.setWarnUnregisteredClasses(false);
        kryo.setReferences(true);
    }

    public static byte[] convertObject2Bytes(Object obj) {
        try (Output out = new Output(1024, -1)) {
            kryo.writeObjectOrNull(out, obj, obj.getClass());
            return out.toBytes();
        }
    }

    public static Object convertBytes2Object(byte[] bytes, Class<?> clazz) throws IOException {
        try (Input in = new Input(bytes, 0, bytes.length)) {
            return kryo.readObjectOrNull(in, clazz);
        }
    }
}
