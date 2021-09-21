package com.ddf.materialbintool;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ByteBufferUtil {
    public static boolean getBoolean(ByteBuffer buffer) {
        return buffer.get() != 0;
    }

    public static byte[] getBytes(ByteBuffer buffer, int length) {
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return bytes;
    }

    public static byte[] getByteArray(ByteBuffer buffer) {
        int length = buffer.getInt();
        return getBytes(buffer, length);
    }

    public static String getString(ByteBuffer buffer) {
        return new String(getByteArray(buffer), StandardCharsets.UTF_8);
    }
}
