package com.ddf.materialbintool.materials.definition.badger;

import com.ddf.materialbintool.util.ByteBuf;

public class BadgerUniform {
    public String name;
    public byte unkByte;
    public short unkShort;

    public void read(ByteBuf buf) {
        name = buf.readStringLE();
        unkByte = buf.readByte();
        unkShort = buf.readShortLE();
    }

    public void write(ByteBuf buf) {
        buf.writeStringLE(name);
        buf.writeByte(unkByte);
        buf.writeShortLE(unkShort);
    }
}
