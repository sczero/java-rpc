package com.github.sczero.java.rpc.marshal;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Test {
    public static void main(String[] args) throws IOException {
        byte[] bytes;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Hessian2Output output = new Hessian2Output(outputStream);
            output.writeObject("1");
            output.writeObject(new HashMap<>());
            output.writeObject(new ArrayList<Object>() {{
                add("asdsdsa");
            }});
            output.writeObject("3");
            output.writeObject("3");
            output.writeObject(new RuntimeException("哇嘎嘎"));
            output.flush();
            bytes = outputStream.toByteArray();
        }
        System.out.println(new String(bytes));

        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(bytes));
        System.out.println(input.readObject().getClass());
        System.out.println(input.readObject().getClass());
        System.out.println(input.readObject().getClass());
        System.out.println(input.readObject().getClass());

    }
}
