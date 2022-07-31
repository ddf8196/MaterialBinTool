package com.ddf.materialbintool.materials.definition;

public enum ShaderStage {
    Vertex,
    Fragment,
    Compute,
    Unknown;

    public static ShaderStage get(int i) {
        return values()[i];
    }
}
