package com.ddf.materialbintool.materials.definition.badger;

import com.ddf.materialbintool.util.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class BadgerUniforms {
    public String name;
    public short unkShort;
    public byte unkByte;
    public List<BadgerUniform> uniforms;

    public void read(ByteBuf buf) {
        name = buf.readStringLE();
        unkShort = buf.readShortLE();
        unkByte = buf.readByte();

        short count = buf.readShortLE();
        uniforms = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            BadgerUniform uniform = new BadgerUniform();
            uniform.read(buf);
            uniforms.add(uniform);
        }
    }

    public void write(ByteBuf buf) {
        buf.writeStringLE(name);
        buf.writeShortLE(unkShort);
        buf.writeByte(unkByte);

        buf.writeShortLE(uniforms.size());
        for (BadgerUniform uniform : uniforms) {
            uniform.write(buf);
        }
    }
}
