package com.ddf.materialbintool.bgfx;

import io.netty.buffer.ByteBuf;

public abstract class BgfxShader {
    public abstract void readFrom(ByteBuf buf);
    public abstract void writeTo(ByteBuf buf);
    public abstract byte[] getCode();

    public static BgfxShader create(String platform) {
        if (platform.startsWith("Direct3D"))
            return new BgfxShaderD3D();
        if (platform.startsWith("GLSL") || platform.startsWith("ESSL"))
            return new BgfxShaderGL();

        throw new RuntimeException("Unsupported platform: " + platform);
    }
}
