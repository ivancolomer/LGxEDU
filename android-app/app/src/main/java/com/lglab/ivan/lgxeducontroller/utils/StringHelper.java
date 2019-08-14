package com.lglab.ivan.lgxeducontroller.utils;

import java.nio.charset.StandardCharsets;

public class StringHelper {

    // convert from UTF-8 -> internal Java String format
    public static String convertFromUTF8(String s) {
        String out = null;
        out = new String(s.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        return out;
    }

    // convert from internal Java String format -> UTF-8
    public static String convertToUTF8(String s) {
        String out = null;
        out = new String(s.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        return out;
    }
}
