package com.ddf.materialbintool.bgfx;

import com.ddf.materialbintool.bgfx.badger.BadgerUnknownData;
import com.ddf.materialbintool.util.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public abstract class BgfxShader {
    protected int magic;
    protected int hash;
    protected List<BadgerUnknownData> unknownData;
    protected List<Uniform> uniforms;
    protected transient byte[] code;

    public static BgfxShader create(String platform) {
        if (platform.startsWith("Direct3D"))
            return new BgfxShaderD3D();
        if (platform.startsWith("GLSL") || platform.startsWith("ESSL"))
            return new BgfxShaderGL();
        if (platform.startsWith("Metal"))
            return new BgfxShaderMtl();
        if (platform.startsWith("Vulkan"))
            return new BgfxShaderVK();

        throw new RuntimeException("Unsupported platform: " + platform);
    }

    public static Class<? extends BgfxShader> getClass(String platform) {
        if (platform.startsWith("Direct3D"))
            return BgfxShaderD3D.class;
        if (platform.startsWith("GLSL") || platform.startsWith("ESSL"))
            return BgfxShaderGL.class;
        if (platform.startsWith("Metal"))
            return BgfxShaderMtl.class;
        if (platform.startsWith("Vulkan"))
            return BgfxShaderVK.class;

        throw new RuntimeException("Unsupported platform: " + platform);
    }

    public void read(ByteBuf buf) {
        magic = buf.readInt();
        hash = buf.readIntLE();

        short unkCount = buf.readShortLE();
        unknownData = new ArrayList<>(unkCount);
        for (int i = 0; i < unkCount; i++) {
            BadgerUnknownData data = new BadgerUnknownData();
            data.read(buf);
            unknownData.add(data);
        }

        short count = buf.readShortLE();
        uniforms = new ArrayList<>(count);
        for (int j = 0; j < count; j++) {
            Uniform uniform = new Uniform();
            uniform.readFrom(buf);
            uniforms.add(uniform);
        }

        code = buf.readByteArrayLE();
        buf.readByte(); //0
    }

    public void write(ByteBuf buf) {
        buf.writeInt(magic);
        buf.writeIntLE(hash);

        buf.writeShortLE(unknownData.size());
        for (BadgerUnknownData data : unknownData) {
            data.write(buf);
        }

        buf.writeShortLE(uniforms.size());
        for (Uniform uniform : uniforms) {
            uniform.writeTo(buf);
        }

        buf.writeByteArrayLE(code);
        buf.writeByte(0);
    }

    public void read(byte[] array) {
        read(new ByteBuf(array));
    }

    public byte[] toByteArray() {
        ByteBuf buf = new ByteBuf();
        write(buf);
        return buf.toByteArray();
    }

    public int getMagic() {
        return magic;
    }

    public void setMagic(int magic) {
        this.magic = magic;
    }

    public int getHash() {
        return hash;
    }

    public void setHash(int hash) {
        this.hash = hash;
    }

    public List<Uniform> getUniforms() {
        return uniforms;
    }

    public void setUniforms(List<Uniform> uniforms) {
        this.uniforms = uniforms;
    }

    public byte[] getCode() {
        return code;
    }

    public void setCode(byte[] code) {
        this.code = code;
    }
}
