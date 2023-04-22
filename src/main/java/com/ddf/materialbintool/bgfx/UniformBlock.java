package com.ddf.materialbintool.bgfx;

import com.ddf.materialbintool.util.ByteBuf;

import java.nio.charset.StandardCharsets;

public class UniformBlock {
    public String name;
    public short size;
    public byte reg;

    public void read(ByteBuf buf) {
        int nameLen = buf.readUnsignedByte();
        name = new String(buf.readBytes(nameLen), StandardCharsets.UTF_8);
        size = buf.readShortLE();
        reg = buf.readByte();
    }

    public void write(ByteBuf buf) {
        buf.writeByte(name.length());
        buf.writeBytes(name.getBytes(StandardCharsets.UTF_8));
        buf.writeShortLE(size);
        buf.writeByte(reg);
    }
}
