package com.ddf.materialbintool.materials.definition;

public enum BlendMode {
    None,
    Replace,
    AlphaBlend,
    ColorBlendAlphaAdd,
    PreMultiplied,
    InvertColor,
    Additive,
    AdditiveAlpha,
    Multiply,
    MultiplyBoth,
    InverseSrcAlpha,
    SrcAlpha;

    public static BlendMode get(int i) {
        return values()[i];
    }
}
