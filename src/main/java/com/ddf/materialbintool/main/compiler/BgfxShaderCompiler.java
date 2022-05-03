package com.ddf.materialbintool.main.compiler;

import com.ddf.materialbintool.main.util.FileUtil;
import com.ddf.materialbintool.materials.ShaderCodePlatform;
import com.ddf.materialbintool.materials.ShaderCodeType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BgfxShaderCompiler {
    private Random random = new Random();
    private String compilerPath;
    private File tempDir;

    public BgfxShaderCompiler(String compilerPath) {
        this.compilerPath = compilerPath;
        this.tempDir = createTempDir();
    }

    public byte[] compile(File input, File varyingDef, Defines defines, ShaderCodePlatform platform, ShaderCodeType type) {
        File tempOutputFile = new File(tempDir, System.nanoTime() + Integer.toHexString(random.nextInt()));
        compile(input, varyingDef, tempOutputFile, defines, platform, type);
        byte[] output = tempOutputFile.exists() ? FileUtil.readAllBytes(tempOutputFile) : null;
        FileUtil.delete(tempOutputFile);
        return output;
    }

    public void compile(File input, File varyingDef, File output, Defines defines, ShaderCodePlatform platform, ShaderCodeType type) {
        List<String> command = new ArrayList<>();
        command.add(compilerPath);

        command.add("-f");
        command.add(input.getAbsolutePath());

        command.add("-o");
        command.add(output.getPath());

        command.add("--varyingdef");
        command.add(varyingDef.getAbsolutePath());

        command.add("--define");
        command.add(defines.toString());

        command.add("--platform");
        command.add(toPlatformString(platform));

        command.add("--type");
        command.add(toTypeString(type));

        if (platform.name().startsWith("Direct3D")) {
            command.add("--profile");
            command.add(toProfileString(platform, type));
        }

        try {
            Process process = new ProcessBuilder()
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .command(command)
                    .start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static File createTempDir() {
        try {
            File dir = Files.createTempDirectory("materialbintool").toFile();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtil.delete(dir)));
            return dir;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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
            default:
                return "";
        }
    }
}
