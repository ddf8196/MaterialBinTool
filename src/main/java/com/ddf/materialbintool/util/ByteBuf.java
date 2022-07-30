package com.ddf.materialbintool.util;

import java.util.Arrays;

public class ByteBuf {
    private byte[] array;
    private int readerIndex = 0;
    private int writerIndex = 0;

    public ByteBuf() {
        this.array = new byte[1024];
    }

    public ByteBuf(byte[] array) {
        this.array = array;
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

    private void ensureWritable(int len) {
        if (writerIndex + len >= array.length) {
            int newCapacity = array.length + (array.length >> 1);
            array = Arrays.copyOf(array, newCapacity);
        }
    }

    public void writeByte(byte b) {
        ensureWritable(1);
        array[writerIndex++] = b;
    }

    public void writeUnsignedByte(int b) {
        ensureWritable(1);
        array[writerIndex++] = (byte) b;
    }

    public void writeShort(short s) {
        ensureWritable(2);
        array[writerIndex++] = (byte) (s >>> 8);
        array[writerIndex++] = (byte) s;
    }

    public void writeShortLE(short s) {
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

    public void writeBytes(byte[] bytes) {
        ensureWritable(bytes.length);
    }
}