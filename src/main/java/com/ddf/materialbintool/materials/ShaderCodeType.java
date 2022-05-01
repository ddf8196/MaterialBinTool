package com.ddf.materialbintool.materials;

public enum ShaderCodeType {
    Vertex,
    Fragment,
    Compute,
    Unknown;

    public static ShaderCodeType get(int i) {
        return values()[i];
    }
}
