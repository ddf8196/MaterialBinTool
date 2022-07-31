package com.ddf.materialbintool.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ByteBuf {
    private byte[] array;
    private int readerIndex = 0;
    private int writerIndex = 0;

    public ByteBuf() {
        this(1024);
    }

    public ByteBuf(int size) {
        this.array = new byte[size];
    }

    public ByteBuf(byte[] array) {
        this.array = array;
        this.writerIndex = array.length;
    }

    public int readerIndex() {
        return readerIndex;
    }

    public void readerIndex(int index) {
        this.readerIndex = index;
    }

    public int writerIndex() {
        return writerIndex;
    }

    public void writerIndex(int index) {
        this.writerIndex = index;
    }

    public boolean isReadable() {
        return readerIndex < this.writerIndex;
    }

    public byte[] toByteArray() {
        return Arrays.copyOfRange(array, readerIndex, writerIndex);
    }

    public boolean readBoolean() {
        return array[readerIndex++] != 0;
    }

    public byte readByte() {
        return array[readerIndex++];
    }

    public int readUnsignedByte() {
        return array[readerIndex++] & 0xFF;
    }

    public short readShort() {
        return (short) ((array[readerIndex++] & 0xFF) << 8 | (array[readerIndex++] & 0xFF));
    }

    public short readShortLE() {
        return (short) ((array[readerIndex++] & 0xFF) | (array[readerIndex++] & 0xFF) << 8);
    }

    public int readInt() {
        return (array[readerIndex++] & 0xFF) << 24
                | (array[readerIndex++] & 0xFF) << 16
                | (array[readerIndex++] & 0xFF) << 8
                | (array[readerIndex++] & 0xFF);
    }

    public int readIntLE() {
        return (array[readerIndex++] & 0xFF)
                | (array[readerIndex++] & 0xFF) << 8
                | (array[readerIndex++] & 0xFF) << 16
                | (array[readerIndex++] & 0xFF) << 24;
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public float readFloatLE() {
        return Float.intBitsToFloat(readIntLE());
    }

    public long readLong() {
        return (long) (array[readerIndex++] & 0xFF) << 56
                | (long) (array[readerIndex++] & 0xFF) << 48
                | (long) (array[readerIndex++] & 0xFF) << 40
                | (long) (array[readerIndex++] & 0xFF) << 32
                | (long) (array[readerIndex++] & 0xFF) << 24
                | (long) (array[readerIndex++] & 0xFF) << 16
                | (long) (array[readerIndex++] & 0xFF) << 8
                | (array[readerIndex++] & 0xFF);
    }

    public long readLongLE() {
        return (long) array[readerIndex++] & 0xFF
                | (long) (array[readerIndex++] & 0xFF) << 8
                | (long) (array[readerIndex++] & 0xFF) << 16
                | (long) (array[readerIndex++] & 0xFF) << 24
                | (long) (array[readerIndex++] & 0xFF) << 32
                | (long) (array[readerIndex++] & 0xFF) << 40
                | (long) (array[readerIndex++] & 0xFF) << 48
                | (long) (array[readerIndex++] & 0xFF) << 56;
    }

    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    public double readDoubleLE() {
        return Double.longBitsToDouble(readLongLE());
    }

    public byte[] readBytes(int len) {
        byte[] result = Arrays.copyOfRange(array, readerIndex, readerIndex + len);
        readerIndex += len;
        return result;
    }

    public byte[] readByteArray() {
        int len = readInt();
        return readBytes(len);
    }

    public byte[] readByteArrayLE() {
        int len = readIntLE();
        return readBytes(len);
    }

    public String readString() {
        return new String(readByteArray(), StandardCharsets.UTF_8);
    }

    public String readStringLE() {
        return new String(readByteArrayLE(), StandardCharsets.UTF_8);
    }

    private void ensureWritable(int len) {
        if (writerIndex + len < array.length) {
            return;
        }
        int newCapacity = array.length;
        while (writerIndex + len >= newCapacity) {
            newCapacity = 1 + newCapacity + (newCapacity >> 1);
        }
        array = Arrays.copyOf(array, newCapacity);
    }

    public void writeBoolean(boolean b) {
        ensureWritable(1);
        array[writerIndex++] = (byte) (b ? 1 : 0);
    }

    public void writeByte(int b) {
        ensureWritable(1);
        array[writerIndex++] = (byte) b;
    }

    public void writeShort(int s) {
        ensureWritable(2);
        array[writerIndex++] = (byte) (s >>> 8);
        array[writerIndex++] = (byte) s;
    }

    public void writeShortLE(int s) {
        ensureWritable(2);
        array[writerIndex++] = (byte) s;
        array[writerIndex++] = (byte) (s >>> 8);
    }

    public void writeInt(int i) {
        ensureWritable(4);
        array[writerIndex++] = (byte) (i >>> 24);
        array[writerIndex++] = (byte) (i >>> 16);
        array[writerIndex++] = (byte) (i >>> 8);
        array[writerIndex++] = (byte) i;
    }

    public void writeIntLE(int i) {
        ensureWritable(4);
        array[writerIndex++] = (byte) i;
        array[writerIndex++] = (byte) (i >>> 8);
        array[writerIndex++] = (byte) (i >>> 16);
        array[writerIndex++] = (byte) (i >>> 24);
    }

    public void writeFloat(float f) {
        writeInt(Float.floatToRawIntBits(f));
    }

    public void writeFloatLE(float f) {
        writeIntLE(Float.floatToRawIntBits(f));
    }

    public void writeLong(long l) {
        ensureWritable(8);
        array[writerIndex++] = (byte) (l >>> 56);
        array[writerIndex++] = (byte) (l >>> 48);
        array[writerIndex++] = (byte) (l >>> 40);
        array[writerIndex++] = (byte) (l >>> 32);
        array[writerIndex++] = (byte) (l >>> 24);
        array[writerIndex++] = (byte) (l >>> 16);
        array[writerIndex++] = (byte) (l >>> 8);
        array[writerIndex++] = (byte) l;
    }

    public void writeLongLE(long l) {
        ensureWritable(8);
        array[writerIndex++] = (byte) l;
        array[writerIndex++] = (byte) (l >>> 8);
        array[writerIndex++] = (byte) (l >>> 16);
        array[writerIndex++] = (byte) (l >>> 24);
        array[writerIndex++] = (byte) (l >>> 32);
        array[writerIndex++] = (byte) (l >>> 40);
        array[writerIndex++] = (byte) (l >>> 48);
        array[writerIndex++] = (byte) (l >>> 56);
    }

    public void writeDouble(double d) {
        writeLong(Double.doubleToRawLongBits(d));
    }

    public void writeDoubleLE(double d) {
        writeLongLE(Double.doubleToRawLongBits(d));
    }

    public void writeBytes(byte[] bytes) {
        ensureWritable(bytes.length);
        System.arraycopy(bytes, 0, array, writerIndex, bytes.length);
        writerIndex += bytes.length;
    }

    public void writeByteArray(byte[] bytes) {
        writeInt(bytes.length);
        writeBytes(bytes);
    }

    public void writeByteArrayLE(byte[] bytes) {
        writeIntLE(bytes.length);
        writeBytes(bytes);
    }

    public void writeString(String string) {
        writeByteArray(string.getBytes(StandardCharsets.UTF_8));
    }

    public void writeStringLE(String string) {
        writeByteArrayLE(string.getBytes(StandardCharsets.UTF_8));
    }
}