package com.ddf.materialbintool.materials;

import com.ddf.materialbintool.materials.definition.*;
import com.ddf.materialbintool.util.ByteBufUtil;
import com.ddf.materialbintool.util.IData;
import com.ddf.materialbintool.util.Util;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class CompiledMaterialDefinition {
    public static final long MAGIC = 0xA11DA1A;
    public static final String COMPILED_MATERIAL_DEFINITION = "RenderDragon.CompiledMaterialDefinition";

    private long version;
    private EncryptionVariants encryptionVariant;
    private String name;
    private boolean hasName2;
    private String name2;

    private Map<String, SamplerDefinition> samplerDefinitionMap;
    private Map<String, PropertyField> propertyFieldMap;
    private Map<String, Pass> passMap;

    public void loadFrom(ByteBuf buf) {
        long magic = buf.readLongLE();
        if (magic != MAGIC)
            return;
		if (!COMPILED_MATERIAL_DEFINITION.equals(ByteBufUtil.readString(buf)))
			return;
        version = buf.readLongLE();
        if (version >= 0x16)
            encryptionVariant = EncryptionVariants.getBySignature(buf.readIntLE());
        else
            encryptionVariant = EncryptionVariants.None;
        switch (encryptionVariant) {
            case None: {
				loadContent(buf);
                break;
            }
            case SimplePassphrase: {
                byte[] digest = null;
                try {
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    String text = "those are not the shaders you are looking for! ";
                    digest = md.digest(Base64.getEncoder().encode(text.getBytes(StandardCharsets.UTF_8)));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                byte[] key = ByteBufUtil.readByteArray(buf);
                if (!Arrays.equals(key, digest))
                    return;
                byte[] iv = ByteBufUtil.readByteArray(buf);
                byte[] encrypted = ByteBufUtil.readByteArray(buf);
				ByteBuf decrypted = ByteBufUtil.wrappedBuffer(Util.decrypt(key, iv, encrypted));
				loadContent(decrypted);
				break;
            }
            case KeyPair: {
                break;
            }
            default: {
                break;
            }
        }
    }

    private boolean loadContent(ByteBuf buf) {
        name = ByteBufUtil.readString(buf);
        hasName2 = buf.readBoolean();
        if (hasName2)
            name2 = ByteBufUtil.readString(buf);

        int samplerDefinitionCount = buf.readByte();
        samplerDefinitionMap = new LinkedHashMap<>(samplerDefinitionCount);
        for (int i = 0; i < samplerDefinitionCount; i++) {
            String name = ByteBufUtil.readString(buf);
			SamplerDefinition samplerDefinition = new SamplerDefinition();
			samplerDefinition.read(buf);
			samplerDefinitionMap.put(name, samplerDefinition);
        }

        short propertyFieldCount = buf.readShortLE();
        propertyFieldMap = new LinkedHashMap<>(propertyFieldCount);
        for (int i = 0; i < propertyFieldCount; i++) {
            String name = ByteBufUtil.readString(buf);
            PropertyField propertyField = new PropertyField();
            propertyField.read(buf);
            propertyFieldMap.put(name, propertyField);
        }

        short passCount = buf.readShortLE();
        passMap = new LinkedHashMap<>(passCount);
        for (int i = 0; i < passCount; ++i) {
            String name = ByteBufUtil.readString(buf);
            Pass pass = new Pass();
            pass.read(buf);
            passMap.put(name, pass);
        }
        long end = buf.readLongLE();
        return end == MAGIC;
    }

    public void saveTo(ByteBuf buf) {
        buf.writeLongLE(MAGIC);
        ByteBufUtil.writeString(buf, COMPILED_MATERIAL_DEFINITION);
        buf.writeLongLE(version);

        if (version >= 0x16)
            buf.writeIntLE(EncryptionVariants.None.getSignature());

        ByteBufUtil.writeString(buf, name);
        buf.writeBoolean(hasName2);
        if (hasName2)
            ByteBufUtil.writeString(buf, name2);

        buf.writeByte(samplerDefinitionMap.size());
        for (Map.Entry<String, SamplerDefinition> entry : samplerDefinitionMap.entrySet()) {
            ByteBufUtil.writeString(buf, entry.getKey());
            entry.getValue().write(buf);
        }

        buf.writeShortLE(propertyFieldMap.size());
        for (Map.Entry<String, PropertyField> entry : propertyFieldMap.entrySet()) {
            ByteBufUtil.writeString(buf, entry.getKey());
            entry.getValue().write(buf);
        }

        buf.writeShortLE(passMap.size());
        for (Map.Entry<String, Pass> entry : passMap.entrySet()) {
            ByteBufUtil.writeString(buf, entry.getKey());
            entry.getValue().write(buf);
        }

        buf.writeLongLE(MAGIC);
    }

    public static class Pass implements IData {
        private boolean hasBitSet = false;
        private String bitSet; //111111111111111 / 011111010111110 / 000000100000000
        private byte unknownByte0;
        private String unknownString0;  //空字符串 / Fallback / DoCheckerboarding

        private boolean hasBlendMode;
        private BlendMode blendMode;

        private Map<String, String> unknownStringMap;
        private List<Variant> variantList;

        public Pass() {
        }

        public void read(ByteBuf buf) {
            hasBitSet = buf.readIntLE() == 15;
            buf.readerIndex(buf.readerIndex() - 4);

            if (hasBitSet) {
                bitSet = ByteBufUtil.readString(buf);
            } else {
                unknownByte0 = buf.readByte();
            }
            unknownString0 = ByteBufUtil.readString(buf);

            hasBlendMode = buf.readBoolean();
            if (hasBlendMode) {
                blendMode = BlendMode.get(buf.readShortLE());
            }

            short unknownCount = buf.readShortLE();
            unknownStringMap = new LinkedHashMap<>(unknownCount);
            for (int i = 0; i < unknownCount; ++i) {
                String key = ByteBufUtil.readString(buf);
                String value = ByteBufUtil.readString(buf);
                unknownStringMap.put(key, value);
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
                ByteBufUtil.writeString(buf, bitSet);
            } else {
                buf.writeByte(unknownByte0);
            }
            ByteBufUtil.writeString(buf, unknownString0);

            buf.writeBoolean(hasBlendMode);
            if (hasBlendMode) {
                buf.writeShortLE(blendMode.ordinal());
            }

            buf.writeShortLE(unknownStringMap.size());
            for (Map.Entry<String, String> entry : unknownStringMap.entrySet()) {
                ByteBufUtil.writeString(buf, entry.getKey());
                ByteBufUtil.writeString(buf, entry.getValue());
            }

            buf.writeShortLE(variantList.size());
            for (Variant variant : variantList) {
                variant.write(buf);
            }
        }
    }

    public static class Variant implements IData {
        private boolean unknownBool0;
        private List<FlagMode> flagModeList;
        private Map<PlatformShaderStage, ShaderCode> shaderCodeMap;

        public Variant() {
        }

        public void read(ByteBuf buf) {
            unknownBool0 = buf.readBoolean();
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
            buf.writeBoolean(unknownBool0);
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

    public static class ShaderCode implements IData {
        private Map<String, ShaderInput> shaderInputMap;
        private long unknownLong0;
        private transient byte[] bgfxShader;

        public ShaderCode() {
        }

        public void read(ByteBuf buf) {
            short shaderInputCount = buf.readShortLE();
            shaderInputMap = new LinkedHashMap<>(shaderInputCount);
            for (int i = 0; i < shaderInputCount; ++i) {
                String name = ByteBufUtil.readString(buf);
                ShaderInput shaderInput = new ShaderInput();
                shaderInput.read(buf);
                shaderInputMap.put(name, shaderInput);
            }
            unknownLong0 = buf.readLong();
            bgfxShader = ByteBufUtil.readByteArray(buf);
        }

        public void write(ByteBuf buf) {
            buf.writeShortLE(shaderInputMap.size());
            for (Map.Entry<String, ShaderInput> entry : shaderInputMap.entrySet()) {
                ByteBufUtil.writeString(buf, entry.getKey());
                entry.getValue().write(buf);
            }
            buf.writeLong(unknownLong0);
            ByteBufUtil.writeByteArray(buf, bgfxShader);
        }
    }
}


