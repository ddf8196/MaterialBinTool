package com.ddf.materialbintool.materials.definition;

import com.ddf.materialbintool.util.ByteBuf;

public class SamplerDefinition {
    public byte reg;
    public SamplerAccess access;
    public byte precision;
    public boolean allowUnorderedAccess;
    public SamplerType type;
    public String textureFormat; //空字符串 / rgba16f / rgba8 / rg16f / r32ui

    public boolean hasDefaultTexture;
    public String defaultTexture; //white

    public boolean hasCustomTypeInfo;
    public CustomTypeInfo customTypeInfo;

    public SamplerDefinition() {
    }

    public void read(ByteBuf buf) {
        reg = buf.readByte();
        access = SamplerAccess.get(buf.readByte());
        precision = buf.readByte();
        allowUnorderedAccess = buf.readBoolean();
        type = SamplerType.get(buf.readByte());
        textureFormat = buf.readStringLE();

        hasDefaultTexture = buf.readBoolean();
        if (hasDefaultTexture) {
            defaultTexture = buf.readStringLE();
        }

        hasCustomTypeInfo = buf.readBoolean();
        if (hasCustomTypeInfo) {
            customTypeInfo = new CustomTypeInfo();
            customTypeInfo.name = buf.readStringLE();
            customTypeInfo.size = buf.readIntLE();
        }
    }

    public void write(ByteBuf buf) {
        buf.writeByte(reg);
        buf.writeByte(access.ordinal());
        buf.writeByte(precision);
        buf.writeBoolean(allowUnorderedAccess);
        buf.writeByte(type.ordinal());
        buf.writeStringLE(textureFormat);

        buf.writeBoolean(hasDefaultTexture);
        if (hasDefaultTexture) {
            buf.writeStringLE(defaultTexture);
        }

        buf.writeBoolean(hasCustomTypeInfo);
        if (hasCustomTypeInfo) {
            buf.writeStringLE(customTypeInfo.name);
            buf.writeIntLE(customTypeInfo.size);
        }
    }

    public static class CustomTypeInfo {
        public String name;
        public int size;
    }
}