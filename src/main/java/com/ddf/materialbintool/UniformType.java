package com.ddf.materialbintool;

public enum UniformType {
    Sampler,
    End,
    Vec4,
    Mat3,
    Mat4;

    public static UniformType get(int type) {
        return values()[type];
    }
}
