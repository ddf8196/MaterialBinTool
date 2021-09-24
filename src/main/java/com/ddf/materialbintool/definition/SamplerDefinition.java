package com.ddf.materialbintool.definition;

import com.ddf.materialbintool.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;

public class SamplerDefinition {
    private String name;

    private byte unknownByte0;
    private byte unknownByte1;
    private byte unknownByte2;
    private boolean unknownBool0;
    private byte unknownByte3;
    private String unknownStr0;

    private boolean unknownBool1;
    private String unknownStr1;

    private boolean unknownBool2;
    private String unknownStr2;
    private int unknownInt0;

    public SamplerDefinition() {
    }

    public void readFrom(ByteBuf buf) {
        name = ByteBufUtil.readString(buf);
        unknownByte0 = buf.readByte();
        unknownByte1 = buf.readByte();
        unknownByte2 = buf.readByte();
        unknownBool0 = buf.readBoolean();
        unknownByte3 = buf.readByte();
        unknownStr0 = ByteBufUtil.readString(buf);

        unknownBool1 = buf.readBoolean();
        if (unknownBool1) {
            unknownStr1 = ByteBufUtil.readString(buf);
        }

        unknownBool2 = buf.readBoolean();
        if (unknownBool2) {
            unknownStr2 = ByteBufUtil.readString(buf);
            unknownInt0 = buf.readIntLE();
        }
    }

    public void writeTo(ByteBuf buf) {
        ByteBufUtil.writeString(buf, name);
        buf.writeByte(unknownByte0);
        buf.writeByte(unknownByte1);
        buf.writeByte(unknownByte2);
        buf.writeBoolean(unknownBool0);
        buf.writeByte(unknownByte3);
        ByteBufUtil.writeString(buf, unknownStr0);

        buf.writeBoolean(unknownBool1);
        if (unknownBool1) {
            ByteBufUtil.writeString(buf, unknownStr1);
        }

        buf.writeBoolean(unknownBool2);
        if (unknownBool2) {
            ByteBufUtil.writeString(buf, unknownStr2);
            buf.writeIntLE(unknownInt0);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getUnknownByte0() {
        return unknownByte0;
    }

    public void setUnknownByte0(byte unknownByte0) {
        this.unknownByte0 = unknownByte0;
    }

    public byte getUnknownByte1() {
        return unknownByte1;
    }

    public void setUnknownByte1(byte unknownByte1) {
        this.unknownByte1 = unknownByte1;
    }

    public byte getUnknownByte2() {
        return unknownByte2;
    }

    public void setUnknownByte2(byte unknownByte2) {
        this.unknownByte2 = unknownByte2;
    }

    public boolean isUnknownBool0() {
        return unknownBool0;
    }

    public void setUnknownBool0(boolean unknownBool0) {
        this.unknownBool0 = unknownBool0;
    }

    public byte getUnknownByte3() {
        return unknownByte3;
    }

    public void setUnknownByte3(byte unknownByte3) {
        this.unknownByte3 = unknownByte3;
    }

    public String getUnknownStr0() {
        return unknownStr0;
    }

    public void setUnknownStr0(String unknownStr0) {
        this.unknownStr0 = unknownStr0;
    }

    public boolean isUnknownBool1() {
        return unknownBool1;
    }

    public void setUnknownBool1(boolean unknownBool1) {
        this.unknownBool1 = unknownBool1;
    }

    public String getUnknownStr1() {
        return unknownStr1;
    }

    public void setUnknownStr1(String unknownStr1) {
        this.unknownStr1 = unknownStr1;
    }

    public boolean isUnknownBool2() {
        return unknownBool2;
    }

    public void setUnknownBool2(boolean unknownBool2) {
        this.unknownBool2 = unknownBool2;
    }

    public String getUnknownStr2() {
        return unknownStr2;
    }

    public void setUnknownStr2(String unknownStr2) {
        this.unknownStr2 = unknownStr2;
    }

    public int getUnknownInt0() {
        return unknownInt0;
    }

    public void setUnknownInt0(int unknownInt0) {
        this.unknownInt0 = unknownInt0;
    }
}