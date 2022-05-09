package com.ddf.materialbintool.main.compiler;

import com.ddf.materialbintool.main.util.FileUtil;
import com.ddf.materialbintool.materials.ShaderCodePlatform;
import com.ddf.materialbintool.materials.ShaderCodeType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BgfxShaderCompiler {
    private final String compilerPath;
    private final List<String> includePaths = new ArrayList<>();

    public BgfxShaderCompiler(String compilerPath) {
        this.compilerPath = compilerPath;
    }

    public void addIncludePath(String includePath) {
        includePaths.add(includePath);
    }

    public byte[] compile(File input, File varyingDef, Defines defines, ShaderCodePlatform platform, ShaderCodeType type) {
        List<String> command = new ArrayList<>();
        command.add(compilerPath);

        command.add("--stdout");

        command.add("-f");
        command.add(input.getAbsolutePath());

        command.add("--varyingdef");
        command.add(varyingDef.getAbsolutePath());

        command.add("--define");
        command.add(defines.toString());

        command.add("--platform");
        command.add(toPlatformString(platform));

        command.add("--type");
        command.add(toTypeString(type));

        command.add("--profile");
        command.add(toProfileString(platform, type));

        for (String includePath : includePaths) {
            command.add("-i");
            command.add(includePath);
        }

        try {
            Process process = new ProcessBuilder()
                    .command(command)
                    .start();
            int code = process.waitFor();
            if (code == 0) {
                return FileUtil.readAllBytes(process.getInputStream());
            } else {
                byte[] stdout = FileUtil.readAllBytes(process.getInputStream());
                byte[] stderr = FileUtil.readAllBytes(process.getErrorStream());
                if (stdout != null)
                    System.out.write(stdout);
                if (stderr != null)
                    System.err.write(stderr);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String toPlatformString(ShaderCodePlatform shaderCodePlatform) {
        switch (shaderCodePlatform) {
            case ESSL_100:
            case ESSL_300:
            case ESSL_310:
                return "android";
            case Direct3D_SM40:
            case Direct3D_SM50:
            case Direct3D_SM60:
            case Direct3D_SM65:
                return "windows";
            case Metal:
                return "ios";
            default:
                return "";
        }
    }

    private static String toTypeString(ShaderCodeType shaderCodeType) {
        switch (shaderCodeType) {
            case Vertex:
                return "vertex";
            case Fragment:
                return "fragment";
            case Compute:
                return "compute";
            case Unknown:
                return "fragment";
            default:
                return "";
        }
    }

    private static String toProfileString(ShaderCodePlatform platform, ShaderCodeType type) {
        String prefix = "";
        switch (type) {
            case Vertex:
                prefix = "v";
                break;
            case Fragment:
                prefix = "p";
                break;
            case Compute:
                prefix = "c";
                break;
            case Unknown:
                prefix = "p";
                break;
        }

        switch (platform) {
            case Direct3D_SM40:
                return prefix + "s_4_0";
            case Direct3D_SM50:
            case Direct3D_SM60:
            case Direct3D_SM65:
                return prefix + "s_5_0";
            case GLSL_120:
                return "120";
            case GLSL_430:
                return "430";
            case ESSL_100:
                return "100_es";
            case ESSL_300:
                return "300_es";
            case ESSL_310:
                return "310_es";
            case Metal:
                return "metal";
            case Vulkan:
                return "spirv";
            default:
                return "";
        }
    }
}
