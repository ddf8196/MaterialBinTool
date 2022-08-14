package com.ddf.materialbintool.materials;

import com.ddf.materialbintool.materials.definition.*;
import com.ddf.materialbintool.util.ByteBuf;

import java.util.*;

public class CompiledMaterialDefinition {
    public static final long MAGIC = 0xA11DA1A;
    public static final String COMPILED_MATERIAL_DEFINITION = "RenderDragon.CompiledMaterialDefinition";

    private long version;
    private String name;
    private boolean hasParentName;
    private String parentName;

    private Map<String, SamplerDefinition> samplerDefinitionMap;
    private Map<String, PropertyField> propertyFieldMap;
    public transient Map<String, Pass> passMap;

    public void loadFrom(ByteBuf buf) {
        long magic = buf.readLongLE();
        if (magic != MAGIC)
            return;
		if (!COMPILED_MATERIAL_DEFINITION.equals(buf.readStringLE()))
			return;
        version = buf.readLongLE();
        if (version != 19)
            throw new UnsupportedOperationException("Only files with version 19 are supported");

        loadContent(buf);
    }

    private boolean loadContent(ByteBuf buf) {
        name = buf.readStringLE();
        hasParentName = buf.readBoolean();
        if (hasParentName)
            parentName = buf.readStringLE();

        int samplerDefinitionCount = buf.readUnsignedByte();
        samplerDefinitionMap = new LinkedHashMap<>(samplerDefinitionCount);
        for (int i = 0; i < samplerDefinitionCount; i++) {
            String name = buf.readStringLE();
			SamplerDefinition samplerDefinition = new SamplerDefinition();
			samplerDefinition.read(buf);
			samplerDefinitionMap.put(name, samplerDefinition);
        }

        short propertyFieldCount = buf.readShortLE();
        propertyFieldMap = new LinkedHashMap<>(propertyFieldCount);
        for (int i = 0; i < propertyFieldCount; i++) {
            String name = buf.readStringLE();
            PropertyField propertyField = new PropertyField();
            propertyField.read(buf);
            propertyFieldMap.put(name, propertyField);
        }

        short passCount = buf.readShortLE();
        passMap = new LinkedHashMap<>(passCount);
        for (int i = 0; i < passCount; ++i) {
            String name = buf.readStringLE();
            Pass pass = new Pass();
            pass.read(buf);
            passMap.put(name, pass);
        }
        long end = buf.readLongLE();
        return end == MAGIC;
    }

    public void saveTo(ByteBuf buf) {
        buf.writeLongLE(MAGIC);
        buf.writeStringLE(COMPILED_MATERIAL_DEFINITION);
        buf.writeLongLE(version);
        saveContent(buf);
    }

    private void saveContent(ByteBuf buf) {
        buf.writeStringLE(name);
        buf.writeBoolean(hasParentName);
        if (hasParentName)
            buf.writeStringLE(parentName);

        buf.writeByte(samplerDefinitionMap.size());
        for (Map.Entry<String, SamplerDefinition> entry : samplerDefinitionMap.entrySet()) {
            buf.writeStringLE(entry.getKey());
            entry.getValue().write(buf);
        }

        buf.writeShortLE(propertyFieldMap.size());
        for (Map.Entry<String, PropertyField> entry : propertyFieldMap.entrySet()) {
            buf.writeStringLE(entry.getKey());
            entry.getValue().write(buf, entry.getKey());
        }

        buf.writeShortLE(passMap.size());
        for (Map.Entry<String, Pass> entry : passMap.entrySet()) {
            buf.writeStringLE(entry.getKey());
            entry.getValue().write(buf);
        }

        buf.writeLongLE(MAGIC);
    }

    public static class Pass {
        private GraphicsProfile graphicsProfile;
        private String fallback;  //空字符串 / Fallback / DoCheckerboarding

        private boolean hasDefaultBlendMode;
        private BlendMode defaultBlendMode;

        private Map<String, String> defaultFlagModes;
        public List<Variant> variantList;

        public Pass() {
        }

        public void read(ByteBuf buf) {
            graphicsProfile = GraphicsProfile.get(buf.readByte());
            fallback = buf.readStringLE();

            hasDefaultBlendMode = buf.readBoolean();
            if (hasDefaultBlendMode) {
                defaultBlendMode = BlendMode.get(buf.readShortLE());
            }

            short defaultFlagModeCount = buf.readShortLE();
            defaultFlagModes = new LinkedHashMap<>(defaultFlagModeCount);
            for (int i = 0; i < defaultFlagModeCount; ++i) {
                String key = buf.readStringLE();
                String value = buf.readStringLE();
                defaultFlagModes.put(key, value);
            }

            short variantCount = buf.readShortLE();
            variantList = new ArrayList<>(variantCount);
            for (int l = 0; l < variantCount; ++l) {
                Variant variant = new Variant();
                variant.read(buf);
                variantList.add(variant);
            }
        }

        public void write(ByteBuf buf) {
            buf.writeByte(graphicsProfile.ordinal());
            buf.writeStringLE(fallback);

            buf.writeBoolean(hasDefaultBlendMode);
            if (hasDefaultBlendMode) {
                buf.writeShortLE(defaultBlendMode.ordinal());
            }

            buf.writeShortLE(defaultFlagModes.size());
            for (Map.Entry<String, String> entry : defaultFlagModes.entrySet()) {
                buf.writeStringLE(entry.getKey());
                buf.writeStringLE(entry.getValue());
            }

            buf.writeShortLE(variantList.size());
            for (Variant variant : variantList) {
                variant.write(buf);
            }
        }
    }

    public static class Variant {
        public boolean isSupported;
        public List<FlagMode> flagModeList;
        public transient Map<PlatformShaderStage, ShaderCode> shaderCodeMap;

        public Variant() {
        }

        public void read(ByteBuf buf) {
            isSupported = buf.readBoolean();
            short flagModeCount = buf.readShortLE();
            short shaderCodeCount = buf.readShortLE();

            flagModeList = new ArrayList<>(flagModeCount);
            for (int j = 0; j < flagModeCount; ++j) {
                FlagMode flagMode = new FlagMode();
                flagMode.read(buf);
                flagModeList.add(flagMode);
            }

            shaderCodeMap = new LinkedHashMap<>(shaderCodeCount);
            for (int i = 0; i < shaderCodeCount; ++i) {
                PlatformShaderStage platformShaderStage = new PlatformShaderStage();
                platformShaderStage.read(buf);
                ShaderCode shaderCode = new ShaderCode();
                shaderCode.read(buf);
                shaderCodeMap.put(platformShaderStage, shaderCode);
            }
        }

        public void write(ByteBuf buf) {
            buf.writeBoolean(isSupported);
            buf.writeShortLE(flagModeList.size());
            buf.writeShortLE(shaderCodeMap.size());
            for (FlagMode flagMode : flagModeList) {
                flagMode.write(buf);
            }
            for (Map.Entry<PlatformShaderStage, ShaderCode> entry : shaderCodeMap.entrySet()) {
                entry.getKey().write(buf);
                entry.getValue().write(buf);
            }
        }
    }

    public static class ShaderCode {
        public Map<String, ShaderInput> shaderInputMap;
        public long sourceHash;
        public transient byte[] bgfxShaderData;

        public ShaderCode() {
        }

        public void read(ByteBuf buf) {
            short shaderInputCount = buf.readShortLE();
            shaderInputMap = new LinkedHashMap<>(shaderInputCount);
            for (int i = 0; i < shaderInputCount; ++i) {
                String name = buf.readStringLE();
                ShaderInput shaderInput = new ShaderInput();
                shaderInput.read(buf);
                shaderInputMap.put(name, shaderInput);
            }
            sourceHash = buf.readLong();
            bgfxShaderData = buf.readByteArrayLE();
        }

        public void write(ByteBuf buf) {
            buf.writeShortLE(shaderInputMap.size());
            for (Map.Entry<String, ShaderInput> entry : shaderInputMap.entrySet()) {
                buf.writeStringLE(entry.getKey());
                entry.getValue().write(buf);
            }
            buf.writeLong(sourceHash);
            buf.writeByteArrayLE(bgfxShaderData);
        }
    }
}


