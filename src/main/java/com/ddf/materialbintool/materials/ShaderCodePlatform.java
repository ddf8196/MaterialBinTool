package com.ddf.materialbintool.materials;

public enum ShaderCodePlatform {
    Direct3D_SM40,
    Direct3D_SM50,
    Direct3D_SM60,
    Direct3D_SM65,
    Direct3D_XB1,
    Direct3D_XBX,
    GLSL_120,
    GLSL_430,
    ESSL_100,
    ESSL_300,
    ESSL_310,
    Metal,
    Vulkan,
    Nvn,
    Pssl;

    public static ShaderCodePlatform get(int i) {
        return values()[i];
    }
}
