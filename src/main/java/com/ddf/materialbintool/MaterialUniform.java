package com.ddf.materialbintool;

public class MaterialUniform {
    String name;
    //UniformType type;
    byte type;
    byte count;
    byte[] unknown4Bytes;

    public MaterialUniform(String name, byte type, byte count, byte[] unknown4Bytes) {
        this.name = name;
        this.type = type;
        this.count = count;
        this.unknown4Bytes = unknown4Bytes;
    }
}
