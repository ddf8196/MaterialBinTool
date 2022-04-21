package com.ddf.materialbintool.materials.definition;

public enum ShaderInputType {
    Float,
    Vec2,
    Vec3,
    Vec4,
    Unknown1;

    public static ShaderInputType get(int type) {
        return values()[type];
    }
}
