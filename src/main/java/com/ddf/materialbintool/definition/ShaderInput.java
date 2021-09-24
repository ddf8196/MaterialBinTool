package com.ddf.materialbintool.definition;

import com.ddf.materialbintool.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public class ShaderInput {
    private String name;
    private ShaderInputType type;
    private byte[] unknownBytes0;
    private boolean unknownBool1;
    private byte unknownByte0;

    public ShaderInput() {
    }

    public void readFrom(ByteBuf buf) {
        name = ByteBufUtil.readString(buf);
        type = ShaderInputType.get(buf.readByte());
        unknownBytes0 = buf.readBytes(4).array();
        unknownBool1 = buf.readBoolean();
        if (unknownBool1) {
            unknownByte0 = buf.readByte();
        }
    }

    public void writeTo(ByteBuf buf) {
        ByteBufUtil.writeString(buf, name);
        buf.writeByte(type.ordinal());
        buf.writeBytes(unknownBytes0);
        buf.writeBoolean(unknownBool1);
        if (unknownBool1) {
            buf.writeByte(unknownByte0);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ShaderInputType getType() {
        return type;
    }

    public void setType(ShaderInputType type) {
        this.type = type;
    }

    public byte[] getUnknownBytes0() {
        return unknownBytes0;
    }

    public void setUnknownBytes0(byte[] unknownBytes0) {
        this.unknownBytes0 = unknownBytes0;
    }

    public boolean isUnknownBool1() {
        return unknownBool1;
    }

    public void setUnknownBool1(boolean unknownBool1) {
        this.unknownBool1 = unknownBool1;
    }

    public byte getUnknownByte0() {
        return unknownByte0;
    }

    public void setUnknownByte0(byte unknownByte0) {
        this.unknownByte0 = unknownByte0;
    }
}
