package com.ddf.materialbintool.bgfx;

import com.ddf.materialbintool.util.ByteBuf;

import java.nio.charset.StandardCharsets;

public class Uniform {
    public String name;
    public byte type;
    public byte num;
    public short regIndex;
    public short regCount;

    public Uniform() {
    }

    public void readFrom(ByteBuf buf) {
        byte nameLength = buf.readByte();
        name = new String(buf.readBytes(nameLength), StandardCharsets.UTF_8);
        type = buf.readByte();
        num = buf.readByte();
        regIndex = buf.readShortLE();
        regCount = buf.readShortLE();
    }

    public void writeTo(ByteBuf buf) {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        buf.writeByte(nameBytes.length);
        buf.writeBytes(nameBytes);
        buf.writeByte(type);
        buf.writeByte(num);
        buf.writeShortLE(regIndex);
        buf.writeShortLE(regCount);
    }
}
