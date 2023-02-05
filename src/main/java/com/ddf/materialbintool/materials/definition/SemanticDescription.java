package com.ddf.materialbintool.materials.definition;

import com.ddf.materialbintool.util.ByteBuf;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SemanticDescription that = (SemanticDescription) o;
        return index == that.index && input == that.input;
    }

    @Override
    public int hashCode() {
        return Objects.hash(input, index);
    }

    @Override
    public String toString() {
        return "SemanticDescription{" +
                "input=" + input +
                ", index=" + index +
                '}';
    }
}
