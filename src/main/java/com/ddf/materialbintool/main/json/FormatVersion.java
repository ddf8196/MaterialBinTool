package com.ddf.materialbintool.main.json;

public enum FormatVersion {
    V1;

    public static boolean contains(String name) {
        for (FormatVersion formatVersion : values()) {
            if (formatVersion.name().equals(name))
                return true;
        }
        return false;
    }
}
