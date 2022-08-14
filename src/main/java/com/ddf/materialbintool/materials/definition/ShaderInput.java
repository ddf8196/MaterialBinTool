package com.ddf.materialbintool.materials.definition;

import com.ddf.materialbintool.util.ByteBuf;

public class ShaderInput {
    public ShaderInputType type;
    public SemanticDescription semantic;
    public boolean isPerInstance;

    public boolean hasPrecisionConstraint;
    public PrecisionConstraint precisionConstraint;

    public boolean hasInterpolationConstraint;
    public InterpolationConstraint interpolationConstraint;

    public ShaderInput() {
    }

    public void read(ByteBuf buf) {
        type = ShaderInputType.get(buf.readByte());
        semantic = new SemanticDescription();
        semantic.read(buf);
        isPerInstance = buf.readBoolean();

        hasPrecisionConstraint = buf.readBoolean();
        if (hasPrecisionConstraint) {
            precisionConstraint = PrecisionConstraint.get(buf.readUnsignedByte());
        }

        hasInterpolationConstraint = buf.readBoolean();
        if (hasInterpolationConstraint) {
            interpolationConstraint = InterpolationConstraint.get(buf.readUnsignedByte());
        }
    }

    public void write(ByteBuf buf) {
        buf.writeByte(type.ordinal());
        semantic.write(buf);
        buf.writeBoolean(isPerInstance);

        buf.writeBoolean(hasPrecisionConstraint);
        if (hasPrecisionConstraint) {
            buf.writeByte(precisionConstraint.ordinal());
        }

        buf.writeBoolean(hasInterpolationConstraint);
        if (hasInterpolationConstraint) {
            buf.writeByte(interpolationConstraint.ordinal());
        }
    }

    @Override
    public String toString() {
        return "ShaderInput{" +
                "type=" + type +
                ", semantic=" + semantic +
                ", isPerInstance=" + isPerInstance +
                ", precisionConstraint=" + precisionConstraint +
                ", interpolationConstraint=" + interpolationConstraint +
                '}';
    }
}
