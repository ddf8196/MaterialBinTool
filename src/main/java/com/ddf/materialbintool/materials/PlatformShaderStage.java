package com.ddf.materialbintool.materials;

import com.ddf.materialbintool.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;

import java.util.Objects;

public class PlatformShaderStage {
    public String typeName;
    public String platformName;
    public ShaderCodeType type;
    public ShaderCodePlatform platform;

    public void read(ByteBuf buf) {
        typeName = ByteBufUtil.readString(buf);
        platformName = ByteBufUtil.readString(buf);
        type = ShaderCodeType.get(buf.readByte());
        platform = ShaderCodePlatform.get(buf.readByte());
    }

    public void write(ByteBuf buf) {
        ByteBufUtil.writeString(buf, typeName);
        ByteBufUtil.writeString(buf, platformName);
        buf.writeByte(type.ordinal());
        buf.writeByte(platform.ordinal());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlatformShaderStage that = (PlatformShaderStage) o;
        return type == that.type && platform == that.platform;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, platform);
    }

    @Override
    public String toString() {
        return "PlatformShaderStage{" +
                "typeName='" + typeName + '\'' +
                ", platformName='" + platformName + '\'' +
                ", type=" + type +
                ", platform=" + platform +
                '}';
    }
}
