package com.ddf.materialbintool.materials.definition;

import com.ddf.materialbintool.util.ByteBufUtil;
import com.google.gson.annotations.SerializedName;
import io.netty.buffer.ByteBuf;

public class SamplerDefinition {
    public byte reg;
    @SerializedName(value = "access", alternate = {"unknownByte1"})
    public byte access;
    @SerializedName(value = "precision", alternate = {"unknownByte2"})
    public byte precision;
    @SerializedName(value = "allowUnorderedAccess", alternate = {"unknownBool0"})
    public boolean allowUnorderedAccess;
    @SerializedName(value = "type", alternate = {"unknownByte3"})
    public byte type;
    public String textureFormat; //空字符串 / rgba16f / rgba8 / rg16f / r32ui

    public boolean hasUnknownInt;
    public int unknownInt; //1

    @SerializedName(value = "hasDefaultTexture", alternate = {"unknownBool1"})
    public boolean hasDefaultTexture;
    @SerializedName(value = "defaultTexture", alternate = {"unknownStr1"})
    public String defaultTexture; //white

    public boolean hasCustomTypeInfo;
    public CustomTypeInfo customTypeInfo;

    public SamplerDefinition() {
    }

    public void read(ByteBuf buf) {
        reg = buf.readByte();
        access = buf.readByte();
        precision = buf.readByte();
        allowUnorderedAccess = buf.readBoolean();
        type = buf.readByte();
        textureFormat = ByteBufUtil.readString(buf);

        int unkInt = buf.readIntLE();
        if (unkInt == 1) {
            hasUnknownInt = true;
            unknownInt = unkInt;
        } else {
            hasUnknownInt = false;
            buf.readerIndex(buf.readerIndex() - 4);
        }

        hasDefaultTexture = buf.readBoolean();
        if (hasDefaultTexture) {
            defaultTexture = ByteBufUtil.readString(buf);
        }

        hasCustomTypeInfo = buf.readBoolean();
        if (hasCustomTypeInfo) {
            customTypeInfo = new CustomTypeInfo();
            customTypeInfo.name = ByteBufUtil.readString(buf);
            customTypeInfo.size = buf.readIntLE();
        }
    }

    public void write(ByteBuf buf) {
        buf.writeByte(reg);
        buf.writeByte(access);
        buf.writeByte(precision);
        buf.writeBoolean(allowUnorderedAccess);
        buf.writeByte(type);
        ByteBufUtil.writeString(buf, textureFormat);

        if (hasUnknownInt) {
            buf.writeIntLE(unknownInt);
        }

        buf.writeBoolean(hasDefaultTexture);
        if (hasDefaultTexture) {
            ByteBufUtil.writeString(buf, defaultTexture);
        }

        buf.writeBoolean(hasCustomTypeInfo);
        if (hasCustomTypeInfo) {
            ByteBufUtil.writeString(buf, customTypeInfo.name);
            buf.writeIntLE(customTypeInfo.size);
        }
    }

    public static class CustomTypeInfo {
        @SerializedName(value = "name", alternate = {"unknownStr"})
        public String name;

        @SerializedName(value = "size", alternate = {"unknownInt"})
        public int size;
    }
}