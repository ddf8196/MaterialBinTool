package com.ddf.materialbintool.materials.definition;

import io.netty.buffer.ByteBuf;

public class ShaderInput {
    public Type type;
    public Attribute attribute;
    public boolean isInstanceData;

    public boolean hasPrecisionConstraint;
    public PrecisionConstraint precisionConstraint;

    public boolean hasInterpolationConstraint;
    public InterpolationConstraint interpolationConstraint;

    public ShaderInput() {
    }

    public void read(ByteBuf buf) {
        type = Type.get(buf.readByte());
        attribute = Attribute.get(buf.readByte(), buf.readByte());
        isInstanceData = buf.readBoolean();

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
        buf.writeByte(attribute.index);
        buf.writeByte(attribute.subIndex);
        buf.writeBoolean(isInstanceData);

        buf.writeBoolean(hasPrecisionConstraint);
        if (hasPrecisionConstraint) {
            buf.writeByte(precisionConstraint.ordinal());
        }

        buf.writeBoolean(hasInterpolationConstraint);
        if (hasInterpolationConstraint) {
            buf.writeByte(interpolationConstraint.ordinal());
        }
    }

    public enum Type {
        Float,
        Vec2,
        Vec3,
        Vec4,
        Unknown1;

        public static Type get(int type) {
            return Type.values()[type];
        }
    }

    public enum Attribute {
        Position(0, 0),
        Normal(1, 0),
        Tangent(2, 0),
        Bitangent(3, 0),
        Color0(4, 0),
        Color1(4, 1),
        Color2(4, 2),
        Color3(4, 3),
        Indices(5, 0),
        Weights(6, 0),
        TexCoord0(7, 0),
        TexCoord1(7, 1),
        TexCoord2(7, 2),
        TexCoord3(7, 3),
        TexCoord4(7, 4),
        TexCoord5(7, 5),
        TexCoord6(7, 6),
        TexCoord7(7, 7),
        TexCoord8(7, 8);

        public byte index;
        public byte subIndex;

        Attribute(int index, int subIndex) {
            this.index = (byte) index;
            this.subIndex = (byte) subIndex;
        }

        public static Attribute get(int index, int subIndex) {
            for (Attribute attribute : values()) {
                if (attribute.index == index && attribute.subIndex == subIndex) {
                    return attribute;
                }
            }
            throw new RuntimeException("Unknown Attribute: index=" + index + ",subIndex=" + subIndex);
        }
    }
}
