package com.ddf.materialbintool.materials.definition;

import java.util.HashMap;
import java.util.Map;

public enum BlendMode {
    None,
    Replace,
    AlphaBlend,
    Unknown_3,
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

    public static Map<BlendMode, String> BlendModeStringMapping = new HashMap<>();

    static {
        BlendModeStringMapping.put(None, "None");
        BlendModeStringMapping.put(Replace, "Replace");
        BlendModeStringMapping.put(AlphaBlend, "AlphaBlend");

        BlendModeStringMapping.put(PreMultiplied, "PreMultiplied");
        BlendModeStringMapping.put(InvertColor, "InvertColor");
        BlendModeStringMapping.put(Additive, "Additive");
        BlendModeStringMapping.put(AdditiveAlpha, "AdditiveAlpha");
        BlendModeStringMapping.put(Multiply, "Multiply");
        BlendModeStringMapping.put(MultiplyBoth, "MultiplyBoth");
        BlendModeStringMapping.put(InverseSrcAlpha, "InverseSrcAlpha");
        BlendModeStringMapping.put(SrcAlpha, "SrcAlpha");
    }
}
