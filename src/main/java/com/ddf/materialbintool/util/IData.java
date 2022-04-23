package com.ddf.materialbintool.util;

import io.netty.buffer.ByteBuf;

public interface IData {
    void read(ByteBuf buf);
    void write(ByteBuf buf);

    default void read(byte[] array) {
        read(ByteBufUtil.wrappedBuffer(array));
    }

    default byte[] toByteArray() {
        ByteBuf buf = ByteBufUtil.buffer();
        write(buf);
        return ByteBufUtil.toByteArray(buf);
    }
}
