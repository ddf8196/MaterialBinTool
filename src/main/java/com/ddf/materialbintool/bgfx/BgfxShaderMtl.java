package com.ddf.materialbintool.bgfx;

import com.ddf.materialbintool.util.ByteBuf;

import java.util.ArrayList;

public class BgfxShaderMtl extends BgfxShader {
    private int[] numThreads;
    private short[] attrs;
    private short size;

    @Override
    public void read(ByteBuf buf) {
        magic = buf.readInt();
        hash = buf.readIntLE();

        short count = buf.readShortLE();
        uniforms = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Uniform uniform = new Uniform();
            uniform.readFrom(buf);
            uniforms.add(uniform);
        }

        if (magic == 0x43534803) { //Compute Shader
            numThreads = new int[3];
            for (int i = 0; i < 3; i++) {
                numThreads[i] = buf.readShortLE();
            }
        }

        code = buf.readByteArrayLE();
        buf.readByte(); //0

        if (buf.isReadable()) {
            int numAttrs = buf.readUnsignedByte();
            attrs = new short[numAttrs];
            for (int i = 0; i < numAttrs; i++) {
                attrs[i] = buf.readShortLE();
            }
            size = buf.readShortLE();
        }
    }

    @Override
    public void write(ByteBuf buf) {
        super.write(buf);
        if (attrs != null) {
            buf.writeByte(attrs.length);
            for (short attr : attrs) {
                buf.writeShortLE(attr);
            }
            buf.writeShortLE(size);
        }
    }

    public int[] getNumThreads() {
        return numThreads;
    }

    public void setNumThreads(int[] numThreads) {
        this.numThreads = numThreads;
    }

    public short[] getAttrs() {
        return attrs;
    }

    public short getSize() {
        return size;
    }

    public void setAttrs(short[] attrs) {
        this.attrs = attrs;
    }

    public void setSize(short size) {
        this.size = size;
    }
}
