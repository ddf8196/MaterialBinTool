package com.ddf.materialbintool.main.compiler;

import com.ddf.materialbintool.main.util.FileUtil;
import com.ddf.materialbintool.materials.PlatformShaderStage;
import org.anarres.cpp.FileLexerSource;
import org.anarres.cpp.LexerException;
import org.anarres.cpp.Preprocessor;
import org.anarres.cpp.Token;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class VaryingDefPreprocessor implements AutoCloseable {
    private final Map<PlatformShaderStage, File> cache = new HashMap<>();
    private final File inputFile;
    private final File tempDir;

    public VaryingDefPreprocessor(File inputFile) {
        this(inputFile, FileUtil.createTempDir());
    }

    public VaryingDefPreprocessor(File inputFile, File tempDir) {
        this.inputFile = inputFile;
        this.tempDir = tempDir;
    }

    public File getPreprocessedVaryingDef(PlatformShaderStage platformShaderStage) {
        File cached = cache.get(platformShaderStage);
        if (cached != null)
            return cached;

        try {
            Preprocessor preprocessor = new Preprocessor(new FileLexerSource(inputFile, StandardCharsets.UTF_8));

            preprocessor.addMacro("BGFX_SHADER_LANGUAGE_GLSL", "0");
            preprocessor.addMacro("BGFX_SHADER_LANGUAGE_HLSL", "0");
            preprocessor.addMacro("BGFX_SHADER_LANGUAGE_METAL", "0");
            preprocessor.addMacro("BGFX_SHADER_LANGUAGE_PSSL", "0");
            preprocessor.addMacro("BGFX_SHADER_LANGUAGE_SPIRV", "0");

            preprocessor.addMacro("BGFX_SHADER_TYPE_COMPUTE", "0");
            preprocessor.addMacro("BGFX_SHADER_TYPE_FRAGMENT", "0");
            preprocessor.addMacro("BGFX_SHADER_TYPE_VERTEX", "0");

            switch (platformShaderStage.platform) {
                case Direct3D_SM40:
                    preprocessor.addMacro("BGFX_SHADER_LANGUAGE_HLSL", "400");
                    break;
                case Direct3D_SM50:
                    preprocessor.addMacro("BGFX_SHADER_LANGUAGE_HLSL", "500");
                    break;
                case Direct3D_SM60:
                    preprocessor.addMacro("BGFX_SHADER_LANGUAGE_HLSL", "630");
                    break;
                case Direct3D_SM65:
                    preprocessor.addMacro("BGFX_SHADER_LANGUAGE_HLSL", "650");
                    break;
                case Direct3D_XB1:
                case Direct3D_XBX:
                    break;
                case GLSL_120:
                    preprocessor.addMacro("BGFX_SHADER_LANGUAGE_GLSL", "120");
                    break;
                case GLSL_430:
                    preprocessor.addMacro("BGFX_SHADER_LANGUAGE_GLSL", "430");
                    break;
                case ESSL_100:
                    preprocessor.addMacro("BGFX_SHADER_LANGUAGE_GLSL", "100");
                    break;
                case ESSL_300:
                    preprocessor.addMacro("BGFX_SHADER_LANGUAGE_GLSL", "300");
                    break;
                case ESSL_310:
                    preprocessor.addMacro("BGFX_SHADER_LANGUAGE_GLSL", "310");
                    break;
                case Metal:
                    preprocessor.addMacro("BGFX_SHADER_LANGUAGE_METAL", "1");
                    break;
                case Vulkan:
                    preprocessor.addMacro("BGFX_SHADER_LANGUAGE_SPIRV", "0");
                    break;
                case Nvn:
                    break;
                case Pssl:
                    preprocessor.addMacro("BGFX_SHADER_LANGUAGE_PSSL", "0");
                    break;
                default:
                    break;
            }

            switch (platformShaderStage.stage) {
                case Vertex:
                    preprocessor.addMacro("BGFX_SHADER_TYPE_VERTEX", "1");
                    break;
                case Fragment:
                    preprocessor.addMacro("BGFX_SHADER_TYPE_FRAGMENT", "1");
                    break;
                case Compute:
                    preprocessor.addMacro("BGFX_SHADER_TYPE_COMPUTE", "1");
                    break;
                case Unknown:
                default:
                    preprocessor.addMacro("BGFX_SHADER_TYPE_FRAGMENT", "1");
                    break;
            }

            StringBuilder preprocessed = new StringBuilder();
            for (Token token = preprocessor.token(); token != null && token.getType() != Token.EOF; token = preprocessor.token()) {
                if (token.getType() == Token.CCOMMENT || token.getType() == Token.CPPCOMMENT)
                    continue;
                preprocessed.append(token.getText());
            }
            preprocessor.close();

            File file = new File(tempDir, System.nanoTime() + Integer.toHexString(ThreadLocalRandom.current().nextInt()) + "varying.def.sc");
            FileUtil.writeString(file, preprocessed.toString());
            cache.put(platformShaderStage, file);
            return file;
        } catch (IOException | LexerException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() {
        for (File file : cache.values()) {
            FileUtil.delete(file);
        }
        cache.clear();
    }
}
