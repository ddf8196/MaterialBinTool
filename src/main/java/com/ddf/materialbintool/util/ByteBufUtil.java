package com.ddf.materialbintool.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.buffer.UnpooledHeapByteBuf;

import java.nio.charset.StandardCharsets;

public class ByteBufUtil {
    private static final UnpooledByteBufAllocator ALLOC = new UnpooledByteBufAllocator(false);

    public static ByteBuf buffer() {
        return ALLOC.buffer();
    }

    public static ByteBuf buffer(int initialCapacity) {
        return ALLOC.buffer(initialCapacity);
    }

    public static ByteBuf buffer(int initialCapacity, int maxCapacity) {
        return ALLOC.buffer(initialCapacity, maxCapacity);
    }

    public static ByteBuf wrappedBuffer(byte[] array) {
        UnpooledHeapByteBuf heapByteBuf = new UnpooledHeapByteBuf(ALLOC, array.length, array.length);
        heapByteBuf.writeBytes(array);
        return heapByteBuf;
    }

    public static byte[] readBytes(ByteBuf buffer, int length) {
        byte[] bytes = new byte[length];
        buffer.readBytes(bytes);
        return bytes;
    }

    public static byte[] readAllBytes(ByteBuf buffer) {
        return readBytes(buffer, buffer.readableBytes());
    }

    public static byte[] readByteArray(ByteBuf buffer) {
        int length = buffer.readIntLE();
        return readBytes(buffer, length);
    }

    public static String readString(ByteBuf buffer) {
        return new String(readByteArray(buffer), StandardCharsets.UTF_8);
    }

    public static void writeByteArray(ByteBuf buffer, byte[] array) {
        buffer.writeIntLE(array.length);
        buffer.writeBytes(array);
    }

    public static void writeString(ByteBuf buffer, String string) {
        writeByteArray(buffer, string.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] toByteArray(ByteBuf buffer) {
        byte[] array = new byte[buffer.readableBytes()];
        buffer.readBytes(array);
        return array;
    }
}
