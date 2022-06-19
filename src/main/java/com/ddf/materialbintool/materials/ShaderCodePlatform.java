package com.ddf.materialbintool.materials;

public enum ShaderCodePlatform {
    Direct3D_SM40, //Windows
    Direct3D_SM50, //Windows
    Direct3D_SM60, //Windows
    Direct3D_SM65, //Windows
    Direct3D_XB1,  //?
    Direct3D_XBX,  //?
    GLSL_120,      //?
    GLSL_430,      //?
    ESSL_100,      //Android
    ESSL_300,      //?
    ESSL_310,      //?
    Metal,         //iOS
    Vulkan,        //Nintendo Switch
    Nvn,           //?
    Pssl;          //?

    public static ShaderCodePlatform get(int i) {
        return values()[i];
    }
}
