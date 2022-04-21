package com.ddf.materialbintool.definition;

import io.netty.buffer.ByteBuf;

public class ShaderInput {
    public ShaderInputType type;
    public byte unknownByte0;
    public byte unknownByte1;
    public boolean unknownBool1;

    public boolean hasPrecisionConstraint;
    public byte precisionConstraint; //dragon::materials::definition::PrecisionConstraint

    public boolean hasInterpolationConstraint;
    public byte interpolationConstraint; //dragon::materials::definition::InterpolationConstraint

    public ShaderInput() {
    }

    public void readFrom(ByteBuf buf) {
        type = ShaderInputType.get(buf.readByte());
        unknownByte0 = buf.readByte();
        unknownByte1 = buf.readByte();
        unknownBool1 = buf.readBoolean();

        hasPrecisionConstraint = buf.readBoolean();
        if (hasPrecisionConstraint) {
            precisionConstraint = buf.readByte();
        }

        hasInterpolationConstraint = buf.readBoolean();
        if (hasInterpolationConstraint) {
            interpolationConstraint = buf.readByte();
        }
    }

    public void writeTo(ByteBuf buf) {
        buf.writeByte(type.ordinal());
        buf.writeByte(unknownByte0);
        buf.writeByte(unknownByte1);
        buf.writeBoolean(unknownBool1);


        buf.writeBoolean(hasPrecisionConstraint);
        if (hasPrecisionConstraint) {
            buf.writeByte(precisionConstraint);
        }

        buf.writeBoolean(hasInterpolationConstraint);
        if (hasInterpolationConstraint) {
            buf.writeByte(interpolationConstraint);
        }
    }
}
