package com.ddf.materialbintool.materials;

public enum ShaderCodePlatform {
    Unknown,
    Direct3D_SM20_Level_9_1,
    Direct3D_SM20_Level_9_2,
    Direct3D_SM20_Level_9_3,
    Direct3D_SM30,
    Direct3D_SM40,
    Direct3D_SM50,
    Direct3D_SM60,
    GLSL_120,
    GLSL_430,
    ESSL_100,
    ESSL_300,
    ESSL_310,
    Metal,
    Vulkan,
    Nvn,
    PSSL;

    public static ShaderCodePlatform get(int i) {
        return values()[i];
    }
}
