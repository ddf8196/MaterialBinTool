package com.ddf.materialbintool.materials;

import com.ddf.materialbintool.materials.definition.*;
import com.ddf.materialbintool.util.ByteBuf;
import com.ddf.materialbintool.util.Util;
import com.google.gson.annotations.SerializedName;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class CompiledMaterialDefinition {
    public static final long MAGIC = 0xA11DA1A;
    public static final String COMPILED_MATERIAL_DEFINITION = "RenderDragon.CompiledMaterialDefinition";

    public long version;
    public EncryptionVariants encryptionVariant;
    public String name;
    @SerializedName(value = "hasParentName", alternate = {"hasName2"})
    public boolean hasParentName;
    @SerializedName(value = "parentName", alternate = {"name2"})
    public String parentName;

    public Map<String, SamplerDefinition> samplerDefinitionMap;
    public Map<String, PropertyField> propertyFieldMap;
    public transient Map<String, Pass> passMap;

    public void loadFrom(ByteBuf buf) {
        long magic = buf.readLongLE();
        if (magic != MAGIC)
            return;
		if (!COMPILED_MATERIAL_DEFINITION.equals(buf.readStringLE()))
			return;
        version = buf.readLongLE();
        if (version < 0x16)
            throw new UnsupportedOperationException("Files with version less than 22 are no longer supported");

        encryptionVariant = EncryptionVariants.getBySignature(buf.readIntLE());
        switch (encryptionVariant) {
            case None: {
				loadContent(buf);
                break;
            }
            case SimplePassphrase: {
                byte[] digest = Util.sha256("dGhvc2UgYXJlIG5vdCB0aGUgc2hhZGVycyB5b3UgYXJlIGxvb2tpbmcgZm9yISA=".getBytes(StandardCharsets.UTF_8)); /*those are not the shaders you are looking for! */
                byte[] key = buf.readByteArrayLE();
                if (!Arrays.equals(key, digest)) {
                    //???
                }
                byte[] iv = buf.readByteArrayLE();
                byte[] encrypted = buf.readByteArrayLE();
				ByteBuf decrypted = new ByteBuf(Util.decrypt(key, iv, encrypted));
				loadContent(decrypted);
				break;
            }
            case KeyPair: {
                byte[] data = buf.readByteArrayLE();
                byte[] iv = buf.readByteArrayLE();
                byte[] encrypted = buf.readByteArrayLE();
                break;
            }
            default: {
                break;
            }
        }
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

    public void saveTo(ByteBuf buf, EncryptionVariants encryptionVariant) {
        buf.writeLongLE(MAGIC);
        buf.writeStringLE(COMPILED_MATERIAL_DEFINITION);
        buf.writeLongLE(version);
        buf.writeIntLE(encryptionVariant.getSignature());

        switch (encryptionVariant) {
            case None: {
                saveContent(buf);
                return;
            }
            case SimplePassphrase: {
                byte[] key = Util.sha256("dGhvc2UgYXJlIG5vdCB0aGUgc2hhZGVycyB5b3UgYXJlIGxvb2tpbmcgZm9yISA=".getBytes(StandardCharsets.UTF_8)); /*those are not the shaders you are looking for! */
                buf.writeByteArrayLE(key);

                byte[] iv = new byte[16];
                new Random().nextBytes(iv);
                buf.writeByteArrayLE(iv);

                ByteBuf byteBuf = new ByteBuf();
                saveContent(byteBuf);

                buf.writeByteArrayLE(Util.encrypt(key, iv, byteBuf.toByteArray()));
                return;
            }
            case KeyPair:
                return;
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompiledMaterialDefinition that = (CompiledMaterialDefinition) o;
        return version == that.version
                && hasParentName == that.hasParentName
                && encryptionVariant == that.encryptionVariant
                && Objects.equals(name, that.name)
                && Objects.equals(parentName, that.parentName)
                && Objects.equals(samplerDefinitionMap, that.samplerDefinitionMap)
                && Objects.equals(propertyFieldMap, that.propertyFieldMap)
                && Objects.equals(passMap, that.passMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, encryptionVariant, name, hasParentName, parentName, samplerDefinitionMap, propertyFieldMap, passMap);
    }

    public static class Pass {
        public boolean hasBitSet = false;
        public String bitSet; //111111111111111 / 011111010111110 / 000000100000000
        @SerializedName(value = "graphicsProfile", alternate = {"unknownByte0"})
        public byte graphicsProfile;
        public String fallback;  //空字符串 / Fallback / DoCheckerboarding

        @SerializedName(value = "hasDefaultBlendMode", alternate = {"hasBlendMode"})
        public boolean hasDefaultBlendMode;
        @SerializedName(value = "defaultBlendMode", alternate = {"blendMode"})
        public BlendMode defaultBlendMode;

        @SerializedName(value = "defaultFlags", alternate = {"defaultFlagModes"})
        public Map<String, String> defaultFlags;
        public List<Variant> variantList;

        public Pass() {
        }

        public void read(ByteBuf buf) {
            hasBitSet = buf.readIntLE() == 15;
            buf.readerIndex(buf.readerIndex() - 4);

            if (hasBitSet) {
                bitSet = buf.readStringLE();
            } else {
                graphicsProfile = buf.readByte();
            }
            fallback = buf.readStringLE();

            hasDefaultBlendMode = buf.readBoolean();
            if (hasDefaultBlendMode) {
                defaultBlendMode = BlendMode.get(buf.readShortLE());
            }

            short defaultFlagModeCount = buf.readShortLE();
            defaultFlags = new LinkedHashMap<>(defaultFlagModeCount);
            for (int i = 0; i < defaultFlagModeCount; ++i) {
                String key = buf.readStringLE();
                String value = buf.readStringLE();
                defaultFlags.put(key, value);
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
            if (hasBitSet) {
                buf.writeStringLE(bitSet);
            } else {
                buf.writeByte(graphicsProfile);
            }
            buf.writeStringLE(fallback);

            buf.writeBoolean(hasDefaultBlendMode);
            if (hasDefaultBlendMode) {
                buf.writeShortLE(defaultBlendMode.ordinal());
            }

            buf.writeShortLE(defaultFlags.size());
            for (Map.Entry<String, String> entry : defaultFlags.entrySet()) {
                buf.writeStringLE(entry.getKey());
                buf.writeStringLE(entry.getValue());
            }

            buf.writeShortLE(variantList.size());
            for (Variant variant : variantList) {
                variant.write(buf);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pass pass = (Pass) o;
            return hasBitSet == pass.hasBitSet
                    && graphicsProfile == pass.graphicsProfile
                    && hasDefaultBlendMode == pass.hasDefaultBlendMode
                    && Objects.equals(bitSet, pass.bitSet)
                    && Objects.equals(fallback, pass.fallback)
                    && defaultBlendMode == pass.defaultBlendMode
                    && Objects.equals(defaultFlags, pass.defaultFlags)
                    && Objects.equals(variantList, pass.variantList);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hasBitSet, bitSet, graphicsProfile, fallback, hasDefaultBlendMode, defaultBlendMode, defaultFlags, variantList);
        }
    }

    public static class Variant {
        @SerializedName(value = "isSupported", alternate = {"unknownBool0"})
        public boolean isSupported;
        @SerializedName(value = "flags", alternate = {"flagModeList"})
        public Set<FlagMode> flags;
        public transient Map<PlatformShaderStage, ShaderCode> shaderCodeMap;

        public Variant() {
        }

        public void read(ByteBuf buf) {
            isSupported = buf.readBoolean();
            short flagModeCount = buf.readShortLE();
            short shaderCodeCount = buf.readShortLE();

            flags = new LinkedHashSet<>(flagModeCount);
            for (int j = 0; j < flagModeCount; ++j) {
                FlagMode flagMode = new FlagMode();
                flagMode.read(buf);
                flags.add(flagMode);
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
            buf.writeShortLE(flags.size());
            buf.writeShortLE(shaderCodeMap.size());
            for (FlagMode flagMode : flags) {
                flagMode.write(buf);
            }
            for (Map.Entry<PlatformShaderStage, ShaderCode> entry : shaderCodeMap.entrySet()) {
                entry.getKey().write(buf);
                entry.getValue().write(buf);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Variant variant = (Variant) o;
            return isSupported == variant.isSupported && Objects.equals(flags, variant.flags) && Objects.equals(shaderCodeMap, variant.shaderCodeMap);
        }

        @Override
        public int hashCode() {
            return Objects.hash(isSupported, flags, shaderCodeMap);
        }
    }

    public static class ShaderCode {
        public Map<String, ShaderInput> shaderInputMap;
        @SerializedName(value = "sourceHash", alternate = {"unknownLong0"})
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ShaderCode that = (ShaderCode) o;
            return sourceHash == that.sourceHash && Objects.equals(shaderInputMap, that.shaderInputMap) && Arrays.equals(bgfxShaderData, that.bgfxShaderData);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(shaderInputMap, sourceHash);
            result = 31 * result + Arrays.hashCode(bgfxShaderData);
            return result;
        }
    }
}


