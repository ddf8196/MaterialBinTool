package com.ddf.materialbintool.materials.definition;

public enum SamplerType {
    Type2D,
    Type2DArray,
    Type2DExternal,
    Type3D,
    TypeCube,
    TypeStructuredBuffer,
    TypeRawBuffer,
    TypeAccelerationStructure,
    Type2DShadow,
    Type2DArrayShadow;

    public static SamplerType get(int i) {
        return values()[i];
    }
}
