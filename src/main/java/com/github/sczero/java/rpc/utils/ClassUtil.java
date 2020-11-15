package com.github.sczero.java.rpc.utils;

public final class ClassUtil {
    public static Class<?> forName(String clazzName) throws ClassNotFoundException {
        switch (clazzName) {
            case "byte":
                return byte.class;
            case "int":
                return int.class;
            case "short":
                return short.class;
            case "long":
                return long.class;
            case "double":
                return double.class;
            case "float":
                return float.class;
            case "boolean":
                return boolean.class;
            case "char":
                return char.class;
            case "void":
                return void.class;
            default:
                return Class.forName(clazzName);
        }
    }
}
