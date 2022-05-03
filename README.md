# MaterialBinTool
RenderDragon .material.bin文件解包/打包/编译工具

## 使用
1. 安装Java8或更高版本
2. 解包: `java -jar MaterialBinTool-0.5.0-all.jar -u material.bin文件路径`   
3. 打包: `java -jar MaterialBinTool-0.5.0-all.jar -r 解包输出的目录或json文件的路径`
4. 编译: `java -jar MaterialBinTool-0.5.0-all.jar -c 输入目录或json文件的路径`

## 命令行参数
```
java -jar MaterialBinTool-0.5.0-all.jar [选项] <输入文件或目录>
所有选项:
  -u, --unpack        解包输入的.material.bin文件或输入目录中的全部.material.bin文件
  -a, --add-flagmodes 将Variant的FlagMode以注释形式添加至输出的着色器文件前(仅ESSL和GLSL平台有效)
  -r, --repack        打包输入目录或json文件为.material.bin文件
  --raw               输出/输入原始bgfx着色器文件而不是仅着色器代码
  -c, --compile       编译输入目录或json文件为.material.bin文件
  -h, --help          查看帮助
  -s, --shaderc       指定shaderc可执行文件路径(不指定或指定的文件不存在/不可执行则尝试从PATH环境变量中查找)
  -i, --include       指定着色器编译时的额外包含目录
  -o, --output        指定输出目录(不指定则解包默认输出至.material.bin的同级目录,打包/编译默认输出至输入目录或输入json文件的同级目录)
```

## 编译sc文件
1. 执行`java -jar MaterialBinTool-0.5.0-all.jar -u material.bin文件路径`解包要编译的.material.bin文件
2. 在解包输出目录中创建src目录
3. 在src目录中放置着色器源文件,顶点着色器命名为`文件名.vertex.sc`, 片元着色器命名为`文件名.fragment.sc`, varyingDef文件命名为`文件名.varying.def`
4. (可选)在解包输出目录中创建defines.json并添加宏定义规则
5. (可选)将shaderc.exe所在的目录添加至PATH环境变量
6. 执行`java -jar MaterialBinTool-0.5.0-all.jar -c 解包输出目录`开始编译

## 默认宏定义规则
编译时会根据Pass名和Variant的FlagMode自动生成一些宏定义, 默认的宏定义添加和命名规则如下:
1. 当前Pass名转为大写下划线形式(例: `DepthOnlyOpaque -> DEPTH_ONLY_OPAQUE`, `Transparent -> TRANSPARENT`)
2. 值为On的FlagMode的键名转为大写下划线形式(例: `RenderAsBillboards=On -> RENDER_AS_BILLBOARDS`, `Seasons=On -> SEASONS`, `Seasons=Off -> 无`)

## defines.json
某些FlagMode的值不只有On和Off两种, 这时可以通过在解包输出目录中创建defines.json来给这些FlagMode添加宏定义规则   
defines.json格式如下:
```json
{
    "passes": {
        "Pass名1": ["宏名称1", "宏名称2"],
        "Pass名2": ["宏名称1", "宏名称2", "宏名称3"]
    },
    "flagModes": {
        "键名1": {
            "值1": ["宏名称1", "宏名称2"],
            "值2": ["宏名称1", "宏名称2", "宏名称3"]
        },
        "键名2": {
            "值1": ["宏名称1", "宏名称2"],
            "值2": ["宏名称1", "宏名称2", "宏名称3"],
            "值3": ["宏名称1"]
        }
    }
}
```
注意: 在defines.json中有声明的Pass和FlagMode不会再按照默认宏定义规则定义宏

## 编译示例
以编译1.18.31的RenderChunk.material.bin为例:
1. 执行`java -jar MaterialBinTool-0.5.0-all.jar -u RenderChunk.material.bin文件路径`   
执行完后的目录结构应该是这样:
```
RenderChunk/
    AlphaTest/
        0~5.平台名称.着色器类型.着色器语言
        AlphaTest.json
    DepthOnly/
        0~5.平台名称.着色器类型.着色器语言
        DepthOnly.json
    DepthOnlyOpaque/
        0~5.平台名称.着色器类型.着色器语言
        DepthOnlyOpaque.json
    Opaque/
        0~5.平台名称.着色器类型.着色器语言
        Opaque.json
    Transparent/
        0~5.平台名称.着色器类型.着色器语言
        Transparent.json
    RenderChunk.json
```
2. 在`RenderChunk`目录中创建`src`目录, 并放置`RenderChunk.vertex.sc`、`RenderChunk.fragment.sc`、`RenderChunk.varying.def.sc`及`bgfx_shader.sh`等头文件(可选)
3. 因为RenderChunk.material.bin中的FlagMode的值都只有On/Off两种, 故无需创建`defines.json`以额外添加宏定义规则(其他文件同理视情况创建)   
此时目录结构应为:
```
RenderChunk/
    AlphaTest/
        0~5.平台名称.着色器类型.着色器语言
        AlphaTest.json
    DepthOnly/
        0~5.平台名称.着色器类型.着色器语言
        DepthOnly.json
    DepthOnlyOpaque/
        0~5.平台名称.着色器类型.着色器语言
        DepthOnlyOpaque.json
    Opaque/
        0~5.平台名称.着色器类型.着色器语言
        Opaque.json
    Transparent/
        0~5.平台名称.着色器类型.着色器语言
        Transparent.json
    src/
        bgfx_shader.sc(可选)
        RenderChunk.vertex.sc
        RenderChunk.fragment.sc
        RenderChunk.varying.def.sc
    RenderChunk.json
```
4. 执行`java -jar MaterialBinTool-0.5.0-all.jar -s shaderc.exe路径(可选) -i 包含bgfx_shader.sh的目录路径(可选) -c RenderChunk目录路径`开始编译   
若已将shaderc.exe所在目录添加至PATH环境变量,则可不指定`-s`参数   
若已将bgfx_shader.sh复制至,则可不指定`-i`参数
5. 执行完成后会在`RenderChunk`目录下生成编译出的`RenderChunk.material.bin`,替换安装包里的对应文件即可使用   
   
注意: 目前编译出的文件仍然不是全平台通用, 解包的是哪个平台的编译完以后的文件就只能在哪个平台上使用


## sc源文件获取
可在[RenderDragonSorceCodeInv](https://github.com/OEOTYAN/RenderDragonSorceCodeInv) 仓库中获取部分已经由[OEOTYAN](https://github.com/OEOTYAN/) 整理好的sc源文件   
其他源文件(RTX相关的除外)可根据安卓版的.material.bin解包出的glsl自行手动整理    
`bgfx_shader.sc`可在bgfx仓库获取(渲染龙使用的bgfx版本较老, 不保证最新版仓库里的源码可用, 这里提供一个可用的版本: [bgfx_shader.sc](https://github.com/bkaradzic/bgfx/blob/1ba107d156d1d28e86550df5d586ea259aec1020/src/bgfx_shader.sh))
