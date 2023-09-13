# MaterialBinTool
RenderDragon .material.bin文件解包/打包/编译工具

[English README/英文自述文件](README.en-us.md)

## 使用
1. 安装Java8或更高版本
2. `java -jar MaterialBinTool.jar`

## 命令行参数
```
java -jar MaterialBinTool.jar [选项] <输入文件或目录>
所有选项:
  -u, --unpack       解包输入的.material.bin文件或输入目录中的全部.material.bin文件
  -a, --add-flags    将Variant的Flag以注释形式添加至输出的着色器文件前(仅ESSL和GLSL平台有效)
  --sort-variants    将Variant按照Flag重新排序
  --data-only        仅输出包含编译所需的必要数据的json, 不包含着色器(不可打包,仅可用于合并和编译)
  -r, --repack       打包输入目录或json文件为.material.bin文件
  --raw              输出或输入原始bgfx着色器文件而不是仅着色器代码
  -c, --compile      编译输入目录或json文件为.material.bin文件
  -t, --threads      指定编译时使用的线程数量, 默认为1(单线程)
  -s, --shaderc      指定shaderc可执行文件路径(不指定或指定的文件不存在或不可执行则尝试从PATH环境变量中查找)
  -i, --include      指定着色器编译时的额外包含目录
  --data             指定着色器编译时使用的json数据文件或包含json数据文件的目录
  --debug            开启调试信息
  -m, --merge-data   合并不同平台的json(仅支持`--data-only`输出的json, 输出目录需手动指定)
  -o, --output       指定输出目录(不指定则解包默认输出至.material.bin的同级目录,打包或编译默认输出至输入目录或输入json文件的同级目录)
  -h, --help         查看帮助
```

## 编译sc文件
目前支持的平台: ESSL(安卓), Direct3D(Win10), Metal(iOS), Vulkan(Nintendo Switch)
1. 执行`java -jar MaterialBinTool-all.jar -u material.bin文件路径`解包要编译的.material.bin文件
2. 在解包输出目录中创建src目录
3. 在src目录中放置着色器源文件,顶点着色器命名为`文件名.vertex.sc`, 片元着色器命名为`文件名.fragment.sc`, varyingDef文件命名为`文件名.varying.def`
4. (可选)在解包输出目录中创建defines.json并添加宏定义规则
5. (可选)将shaderc.exe所在的目录添加至PATH环境变量
6. 执行`java -jar MaterialBinTool-all.jar -c 解包输出目录`开始编译

## 默认宏定义规则
编译时会根据Pass名和Variant的Flag自动生成一些宏定义, 默认的宏定义添加和命名规则如下:
1. 当前Pass名转为大写下划线形式(例: `DepthOnlyOpaque -> DEPTH_ONLY_OPAQUE`, `Transparent -> TRANSPARENT`)
2. 值为On的Flag的键名转为大写下划线形式(例: `RenderAsBillboards=On -> RENDER_AS_BILLBOARDS`, `Seasons=On -> SEASONS`, `Seasons=Off -> 无`)

## defines.json
某些Flag的值不只有On和Off两种, 这时可以通过在解包输出目录中创建defines.json来添加宏定义规则   
defines.json格式如下:
```json
{
    "pass": {
        "Pass名": ["宏名称"]
    },
    "flag": {
        "键名": {
            "值": ["宏名称"]
        }
    }
}
```
注意: 在defines.json中有声明的Pass和Flag不会再按照默认宏定义规则定义宏

## 编译示例
以编译RenderChunk.material.bin为例:
1. 执行`java -jar MaterialBinTool-all.jar --unpack --data-only RenderChunk.material.bin`   
执行完后的目录结构应该是这样:
```
RenderChunk/
    RenderChunk.json
```
2. 在`RenderChunk`目录中创建`src`目录, 并放置`RenderChunk.vertex.sc`、`RenderChunk.fragment.sc`、`RenderChunk.varying.def.sc`及`bgfx_shader.sh`等头文件(可选)
3. 因为RenderChunk.material.bin中的Flag的值都只有On/Off两种, 故无需创建`defines.json`以额外添加宏定义规则(其他文件同理视情况创建)   
此时目录结构应为:
```
RenderChunk/
    src/
        bgfx_shader.sh(可选)
        RenderChunk.vertex.sc
        RenderChunk.fragment.sc
        RenderChunk.varying.def.sc
    RenderChunk.json
```
4. 执行`java -jar MaterialBinTool-all.jar -s shaderc.exe路径(可选) -i 包含bgfx_shader.sh的目录路径(可选) -c RenderChunk目录路径`开始编译   
若已将shaderc.exe所在目录添加至PATH环境变量,则可不指定`-s`参数   
若已将bgfx_shader.sh复制至src目录,则可不指定`-i`参数
5. 执行完成后会在`RenderChunk`目录下生成编译出的`RenderChunk.material.bin`,替换安装包里的对应文件即可使用   
   
注意: 目前编译出的文件仍然不是全平台通用, 解包的是哪个平台的编译完以后的文件就只能在哪个平台上使用   
若要编译同时支持多个平台的文件，可使用`--merge-data`选项将多个平台的json合并

## 关于sc文件
sc是bgfx的基于GLSL的跨平台着色器(`bgfx's shaderc flavor of GLSL`), 可通过`shaderc`编译为各个平台的着色器   
sc的大多数语法GLSL相同, 但也有部分区别, 编写时bgfx有修改的部分需要按照bgfx的标准, 其余部分需要严格遵守GLSL规范, 具体区别可在bgfx的文档查看: [shader-compiler-shaderc](https://bkaradzic.github.io/bgfx/tools.html#shader-compiler-shaderc)

## sc着色器源文件获取
可在[RenderDragonSourceCodeInv](https://github.com/SurvivalApparatusCommunication/RenderDragonSourceCodeInv) 仓库中获取部分已经整理好的sc源文件
其他源文件(RTX相关的除外)可根据安卓版的.material.bin解包出的glsl自行手动整理   
