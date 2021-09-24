package com.ddf.materialbintool.materials;

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
		if (!ByteBufUtil.readString(buf).equals(COMPILED_MATERIAL_DEFINITION))
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
            Pass pass = new Pass(version);
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

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public EncryptionVariants getEncryptionVariant() {
        return encryptionVariant;
    }

    public void setEncryptionVariant(EncryptionVariants encryptionVariant) {
        this.encryptionVariant = encryptionVariant;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHasName2() {
        return hasName2;
    }

    public void setHasName2(boolean hasName2) {
        this.hasName2 = hasName2;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    public Map<String, SamplerDefinition> getSamplerDefinitionMap() {
        return samplerDefinitionMap;
    }

    public void setSamplerDefinitionMap(Map<String, SamplerDefinition> samplerDefinitionMap) {
        this.samplerDefinitionMap = samplerDefinitionMap;
    }

    public List<PropertyField> getPropertyFieldList() {
        return propertyFieldList;
    }

    public void setPropertyFieldList(List<PropertyField> propertyFieldList) {
        this.propertyFieldList = propertyFieldList;
    }

    public List<Pass> getPassList() {
        return passList;
    }

    public void setPassList(List<Pass> passList) {
        this.passList = passList;
    }

    public static class Pass {
        private boolean v1_17_40_20_Android;
        private long version;

        private String name;
        private String bitSet = ""; //111111111111111 / 011111010111110 /000000100000000
        private byte unknownByte0;
        private String unknownString0;  //空字符串 / Fallback / DoCheckerboarding

        private boolean unknownBool0;
        private short unknownShort0;
        private List<Pair<String, String>> unknownStringPairList;


        private List<FlagMode> flagModeList;
        private List<Variant> variantList;

        public Pass() {
        }

        public Pass(long version) {
            this.version = version;
        }

        public void readFrom(ByteBuf buf) {
            name = ByteBufUtil.readString(buf);
            if (version >= 0x16) {
                v1_17_40_20_Android = buf.readIntLE() == 15;
                buf.readerIndex(buf.readerIndex() - 4);
            } else {
                v1_17_40_20_Android = false;
            }

            if (v1_17_40_20_Android) {
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
                Variant variant = new Variant(v1_17_40_20_Android);
                variant.readFrom(buf);
                variantList.add(variant);
            }
        }

        public void writeTo(ByteBuf buf) {
            ByteBufUtil.writeString(buf, name);
            if (v1_17_40_20_Android) {
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

        public boolean isV1_17_40_20_Android() {
            return v1_17_40_20_Android;
        }

        public void setV1_17_40_20_Android(boolean v1_17_40_20_Android) {
            this.v1_17_40_20_Android = v1_17_40_20_Android;
        }

        public long getVersion() {
            return version;
        }

        public void setVersion(long version) {
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBitSet() {
            return bitSet;
        }

        public void setBitSet(String bitSet) {
            this.bitSet = bitSet;
        }

        public byte getUnknownByte0() {
            return unknownByte0;
        }

        public void setUnknownByte0(byte unknownByte0) {
            this.unknownByte0 = unknownByte0;
        }

        public String getUnknownString0() {
            return unknownString0;
        }

        public void setUnknownString0(String unknownString0) {
            this.unknownString0 = unknownString0;
        }

        public boolean isUnknownBool0() {
            return unknownBool0;
        }

        public void setUnknownBool0(boolean unknownBool0) {
            this.unknownBool0 = unknownBool0;
        }

        public short getUnknownShort0() {
            return unknownShort0;
        }

        public void setUnknownShort0(short unknownShort0) {
            this.unknownShort0 = unknownShort0;
        }

        public List<Pair<String, String>> getUnknownStringPairList() {
            return unknownStringPairList;
        }

        public void setUnknownStringPairList(List<Pair<String, String>> unknownStringPairList) {
            this.unknownStringPairList = unknownStringPairList;
        }

        public List<FlagMode> getFlagModeList() {
            return flagModeList;
        }

        public void setFlagModeList(List<FlagMode> flagModeList) {
            this.flagModeList = flagModeList;
        }

        public List<Variant> getVariantList() {
            return variantList;
        }

        public void setVariantList(List<Variant> variantList) {
            this.variantList = variantList;
        }
    }

    public static class Variant {
        private boolean v1_17_40_20_Android;

        private boolean unknownBool0;
        private List<FlagMode> flagModeList;
        private List<ShaderCode> shaderCodeList;

        public Variant() {
        }

        public Variant(boolean v1_17_40_20_Android) {
            this.v1_17_40_20_Android = v1_17_40_20_Android;
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
                ShaderCode shaderCode = new ShaderCode(v1_17_40_20_Android);
                shaderCode.readFrom(buf);
                shaderCodeList.add(shaderCode);
//                String fileName = passName + "_Variant_" + l + "_" + shaderCode.getType() + "_" + shaderCode.getPlatform();
//                Main.saveFile(fileName + ".code", shaderCode.getCode());
//                saveShaderCodeInfo(Main.openFile(fileName + "_Info.txt"), shaderCode.getShaderInputList(), shaderCode.getMaterialUniformList(), shaderCode.getUnknownBytes0(), shaderCode.getUnknownBytes1());
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

        public boolean isV1_17_40_20_Android() {
            return v1_17_40_20_Android;
        }

        public void setV1_17_40_20_Android(boolean v1_17_40_20_Android) {
            this.v1_17_40_20_Android = v1_17_40_20_Android;
        }

        public boolean isUnknownBool0() {
            return unknownBool0;
        }

        public void setUnknownBool0(boolean unknownBool0) {
            this.unknownBool0 = unknownBool0;
        }

        public List<FlagMode> getFlagModeList() {
            return flagModeList;
        }

        public void setFlagModeList(List<FlagMode> flagModeList) {
            this.flagModeList = flagModeList;
        }

        public List<ShaderCode> getShaderCodeList() {
            return shaderCodeList;
        }

        public void setShaderCodeList(List<ShaderCode> shaderCodeList) {
            this.shaderCodeList = shaderCodeList;
        }
    }

    public static class ShaderCode {
        private String fileName;
        private boolean v1_17_40_20_Android;

        private String type;
        private String platform;
        private byte unknownByte0; //Vertex 0   Fragment 1   Unknown 3
        private byte unknownByte1;
        private List<ShaderInput> shaderInputList;

        private byte[] unknownBytes0;

        private List<MaterialUniform> materialUniformList;
        private transient byte[] code;
        private byte unknownByte2; //0

        private byte unknownLength0; //1.17 Win10 / 1.16 Win10
        private byte[] unknownBytes1; //1.17 Win10 / 1.16 Win10

        public ShaderCode() {
        }

        public ShaderCode(boolean v1_17_40_20_Android) {
            this.v1_17_40_20_Android = v1_17_40_20_Android;
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

            unknownBytes0 = ByteBufUtil.readBytes(buf, 20);

            short uniformCount = buf.readShortLE();
            materialUniformList = new ArrayList<>(uniformCount);
            for (int i = 0; i < uniformCount; ++i) {
                MaterialUniform materialUniform = new MaterialUniform();
                materialUniform.readFrom(buf);
                materialUniformList.add(materialUniform);
            }

            code = ByteBufUtil.readByteArray(buf);
            unknownByte2 = buf.readByte();

            if (!v1_17_40_20_Android) {
                unknownLength0 = buf.readByte();
                unknownBytes1 = ByteBufUtil.readBytes(buf, 2 + unknownLength0 * 2);
            }
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
            buf.writeShortLE(materialUniformList.size());
            for (MaterialUniform materialUniform : materialUniformList) {
                materialUniform.writeTo(buf);
            }
            ByteBufUtil.writeByteArray(buf, code);
            buf.writeByte(unknownByte2);
            if (!v1_17_40_20_Android) {
                buf.writeByte(unknownLength0);
                buf.writeBytes(unknownBytes1);
            }
        }

        public boolean isV1_17_40_20_Android() {
            return v1_17_40_20_Android;
        }

        public void setV1_17_40_20_Android(boolean v1_17_40_20_Android) {
            this.v1_17_40_20_Android = v1_17_40_20_Android;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public byte getUnknownByte0() {
            return unknownByte0;
        }

        public void setUnknownByte0(byte unknownByte0) {
            this.unknownByte0 = unknownByte0;
        }

        public byte getUnknownByte1() {
            return unknownByte1;
        }

        public void setUnknownByte1(byte unknownByte1) {
            this.unknownByte1 = unknownByte1;
        }

        public List<ShaderInput> getShaderInputList() {
            return shaderInputList;
        }

        public void setShaderInputList(List<ShaderInput> shaderInputList) {
            this.shaderInputList = shaderInputList;
        }

        public byte[] getUnknownBytes0() {
            return unknownBytes0;
        }

        public void setUnknownBytes0(byte[] unknownBytes0) {
            this.unknownBytes0 = unknownBytes0;
        }

        public List<MaterialUniform> getMaterialUniformList() {
            return materialUniformList;
        }

        public void setMaterialUniformList(List<MaterialUniform> materialUniformList) {
            this.materialUniformList = materialUniformList;
        }

        public byte[] getCode() {
            return code;
        }

        public void setCode(byte[] code) {
            this.code = code;
        }

        public byte getUnknownByte2() {
            return unknownByte2;
        }

        public void setUnknownByte2(byte unknownByte2) {
            this.unknownByte2 = unknownByte2;
        }

        public byte getUnknownLength0() {
            return unknownLength0;
        }

        public void setUnknownLength0(byte unknownLength0) {
            this.unknownLength0 = unknownLength0;
        }

        public byte[] getUnknownBytes1() {
            return unknownBytes1;
        }

        public void setUnknownBytes1(byte[] unknownBytes1) {
            this.unknownBytes1 = unknownBytes1;
        }
    }

//    private void saveShaderCodeInfo(Writer writer, List<ShaderInput> shaderInputList, List<MaterialUniform> materialUniformList, byte[] unknownBytes0, byte[] unknownBytes1) {
//        try {
//            writer.write("ShaderInput:\n");
//            writer.write(Util.shaderInputListToString(shaderInputList));
//            writer.write("Uniform:\n");
//            writer.write(Util.uniformListToString(materialUniformList));
//            writer.write("UnknownBytes0:\n");
//            writer.write(Util.byteArrayToString(unknownBytes0));
//            if (!v1_17_40_20_Android) {
//                writer.write("\n");
//                writer.write("UnknownBytes1:\n");
//                writer.write(Util.byteArrayToString(unknownBytes1));
//            }
//            writer.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void savePassInfo(Writer writer, List<FlagMode> flagModeList, String bitSet, String str, byte unknownByte) {
//        try {
//            writer.write("UnknownData:\n");
//            if (v1_17_40_20_Android) {
//                writer.write(bitSet);
//            } else {
//                writer.write(Util.byteToHexString(unknownByte));
//            }
//            writer.write("\n");
//            writer.write(str);
//            writer.write("\n\n");
//            writer.write("FlagMode:\n");
//            writer.write(Util.flagModeListToString(flagModeList));
//            writer.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}


