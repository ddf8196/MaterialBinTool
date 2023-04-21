package com.ddf.materialbintool.materials.definition;

import com.ddf.materialbintool.util.ByteBuf;

import java.util.Objects;

public class SamplerDefinition {
    public short reg;
    public SamplerAccess access;
    public byte precision;
    public boolean allowUnorderedAccess;
    public SamplerType type;
    public String textureFormat; //空字符串 / rgba16f / rgba8 / rg16f / r32ui

    public int unknownInt; //1
    public byte unknownByte;

    public boolean hasDefaultTexture;
    public String defaultTexture; //white

    public boolean hasCustomTypeInfo;
    public CustomTypeInfo customTypeInfo;

    public SamplerDefinition() {
    }

    public void read(ByteBuf buf) {
        reg = buf.readShortLE();
        access = SamplerAccess.get(buf.readByte());
        precision = buf.readByte();
        allowUnorderedAccess = buf.readBoolean();
        type = SamplerType.get(buf.readByte());
        textureFormat = buf.readStringLE();
        unknownInt = buf.readIntLE(); //1
        unknownByte = buf.readByte();

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
        buf.writeShortLE(reg);
        buf.writeByte(access.ordinal());
        buf.writeByte(precision);
        buf.writeBoolean(allowUnorderedAccess);
        buf.writeByte(type.ordinal());
        buf.writeStringLE(textureFormat);
        buf.writeIntLE(unknownInt);
        buf.writeByte(unknownByte);

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SamplerDefinition that = (SamplerDefinition) o;
        return reg == that.reg
                && access == that.access
                && precision == that.precision
                && allowUnorderedAccess == that.allowUnorderedAccess
                && type == that.type
                && unknownInt == that.unknownInt
                && unknownByte == that.unknownByte
                && hasDefaultTexture == that.hasDefaultTexture
                && hasCustomTypeInfo == that.hasCustomTypeInfo
                && Objects.equals(textureFormat, that.textureFormat)
                && Objects.equals(defaultTexture, that.defaultTexture)
                && Objects.equals(customTypeInfo, that.customTypeInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reg, access, precision, allowUnorderedAccess, type, textureFormat, unknownInt, unknownByte, hasDefaultTexture, defaultTexture, hasCustomTypeInfo, customTypeInfo);
    }

    public static class CustomTypeInfo {
        public String name;
        public int size;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CustomTypeInfo that = (CustomTypeInfo) o;
            return size == that.size && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, size);
        }
    }
}