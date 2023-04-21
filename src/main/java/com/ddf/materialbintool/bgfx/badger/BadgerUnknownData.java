package com.ddf.materialbintool.bgfx.badger;

import com.ddf.materialbintool.util.ByteBuf;

import java.nio.charset.StandardCharsets;

public class BadgerUnknownData {
    public String name;
    public short unkShort;
    public byte unkByte;

    public void read(ByteBuf buf) {
        int nameLen = buf.readUnsignedByte();
        name = new String(buf.readBytes(nameLen), StandardCharsets.UTF_8);
        unkShort = buf.readShortLE();
        unkByte = buf.readByte();
    }

    public void write(ByteBuf buf) {
        buf.writeByte(name.length());
        buf.writeBytes(name.getBytes(StandardCharsets.UTF_8));
        buf.writeShortLE(unkShort);
        buf.writeByte(unkByte);
    }
}
