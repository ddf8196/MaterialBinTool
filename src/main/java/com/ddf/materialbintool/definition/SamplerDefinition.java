package com.ddf.materialbintool.definition;

import com.ddf.materialbintool.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;

public class SamplerDefinition {
    private byte unknownByte0;
    private byte unknownByte1;
    private byte unknownByte2;
    private boolean unknownBool0;
    private byte unknownByte3;
    private String unknownStr0; //rgba16f / rgba8 / rg16f / r32ui

    private boolean unknownBool1;
    private String unknownStr1; //white

    private boolean hasCustomTypeInfo;
    private CustomTypeInfo customTypeInfo;

    public SamplerDefinition() {
    }

    public void readFrom(ByteBuf buf) {
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

        hasCustomTypeInfo = buf.readBoolean();
        if (hasCustomTypeInfo) {
            customTypeInfo = new CustomTypeInfo();
            customTypeInfo.unknownStr = ByteBufUtil.readString(buf);
            customTypeInfo.unknownInt = buf.readIntLE();
        }
    }

    public void writeTo(ByteBuf buf) {
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