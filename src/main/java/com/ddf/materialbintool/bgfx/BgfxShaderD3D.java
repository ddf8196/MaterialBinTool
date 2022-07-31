package com.ddf.materialbintool.bgfx;

import com.ddf.materialbintool.util.ByteBuf;

public class BgfxShaderD3D extends BgfxShader {
    private short[] attrs;
    private short size;

    @Override
    public void read(ByteBuf buf) {
        super.read(buf);
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
