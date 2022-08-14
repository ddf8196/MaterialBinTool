package com.ddf.materialbintool.materials;

import com.ddf.materialbintool.materials.definition.ShaderStage;
import com.ddf.materialbintool.util.ByteBuf;

import java.util.Objects;

public class PlatformShaderStage {
    public String stageName;
    public String platformName;
    public ShaderStage stage;
    public ShaderCodePlatform platform;

    public PlatformShaderStage() {}

    public PlatformShaderStage(ShaderStage stage, ShaderCodePlatform platform) {
        this.stage = stage;
        this.platform = platform;
        this.stageName = stage.name();
        this.platformName = platform.name();
    }

    public void read(ByteBuf buf) {
        stageName = buf.readStringLE();
        platformName = buf.readStringLE();
        stage = ShaderStage.get(buf.readByte());
        platform = ShaderCodePlatform.get(buf.readByte());
    }

    public void write(ByteBuf buf) {
        buf.writeStringLE(stageName);
        buf.writeStringLE(platformName);
        buf.writeByte(stage.ordinal());
        buf.writeByte(platform.ordinal());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlatformShaderStage that = (PlatformShaderStage) o;
        return stage == that.stage && platform == that.platform;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stage, platform);
    }

    @Override
    public String toString() {
        return "PlatformShaderStage{" +
                "typeName='" + stageName + '\'' +
                ", platformName='" + platformName + '\'' +
                ", type=" + stage +
                ", platform=" + platform +
                '}';
    }
}
