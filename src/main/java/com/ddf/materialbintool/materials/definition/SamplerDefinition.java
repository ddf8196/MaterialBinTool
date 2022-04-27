package com.ddf.materialbintool.materials.definition;

import com.ddf.materialbintool.util.ByteBufUtil;
import com.ddf.materialbintool.util.IData;
import io.netty.buffer.ByteBuf;

public class SamplerDefinition implements IData {
    public byte index;
    public byte unknownByte1;
    public byte unknownByte2;
    public boolean unknownBool0;
    public byte unknownByte3;
    public String textureFormat; //空字符串 / rgba16f / rgba8 / rg16f / r32ui

    public boolean hasUnknownInt;
    public int unknownInt;

    public boolean unknownBool1;
    public String unknownStr1; //white

    public boolean hasCustomTypeInfo;
    public CustomTypeInfo customTypeInfo;

    public SamplerDefinition() {
    }

    public void read(ByteBuf buf) {
        index = buf.readByte();
        unknownByte1 = buf.readByte();
        unknownByte2 = buf.readByte();
        unknownBool0 = buf.readBoolean();
        unknownByte3 = buf.readByte();
        textureFormat = ByteBufUtil.readString(buf);

        int unkInt = buf.readIntLE();
        if (unkInt == 1) {
            hasUnknownInt = true;
            unknownInt = unkInt;
        } else {
            hasUnknownInt = false;
            buf.readerIndex(buf.readerIndex() - 4);
        }

        unknownBool1 = buf.readBoolean();
        if (unknownBool1) {
            unknownStr1 = ByteBufUtil.readString(buf);
        }

        hasCustomTypeInfo = buf.readBoolean();
        if (hasCustomTypeInfo) {
            customTypeInfo = new CustomTypeInfo();
            customTypeInfo.unknownStr = ByteBufUtil.readString(buf);
            customTypeInfo.unknownInt = buf.readIntLE();
        }
    }

    public void write(ByteBuf buf) {
        buf.writeByte(index);
        buf.writeByte(unknownByte1);
        buf.writeByte(unknownByte2);
        buf.writeBoolean(unknownBool0);
        buf.writeByte(unknownByte3);
        ByteBufUtil.writeString(buf, textureFormat);

        if (hasUnknownInt) {
            buf.writeIntLE(unknownInt);
        }

        buf.writeBoolean(unknownBool1);
        if (unknownBool1) {
            ByteBufUtil.writeString(buf, unknownStr1);
        }

        buf.writeBoolean(hasCustomTypeInfo);
        if (hasCustomTypeInfo) {
            ByteBufUtil.writeString(buf, customTypeInfo.unknownStr);
            buf.writeIntLE(customTypeInfo.unknownInt);
        }
    }

    public static class CustomTypeInfo {
        public String unknownStr;
        public int unknownInt;
    }
}