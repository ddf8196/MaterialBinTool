package com.ddf.materialbintool.materials.definition;

public enum ShaderInputType {
    Float,
    Vec2,
    Vec3,
    Vec4,
    Int,
    Int2,
    Int3,
    Int4,
    UInt,
    UInt2,
    UInt3,
    UInt4,
    Mat4;

    public static ShaderInputType get(int type) {
        return values()[type];
    }
}
