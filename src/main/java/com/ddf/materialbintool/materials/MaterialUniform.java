package com.ddf.materialbintool.materials;

import com.ddf.materialbintool.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class MaterialUniform {
    private String name;
    private byte type;
    private byte count;
    private byte[] unknownBytes0;

    public MaterialUniform() {
    }

    public void readFrom(ByteBuf buf) {
        byte nameLength = buf.readByte();
        name = new String(ByteBufUtil.readBytes(buf, nameLength), StandardCharsets.UTF_8);
        type = buf.readByte();
        count = buf.readByte();
        unknownBytes0 = ByteBufUtil.readBytes(buf, 4);
    }

    public void writeTo(ByteBuf buf) {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        buf.writeByte(nameBytes.length);
        buf.writeBytes(nameBytes);
        buf.writeByte(type);
        buf.writeByte(count);
        buf.writeBytes(unknownBytes0);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getCount() {
        return count;
    }

    public void setCount(byte count) {
        this.count = count;
    }

    public byte[] getUnknownBytes0() {
        return unknownBytes0;
    }

    public void setUnknownBytes0(byte[] unknownBytes0) {
        this.unknownBytes0 = unknownBytes0;
    }
}
