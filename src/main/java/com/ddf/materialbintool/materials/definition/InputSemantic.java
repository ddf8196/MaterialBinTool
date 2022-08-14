package com.ddf.materialbintool.materials.definition;

public enum InputSemantic {
    Position,
    Normal,
    Tangent,
    Bitangent,
    Color,
    Indices,
    Weight,
    TexCoord;

    public static InputSemantic get(int i) {
        return values()[i];
    }
}
