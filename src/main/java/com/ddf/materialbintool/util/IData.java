package com.ddf.materialbintool.util;

import io.netty.buffer.ByteBuf;

public interface IData {
    void read(ByteBuf buf);
    void write(ByteBuf buf);
}
