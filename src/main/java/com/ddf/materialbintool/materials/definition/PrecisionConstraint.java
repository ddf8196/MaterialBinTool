package com.ddf.materialbintool.materials.definition;

public enum PrecisionConstraint {
    LOWP,
    MEDIUMP,
    HIGHP;

    public static PrecisionConstraint get(int i) {
        return values()[i];
    }
}
