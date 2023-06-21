# MaterialBinTool
RenderDragon .material.bin unpacking/packaging/compilation tool

[Chinese README/中文自述文件](README.md)

## Usage
1. Install Java 8 or later
2. Unpack: `java -jar MaterialBinTool-all.jar -u "material.bin file"`
3. Pack: `java -jar MaterialBinTool-all.jar -r "path to unpacked directory or json file"`
4. Compile: `java -jar MaterialBinTool-all.jar -c "path to unpacked directory or json file"`

## Command line arguments
```
java -jar MaterialBinTool-all.jar [Option] <Enter a file or directory>
All options:
    -u, --unpack:           Unpacks the entered .material.bin file or all .material.bin files in the input directory
    -a, --add-flags:        Adds Variant's Flags as an annotation to the output shader file (ESSL and GLSL platforms only)
    --sort-variants         Sort the Variants by its Flag
    --data-only             Output only json with necessary data for compilation, without shaders (not repackable, only available for merging and compilation)
    -r, --repack:           Packages the input directory or json file as a .material.bin file
    --raw:                  Output/Input raw bgfx shader files instead of shader-only code
    -c, --compile:          Compiles the input directory or json file as a .material.bin file
    -t, --threads           Specify the number of threads used at compile time, default is 1 (single thread)
    -s, --shaderc:          Specifies the shaderc executable path (try to find it from the PATH environment variable if the specified file is not specified or does not exist/is not executable)
    -i, --include:          Specifies an extra include directory at shader compile time
    --data                  Specify the json data file used by the shader when compiling or the directory containing the json data file
    --debug                 Enables debug information
    -m, --merge-data        Merge json from different platforms (only `-data-only` output json is supported, output directory should be specified manually)
    -o, --output:           Specifies the output directory (if not specified, the default output is unpacked to the sibling directory of .material.bin, and the packaging/compilation output is output to the input directory or the sibling directory of the input json file)
    -h, --help:             Displays help
```

## Compiling .sc files
Currently supported platforms: ESSL (Android), Direct3D (Win10), Metal (iOS), Vulkan(Nintendo Switch)
1. Unpack the .material.bin file to be compiled `java -jar MaterialBinTool-all.jar -u "material.bin file"`
2. Create a `src` directory in the unpacked output directory
3. Place the shader source files in the `src` directory, named the vertex shader, fragment shader, and varyingDef file `filename.vertex.sc, filename.fragment.sc, filename.varying.def.sc`
4. Optionally, create `defines.json` in the unpacked output directory and add macro definition rules
5. Optionally, add the directory where `shaderc.exe` is located to the PATH environment variable
6. Compile the output directory `java -jar MaterialBinTool-all.jar -c "output directory"`

## The default macro definition rules
At compile time, some macro definitions will be automatically generated based on the Pass name and Variant's Flags, and the default macro definition addition and naming rules are as follows:

1. The current Pass name is underscored in uppercase (Example: `DepthOnlyOpaque -> DEPTH_ONLY_OPAQUE`, `Transparent -> TRANSPARENT`)
2. The key name of Flag with the value On is converted to uppercase (Example: `RenderAsBillboards=On -> RENDER_AS_BILLBOARDS`, `Seasons=On -> SEASONS`, `Seasons=Off -> (nothing)`)

## defines.json
Some Flag have more than just On and Off values, so you can add macro definition rules to these Flags by creating `defines.json` in the unpacked output directory.
The format for `defines.json` is as follows:
```json
{
    "pass": {
        "PassName1": ["Macro_Name_1", "Macro_Name_2"],
        "PassName2": ["Macro_Name_1", "Macro_Name_2", "Macro_Name_3"]
    },
    "flag": {
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

Note: Pass and Flag declared in `defines.json` no longer define macros according to the default macro definition rules

## Compile the sample
Let's compile RenderChunk.material.bin as an example:

1. Execute: `java -jar MaterialBinTool-all.jar -u --data-only RenderChunk.material.bin`.
The directory structure after execution should look like this.
```
RenderChunk/
    RenderChunk.json
```
2. Create the `src` directory in the `RenderChunk` directory and place in it `RenderChunk.vertex.sc`, `RenderChunk.fragment.sc`, `RenderChunk.varying.def.sc`, and, optionally, `bgfx_shader.sh`.
3. Because the values of Flags in `RenderChunk.material.bin` are only On/Off, there is no need to create additional macro definition rules (other files are created as appropriate).
At this point, the directory structure should be:
```
RenderChunk/
    src/
        bgfx_shader.sh (optional)
        RenderChunk.vertex.sh
        RenderChunk.varying.def.sh
        RenderChunk.fragment.sh
    RenderChunk.json
```
4. Execute `java -jar MaterialBinTool-all.jar -s "shaderc.exe path" (optional) -i "directory path containing bgfx_shader.sh" (optional) -c "RenderChunk directory path"` which will start compiling the shaders.
If the directory where `shaderc.exe` is located has been added to the PATH environment variable, you can leave the `-s` parameter
unspecified. If the `bgfx_shader.sh` has been copied to the src directory, you can leave the `-i` parameter unspecified too.
5. After the execution is completed, the compiled "RenderChunk.material.bin" will be generated in the "RenderChunk" directory, and the corresponding files in the installation package can be replaced.

Note: At present, the compiled files are still not common to all platforms, and the unpacked platform can only be used on which platform the compiled files came from.   
To compile files that support multiple platforms, use the `-merge-data` option to merge json from multiple platforms.

## About the SC language
sc is BGFX's GLSL-based cross-platform shader language(`bgfx's shaderc flavor of GLSL`), which can be compiled into shaders for various platforms by "shaderc" sc.
Most of the syntax GLSL is the same, but there are some differences, and it needs to be written according to BGFX standards, the specific differences can be found in the bgfx documentation: [shader-compiler-shaderc](https://bkaradzic.github.io/bgfx/tools.html#shader-compiler-shaderc)

## sc source files
Some sc source files that have been sorted out can be obtained in the [RenderDragonSourceCodeInv](https://github.com/SurvivalApparatusCommunication/RenderDragonSourceCodeInv) repository.   
Other source files (except RTX-related) can be manually organized, according to the .material.bin unpacked glsl in the Android version.
