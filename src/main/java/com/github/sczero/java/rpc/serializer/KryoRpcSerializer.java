package com.github.sczero.java.rpc.serializer;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

public class KryoRpcSerializer implements RpcSerializer {
    @Override
    public byte[] serialize(Object obj) {
        try (Output out = new Output(1024, -1)) {
            getKryo().writeObjectOrNull(out, obj, obj.getClass());
            return out.toBytes();
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {

        try (Input in = new Input(bytes, 0, bytes.length)) {
            return getKryo().readObjectOrNull(in, clazz);
        }
    }

    private Kryo getKryo() {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setWarnUnregisteredClasses(false);
        kryo.setReferences(true);
        return kryo;
    }
}
