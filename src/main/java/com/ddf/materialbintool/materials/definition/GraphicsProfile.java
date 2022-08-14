package com.ddf.materialbintool.materials.definition;

public enum GraphicsProfile {
    HiDef,
    LowDef;

    public static GraphicsProfile get(int i) {
        return values()[i];
    }
}
