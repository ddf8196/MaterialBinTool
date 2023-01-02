# MaterialBinTool
RenderDragon .material.bin unpacking/packaging/compilation tool

[Chinese README/中文自述文件](README.md)

## Usage
1. Install Java 8 or later
2. Unpack: `java -jar MaterialBinTool-0.5.1-all.jar -u "material.bin file"`
3. Pack: `java -jar MaterialBinTool-0.5.1-all.jar -r "path to unpacked directory or json file"`
4. Compile: `java -jar MaterialBinTool-0.5.1-all.jar -c "path to unpacked directory or json file"`

## Command line arguments
```
java -jar MaterialBinTool-0.5.1-all.jar [Option] <Enter a file or directory>
All options:
    -u, --unpack:           Unpacks the entered .material.bin file or all .material.bin files in the input directory
    -a, --add-flagmodes:    Adds Variant's FlagMode as an annotation to the output shader file (ESSL and GLSL platforms only)
    -r, --repack:           Packages the input directory or json file as a .material.bin file
    --raw:                  Output/Input raw bgfx shader files instead of shader-only code
    -c, --compile:          Compiles the input directory or json file as a .material.bin file
    -h, --help:             Displays help
    -s, --shaderc:          Specifies the shaderc executable path (try to find it from the PATH environment variable if the specified file is not specified or does not exist/is not executable)
    -i, --include:          Specifies an extra include directory at shader compile time
    -o, --output:           Specifies the output directory (if not specified, the default output is unpacked to the sibling directory of .material.bin, and the packaging/compilation output is output to the input directory or the sibling directory of the input json file)
```

## Compiling .sc files
Currently supported platforms: ESSL (Android), Direct3D (Win10), Metal (iOS)
1. Unpack the .material.bin file to be compiled `java -jar MaterialBinTool-0.5.1-all.jar -u "material.bin file"`
2. Create a `src` directory in the unpacked output directory
3. Place the shader source files in the `src` directory, named the vertex shader, fragment shader, and varyingDef file `filename.vertex.sc, filename.fragment.sc, filename.varying.def.sc`
4. Optionally, create `defines.json` in the unpacked output directory and add macro definition rules
5. Optionally, add the directory where `shaderc.exe` is located to the PATH environment variable
6. Compile the output directory `java -jar MaterialBinTool-0.5.1-all.jar -c "output directory"`

## The default macro defines the rule
At compile time, some macro definitions will be automatically generated based on the Pass name and Variant's FlagMode, and the default macro definition addition and naming rules are as follows:

1. The current Pass name is underscored in uppercase (Example: `DepthOnlyOpaque -> DEPTH_ONLY_OPAQUE`, `Transparent -> TRANSPARENT`)
2. The key name of FlagMode with the value On is converted to uppercase (Example: `RenderAsBillboards=On -> RENDER_AS_BILLBOARDS`, `Seasons=On -> SEASONS`, `Seasons=Off -> (nothing)`)

## defines.json
Some FlagModes have more than just On and Off values, so you can add macro definition rules to these FlagModes by creating `defines.json` in the unpacked output directory.
The format for `defines.json` is as follows:
```json
{
    "passes": {
        "PassName1": ["Macro_Name_1", "Macro_Name_2"],
        "PassName2": ["Macro_Name_1", "Macro_Name_2", "Macro_Name_3"]
    },
    "flagModes": {
        "KeyName1": {
            "Value1": ["Macro_Name_1", "Macro_Name_2"],
            "Value2": ["Macro_Name_1", "Macro_Name_2", "Macro_Name_3"]
        },
        "KeyName2": {
            "Value1": ["Macro_Name_1", "Macro_Name_2"],
            "Value2": ["Macro_Name_1", "Macro_Name_2", "Macro_Name_3"],
            "value3": ["Macro_Name_1"] 
        }
    } 
}
```

Note: Pass and FlagMode declared in `defines.json` no longer define macros according to the default macro definition rules

## Compile the sample
Let's compile RenderChunk.material.bin as an example:

1. Execute: `java -jar MaterialBinTool-0.5.1-all.jar -u RenderChunk.material.bin文件路径`.
The directory structure after execution should look like this.
```
RenderChunk/
    AlphaTest/
        0~5.Platform_name.Shader_type.Shader_language
        AlphaTest.json
    DepthOnly/
        0~5.Platform_name.Shader_type.Shader_language
        DepthOnly.json
    DepthOnlyOpaque/
        0~5.Platform_name.Shader_type.Shader_language
        DepthOnlyOpaque.json
    Opaque/
        0~5.Platform_name.Shader_type.Shader_language
        Opaque.json
    Transparent/
        0~5.Platform_name.Shader_type.Shader_language
        Transparent.json
    RenderChunk.json
```
2. Create the `src` directory in the `RenderChunk` directory and place in it `RenderChunk.vertex.sc`, `RenderChunk.fragment.sc`, `RenderChunk.varying.def.sc`, and, optionally, `bgfx_shader.sh`.
3. Because the values of FlagMode in `RenderChunk.material.bin` are only On/Off, there is no need to create additional macro definition rules (other files are created as appropriate).
At this point, the directory structure should be:
```
RenderChunk/
    AlphaTest/
        0~5.Platform_name.Shader_type.Shader_language
        AlphaTest.json
    DepthOnly/
        0~5.Platform_name.Shader_type.Shader_language
        DepthOnly.json
    DepthOnlyOpaque/
        0~5.Platform_name.Shader_type.Shader_language
        DepthOnlyOpaque.json
    Opaque/
        0~5.Platform_name.Shader_type.Shader_language
        Opaque.json
    Transparent/
        0~5.Platform_name.Shader_type.Shader_language
        Transparent.json
    src/
        bgfx_shader.sh (optional)
        RenderChunk.vertex.sh
        RenderChunk.varying.def.sh
        RenderChunk.fragment.sh
    RenderChunk.json
```
4. Execute `java -jar MaterialBinTool-0.5.1-all.jar -s "shaderc.exe path" (optional) -i "directory path containing bgfx_shader.sh" (optional) -c "RenderChunk directory path"` which will start compiling the shaders.
If the directory where `shaderc.exe` is located has been added to the PATH environment variable, you can leave the `-s` parameter
unspecified. If the `bgfx_shader.sh` has been copied to the src directory, you can leave the `-i` parameter unspecified too.
5. After the execution is completed, the compiled "RenderChunk.material.bin" will be generated in the "RenderChunk" directory, and the corresponding files in the installation package can be replaced.

Note: At present, the compiled files are still not common to all platforms, and the unpacked platform can only be used on which platform the compiled files came from.

## About the SC language
sc is BGFX's GLSL-based cross-platform shader language, which can be compiled into shaders for various platforms by "shaderc" sc.
Most of the syntax GLSL is the same, but there are some differences, and it needs to be written according to BGFX standards, the specific differences can be found in the bgfx documentation: [shader-compiler-shaderc](https://bkaradzic.github.io/bgfx/tools.html#shader-compiler-shaderc)

## sc source files
Some sc source files that have been sorted out by [OEOTYAN](https://github.com/OEOTYAN/) can be obtained in the [RenderDragonSorceCodeInv](https://github.com/OEOTYAN/RenderDragonSorceCodeInv) repository. Other source files
(except RTX-related) can be manually organized, according to the .material.bin unpacked glsl in the Android version.
"bgfx_shader.sh" is available in the bgfx repository (the older version of bgfx used by the render dragon, there is no guarantee that the source code in the latest version of the repository is available, here is a version available: [bgfx_shader.sh](https://github.com/bkaradzic/bgfx/blob/1ba107d156d1d28e86550df5d586ea259aec1020/src/bgfx_shader.sh))