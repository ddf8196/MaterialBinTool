package com.ddf.materialbintool.materials.definition;

import com.ddf.materialbintool.util.ByteBuf;

public class SemanticDescription {
    public InputSemantic input;
    public byte index;

    public void read(ByteBuf buf) {
        input = InputSemantic.get(buf.readByte());
        index = buf.readByte();
    }

    public void write(ByteBuf buf) {
        buf.writeByte(input.ordinal());
        buf.writeByte(index);
    }

    @Override
    public String toString() {
        return "SemanticDescription{" +
                "input=" + input +
                ", index=" + index +
                '}';
    }
}
