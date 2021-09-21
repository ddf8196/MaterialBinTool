package com.ddf.materialbintool;

public class ShaderInput {
    String name;
    ShaderInputType type;
    byte[] unknown4Bytes;
    byte unknownByte1;
    byte unknownByte2;

    public ShaderInput(String name, ShaderInputType type, byte[] unknown4Bytes, byte unknownByte1, byte unknownByte2) {
        this.name = name;
        this.type = type;
        this.unknown4Bytes = unknown4Bytes;
        this.unknownByte1 = unknownByte1;
        this.unknownByte2 = unknownByte2;
    }
}
