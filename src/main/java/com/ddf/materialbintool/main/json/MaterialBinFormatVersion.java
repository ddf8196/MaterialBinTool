package com.ddf.materialbintool.main.json;

public enum MaterialBinFormatVersion {
    //1.16.100.54 beta - 1.16.230.56 beta
    //1.16.200.02 - 1.16.221.01
    //CompiledMaterialDefinition version 21
    V1_16_100_54,

    //1.17.0.50 beta - 1.17.30.25 beta
    //1.17.0.02 - 1.17.34.02
    //CompiledMaterialDefinition version 22
    //Added CompiledMaterialDefinition.encryptionVariant
    //Updated CompiledMaterialDefinition version to 22
    V1_17_0_50,

    //1.17.40.20 beta - 1.17.40.23 beta
    //1.17.40.06 - 1.17.41.01
    //CompiledMaterialDefinition version 22
    //Removed CompiledMaterialDefinition.Pass.graphicsMode
    //Added CompiledMaterialDefinition.Pass.platformSupport
    V1_17_40_20,

    //1.18.0.20 beta - 1.19.50.25 beta
    //1.18.0.02 - 1.19.51.01
    //CompiledMaterialDefinition version 22
    //Added SamplerDefinition.unknownInt (which seems always to be 1)
    V1_18_0_20,

    //1.19.60.20 preview - 1.20.80.20 preview
    //1.19.60.03 - 1.20.73.01
    //CompiledMaterialDefinition version 22
    //Added SamplerDefinition.unknownByte (which seems always to be the same as SamplerDefinition.reg)
    V1_19_60_20,

    //1.20.80.21 preview - 1.21.20.21 preview
    //1.20.80.05 - 1.21.2.02
    //CompiledMaterialDefinition version 22
    //Added SamplerDefinition.hasUnknownString and SamplerDefinition.unknownString
    V1_20_80_21,

    //1.21.20.22 preview - current preview
    //1.21.20.03 - current release
    //CompiledMaterialDefinition version 22
    //Added SamplerDefinition.hasUnknownByte2 and SamplerDefinition.unknownByte2
    V1_21_20_22
}
