package com.ddf.materialbintool.materials.definition;

public enum SamplerAccess {
    None,
    Read,
    Write,
    ReadWrite;

    public static SamplerAccess get(int i) {
        return values()[i];
    }
}
