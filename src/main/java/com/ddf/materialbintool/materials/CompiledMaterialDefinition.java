package com.ddf.materialbintool.materials;

import com.ddf.materialbintool.bgfx.BgfxShader;
import com.ddf.materialbintool.definition.*;
import com.ddf.materialbintool.util.ByteBufUtil;
import com.ddf.materialbintool.util.Pair;
import com.ddf.materialbintool.util.Util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

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
    private List<PropertyField> propertyFieldList;
    private List<Pass> passList;

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
				ByteBuf decrypted = Unpooled.wrappedBuffer(Util.decrypt(key, iv, encrypted));
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
			SamplerDefinition samplerDefinition = new SamplerDefinition();
			samplerDefinition.readFrom(buf);
			samplerDefinitionMap.put(samplerDefinition.getName(), samplerDefinition);
        }

        short propertyFieldCount = buf.readShortLE();
        propertyFieldList = new ArrayList<>(propertyFieldCount);
        for (int i = 0; i < propertyFieldCount; i++) {
            PropertyField propertyField = new PropertyField();
            propertyField.readFrom(buf);
            propertyFieldList.add(propertyField);
        }

        short passCount = buf.readShortLE();
        passList = new ArrayList<>(passCount);
        for (int i = 0; i < passCount; ++i) {
            Pass pass = new Pass();
            pass.readFrom(buf);
            passList.add(pass);
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
        for (SamplerDefinition samplerDefinition : samplerDefinitionMap.values()) {
            samplerDefinition.writeTo(buf);
        }

        buf.writeShortLE(propertyFieldList.size());
        for (PropertyField propertyField : propertyFieldList) {
            propertyField.writeTo(buf);
        }

        buf.writeShortLE(passList.size());
        for (Pass pass : passList) {
            pass.writeTo(buf);
        }

        buf.writeLongLE(MAGIC);
    }

    public static class Pass {
        private String name;
        private boolean hasBitSet = false;
        private String bitSet; //111111111111111 / 011111010111110 / 000000100000000
        private byte unknownByte0;
        private String unknownString0;  //空字符串 / Fallback / DoCheckerboarding

        private boolean unknownBool0;
        private short unknownShort0;
        private List<Pair<String, String>> unknownStringPairList;

        private List<FlagMode> flagModeList;
        private List<Variant> variantList;

        public Pass() {
        }

        public void readFrom(ByteBuf buf) {
            name = ByteBufUtil.readString(buf);
            hasBitSet = buf.readIntLE() == 15;
            buf.readerIndex(buf.readerIndex() - 4);

            if (hasBitSet) {
                bitSet = ByteBufUtil.readString(buf);
            } else {
                unknownByte0 = buf.readByte();
            }
            unknownString0 = ByteBufUtil.readString(buf);

            unknownBool0 = buf.readBoolean();
            if (unknownBool0) {
                unknownShort0 = buf.readShortLE();
                short unknownCount2 = buf.readShortLE();
                unknownStringPairList = new ArrayList<>(unknownCount2);
                for (int i = 0; i < unknownCount2; ++i) {
                    Pair<String, String> pair = new Pair<>(ByteBufUtil.readString(buf), ByteBufUtil.readString(buf));
                    unknownStringPairList.add(pair);
                }
            }

            short flagModeCount = buf.readShortLE();
            flagModeList = new ArrayList<>(flagModeCount);

            for (int j = 0; j < flagModeCount; ++j) {
                FlagMode flagMode = new FlagMode();
                flagMode.readFrom(buf);
                flagModeList.add(flagMode);
            }

            short variantCount = buf.readShortLE();
            variantList = new ArrayList<>(variantCount);
            for (int l = 0; l < variantCount; ++l) {
                Variant variant = new Variant();
                variant.readFrom(buf);
                variantList.add(variant);
            }
        }

        public void writeTo(ByteBuf buf) {
            ByteBufUtil.writeString(buf, name);
            if (hasBitSet) {
                ByteBufUtil.writeString(buf, bitSet);
            } else {
                buf.writeByte(unknownByte0);
            }
            ByteBufUtil.writeString(buf, unknownString0);

            buf.writeBoolean(unknownBool0);
            if (unknownBool0) {
                buf.writeShortLE(unknownShort0);
                buf.writeShortLE(unknownStringPairList.size());
                for (Pair<String, String> pair : unknownStringPairList) {
                    ByteBufUtil.writeString(buf, pair.getKey());
                    ByteBufUtil.writeString(buf, pair.getValue());
                }
            }

            buf.writeShortLE(flagModeList.size());
            for (FlagMode flagMode : flagModeList) {
                flagMode.writeTo(buf);
            }

            buf.writeShortLE(variantList.size());
            for (Variant variant : variantList) {
                variant.writeTo(buf);
            }
        }
    }

    public static class Variant {
        private boolean unknownBool0;
        private List<FlagMode> flagModeList;
        private List<ShaderCode> shaderCodeList;

        public Variant() {
        }

        public void readFrom(ByteBuf buf) {
            unknownBool0 = buf.readBoolean();
            short flagModeCount = buf.readShortLE();
            short shaderCodeCount = buf.readShortLE();

            flagModeList = new ArrayList<>(flagModeCount);
            for (int j = 0; j < flagModeCount; ++j) {
                FlagMode flagMode = new FlagMode();
                flagMode.readFrom(buf);
                flagModeList.add(flagMode);
            }

            shaderCodeList = new ArrayList<>(shaderCodeCount);
            for (int i = 0; i < shaderCodeCount; ++i) {
                ShaderCode shaderCode = new ShaderCode();
                shaderCode.readFrom(buf);
                shaderCodeList.add(shaderCode);
            }
        }

        public void writeTo(ByteBuf buf) {
            buf.writeBoolean(unknownBool0);
            buf.writeShortLE(flagModeList.size());
            buf.writeShortLE(shaderCodeList.size());
            for (FlagMode flagMode : flagModeList) {
                flagMode.writeTo(buf);
            }
            for (ShaderCode shaderCode : shaderCodeList) {
                shaderCode.writeTo(buf);
            }
        }
    }

    public static class ShaderCode {
        private String type;
        private String platform;
        private byte unknownByte0; //Vertex 0   Fragment 1   Unknown 3
        private byte unknownByte1;
        private List<ShaderInput> shaderInputList;

        private byte[] unknownBytes0;
        private BgfxShader bgfxShader;

        public ShaderCode() {
        }

        public void readFrom(ByteBuf buf) {
            type = ByteBufUtil.readString(buf);
            platform = ByteBufUtil.readString(buf);
            unknownByte0 = buf.readByte();
            unknownByte1 = buf.readByte();

            short shaderInputCount = buf.readShortLE();
            shaderInputList = new ArrayList<>(shaderInputCount);
            for (int i = 0; i < shaderInputCount; ++i) {
                ShaderInput shaderInput = new ShaderInput();
                shaderInput.readFrom(buf);
                shaderInputList.add(shaderInput);
            }

            unknownBytes0 = ByteBufUtil.readBytes(buf, 8);

            int length = buf.readIntLE();
            ByteBuf byteBuf = buf.readBytes(length);

            bgfxShader = BgfxShader.create(platform);
            bgfxShader.readFrom(byteBuf);
        }

        public void writeTo(ByteBuf buf) {
            ByteBufUtil.writeString(buf, type);
            ByteBufUtil.writeString(buf, platform);
            buf.writeByte(unknownByte0);
            buf.writeByte(unknownByte1);
            buf.writeShortLE(shaderInputList.size());
            for (ShaderInput shaderInput : shaderInputList) {
                shaderInput.writeTo(buf);
            }
            buf.writeBytes(unknownBytes0);

            ByteBuf byteBuf = Unpooled.buffer();
            bgfxShader.writeTo(byteBuf);

            buf.writeIntLE(byteBuf.readableBytes());
            buf.writeBytes(byteBuf);
        }
    }
}


