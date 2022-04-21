package com.ddf.materialbintool.materials;

import com.ddf.materialbintool.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;

import java.util.Objects;

public class PlatformShaderStage {
    private String type;
    private String platform;
    private byte typeId; //Vertex 0  Fragment 1  Compute 2(?)  Unknown 3
    private byte platformId;

    public void readFrom(ByteBuf buf) {
        type = ByteBufUtil.readString(buf);
        platform = ByteBufUtil.readString(buf);
        typeId = buf.readByte();
        platformId = buf.readByte();
    }

    public void writeTo(ByteBuf buf) {
        ByteBufUtil.writeString(buf, type);
        ByteBufUtil.writeString(buf, platform);
        buf.writeByte(typeId);
        buf.writeByte(platformId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlatformShaderStage that = (PlatformShaderStage) o;
        return typeId == that.typeId && platformId == that.platformId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeId, platformId);
    }

    @Override
    public String toString() {
        return "PlatformShaderStage{" +
                "type='" + type + '\'' +
                ", platform='" + platform + '\'' +
                ", typeId=" + typeId +
                ", platformId=" + platformId +
                '}';
    }
}
