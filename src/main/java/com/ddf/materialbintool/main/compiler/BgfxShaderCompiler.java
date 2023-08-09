package com.ddf.materialbintool.main.compiler;

import com.ddf.materialbintool.main.util.FileUtil;
import com.ddf.materialbintool.materials.ShaderCodePlatform;
import com.ddf.materialbintool.materials.definition.ShaderStage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class BgfxShaderCompiler {
    private final String compilerPath;
    private final List<String> includePaths = new ArrayList<>();
    private boolean debug = false;
    private boolean optimize = true;
    private int optimizationLevel = 3;
    private final File tempDir;

    public BgfxShaderCompiler(String compilerPath) {
        this(compilerPath, FileUtil.createTempDir());
    }

    public BgfxShaderCompiler(String compilerPath, File tempDir) {
        this.compilerPath = compilerPath;
        this.tempDir = tempDir;
    }

    public void addIncludePath(String includePath) {
        includePaths.add(includePath);
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isOptimize() {
        return optimize;
    }

    public void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }

    public int getOptimizationLevel() {
        return optimizationLevel;
    }

    public void setOptimizationLevel(int optimizationLevel) {
        this.optimizationLevel = optimizationLevel;
    }

    public byte[] compile(File input, File varyingDef, Defines defines, ShaderCodePlatform platform, ShaderStage type) {
        File tempOutputFile = new File(tempDir, input.getName() + "_" + System.nanoTime() + Integer.toHexString(ThreadLocalRandom.current().nextInt()) + ".bin");
        int code = compile(input, varyingDef, tempOutputFile, defines, platform, type);
        if (code == 0) {
            byte[] output = tempOutputFile.exists() ? FileUtil.readAllBytes(tempOutputFile) : null;
            FileUtil.delete(tempOutputFile);
            return output;
        } else {
            return null;
        }
    }

    public int compile(File input, File varyingDef, File output, Defines defines, ShaderCodePlatform platform, ShaderStage stage) {
        List<String> command = new ArrayList<>();
        command.add(compilerPath);

        for (String includePath : includePaths) {
            command.add("-i");
            command.add(includePath);
        }

        command.add("-f");
        command.add(input.getAbsolutePath());

        command.add("--varyingdef");
        command.add(varyingDef.getAbsolutePath());

        command.add("-o");
        command.add(output.getPath());

        command.add("--define");
        command.add(defines.toString());

        command.add("--platform");
        command.add(toPlatformString(platform));

        command.add("--type");
        command.add(toTypeString(stage));

        command.add("--profile");
        command.add(toProfileString(platform));

        if (optimize) {
            command.add("-O");
            command.add(Integer.toString(Math.max(Math.min(optimizationLevel, 3), 0)));
        }

        if (debug)
            command.add("--debug");

        try {
            Process process = new ProcessBuilder()
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .command(command)
                    .start();
            return process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
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
            case Vulkan:
                return "vulkan";
            default:
                return "";
        }
    }

    private static String toTypeString(ShaderStage shaderStage) {
        switch (shaderStage) {
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

    private static String toProfileString(ShaderCodePlatform platform) {
        switch (platform) {
            case Direct3D_SM40:
                return "s_4_0";
            case Direct3D_SM50:
            case Direct3D_SM60:
            case Direct3D_SM65:
                return "s_5_0";
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
