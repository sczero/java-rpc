package com.github.sczero.java.rpc.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public final class ExceptionUtil {
    public static String getStackTrace(Exception e) {
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw);) {
            e.printStackTrace(pw);
            return sw.toString();
        } catch (IOException ioException) {
            return null;
        }
    }
}
