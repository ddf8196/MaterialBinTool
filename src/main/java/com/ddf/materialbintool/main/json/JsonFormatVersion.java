package com.ddf.materialbintool.main.json;

public enum JsonFormatVersion {
    //MaterialBinFormatVersion.V1_19_60_20
    V1,

    //MaterialBinFormatVersion.V1_20_80_21
    V2,

    //MaterialBinFormatVersion.V1_21_20_22
    V3;

    public static boolean contains(String name) {
        for (JsonFormatVersion jsonFormatVersion : values()) {
            if (jsonFormatVersion.name().equals(name))
                return true;
        }
        return false;
    }
}
