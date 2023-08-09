package com.ddf.materialbintool.materials;

import com.ddf.materialbintool.materials.definition.*;
import com.ddf.materialbintool.util.ByteBuf;
import com.ddf.materialbintool.util.Util;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class CompiledMaterialDefinition {
    public static final long MAGIC = 0xA11DA1A;
    public static final String COMPILED_MATERIAL_DEFINITION = "RenderDragon.CompiledMaterialDefinition";

    public long version;
    public EncryptionVariants encryptionVariant;
    public String name;
    public boolean hasParentName;
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
                //1. Decrypt the data array using the RSA-1024 algorithm with a hardcoded private key
                //2. Compute SHA-256 of the decrypted data array as the decryption key
                //3. Decrypt the encrypted array using the AES-256-GCM algorithm
                //4. Load the decrypted array

                throw new RuntimeException("KeyPair encryption is not currently supported");
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
                throw new RuntimeException("KeyPair encryption is not currently supported");
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
        public String bitSet; //platformSupport
        public String fallback;

        public boolean hasDefaultBlendMode;
        public BlendMode defaultBlendMode;

        public Map<String, String> flagDefaultValues;
        public List<Variant> variantList;

        public Pass() {
        }

        public void read(ByteBuf buf) {
            bitSet = buf.readStringLE();
            fallback = buf.readStringLE();

            hasDefaultBlendMode = buf.readBoolean();
            if (hasDefaultBlendMode) {
                defaultBlendMode = BlendMode.get(buf.readShortLE());
            }

            short flagDefaultValueCount = buf.readShortLE();
            flagDefaultValues = new LinkedHashMap<>(flagDefaultValueCount);
            for (int i = 0; i < flagDefaultValueCount; ++i) {
                String key = buf.readStringLE();
                String value = buf.readStringLE();
                flagDefaultValues.put(key, value);
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
            buf.writeStringLE(bitSet);
            buf.writeStringLE(fallback);

            buf.writeBoolean(hasDefaultBlendMode);
            if (hasDefaultBlendMode) {
                buf.writeShortLE(defaultBlendMode.ordinal());
            }

            buf.writeShortLE(flagDefaultValues.size());
            for (Map.Entry<String, String> entry : flagDefaultValues.entrySet()) {
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
            return hasDefaultBlendMode == pass.hasDefaultBlendMode
                    && Objects.equals(bitSet, pass.bitSet)
                    && Objects.equals(fallback, pass.fallback)
                    && defaultBlendMode == pass.defaultBlendMode
                    && Objects.equals(flagDefaultValues, pass.flagDefaultValues)
                    && Objects.equals(variantList, pass.variantList);
        }

        @Override
        public int hashCode() {
            return Objects.hash(bitSet, fallback, hasDefaultBlendMode, defaultBlendMode, flagDefaultValues, variantList);
        }
    }

    public static class Variant {
        public boolean isSupported;
        public Map<String, String> flags;
        public transient Map<PlatformShaderStage, ShaderCode> shaderCodeMap;

        public Variant() {
        }

        public void read(ByteBuf buf) {
            isSupported = buf.readBoolean();
            short flagCount = buf.readShortLE();
            short shaderCodeCount = buf.readShortLE();

            flags = new LinkedHashMap<>(flagCount);
            for (int j = 0; j < flagCount; ++j) {
                String key = buf.readStringLE();
                String value = buf.readStringLE();
                flags.put(key, value);
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
            for (Map.Entry<String, String> flag : flags.entrySet()) {
                buf.writeStringLE(flag.getKey());
                buf.writeStringLE(flag.getValue());
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


