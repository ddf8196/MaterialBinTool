package com.ddf.materialbintool;

import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
    private Map<String, SamplerDefinition> samplerDefinitionMap = new HashMap<>();

    private boolean v1_17_40_20_Android;

    public void loadFrom(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        long magic = buf.getLong();
        if (magic != MAGIC)
            return;
		if (!ByteBufferUtil.getString(buf).equals(COMPILED_MATERIAL_DEFINITION))
			return;
        version = buf.getLong();
        if (version >= 0x16)
            encryptionVariant = EncryptionVariants.getBySignature(buf.getInt());
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
                byte[] key = ByteBufferUtil.getByteArray(buf);
                if (!Arrays.equals(key, digest))
                    return;
                byte[] iv = ByteBufferUtil.getByteArray(buf);
                byte[] encrypted = ByteBufferUtil.getByteArray(buf);
				ByteBuffer decrypted = ByteBuffer.wrap(Util.decrypt(key, iv, encrypted));
                decrypted.order(ByteOrder.LITTLE_ENDIAN);

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

    private boolean loadContent(ByteBuffer buf) {
        name = ByteBufferUtil.getString(buf);
        boolean unknownBool0 = ByteBufferUtil.getBoolean(buf);
        String unknownString0 = null;
        if (!unknownBool0) {

        } else {
            unknownString0 = ByteBufferUtil.getString(buf);
        }

        int samplerDefinitionCount = buf.get();
        for (int i = 0; i < samplerDefinitionCount; i++) {
            String name = ByteBufferUtil.getString(buf);
            byte byte0 = buf.get();
			byte byte1 = buf.get();
			byte byte2 = buf.get();
			boolean bool0 = ByteBufferUtil.getBoolean(buf);
			byte byte3 = buf.get();

			String str1 = ByteBufferUtil.getString(buf);
			boolean bool1 = ByteBufferUtil.getBoolean(buf);
            String str2 = "";
			if (bool1) {
                str2 = ByteBufferUtil.getString(buf);
            }
			boolean bool2 = ByteBufferUtil.getBoolean(buf);
            String str3 = "";
            int int0 = 0;
			if (bool2) {
			    str3 = ByteBufferUtil.getString(buf);
			    int0 = buf.getInt();
            }
			SamplerDefinition samplerDefinition = new SamplerDefinition(name, byte0, byte1, byte2, bool0, byte3, str1, bool1, str2, bool2, str3, int0);
			samplerDefinitionMap.put(name, samplerDefinition);
        }

        short unknownCount0 = buf.getShort();
        for (int i = 0; i < unknownCount0; i++) {
            String str0 = ByteBufferUtil.getString(buf);

            short short1 = buf.getShort();
            switch (short1) {
                case 2: {
                    int int0 = buf.getInt();
                    boolean bool0 = ByteBufferUtil.getBoolean(buf);
                    float float0 = 0;
                    float float1 = 0;
                    float float2 = 0;
                    float float3 = 0;

                    if (bool0) {
                        float0 = buf.getFloat();
                        float1 = buf.getFloat();
                        float2 = buf.getFloat();
                        float3 = buf.getFloat();
                    }
                    break;
                }
                case 3: {
                    int int0 = buf.getInt();
                    boolean bool0 = ByteBufferUtil.getBoolean(buf);
                    byte[] bytes0 = null;
                    if (bool0) {
                        bytes0 = ByteBufferUtil.getBytes(buf, 36);
                    }
                    break;
                }
                case 4: {
                    int int0 = buf.getInt();
                    boolean bool0 = ByteBufferUtil.getBoolean(buf);
                    byte[] bytes0 = null;
                    if (bool0) {
                        bytes0 = ByteBufferUtil.getBytes(buf, 64);
                    }
                    break;
                }
                case 5: {
                    break;
                }
            }
        }

        short passCount = buf.getShort();
        for (int i = 0; i < passCount; ++i) {
            String passName = ByteBufferUtil.getString(buf);
            buf.mark();
            v1_17_40_20_Android = buf.getInt() == 15;
            buf.reset();

            String bitSet = ""; //111111111111111 / 011111010111110 /000000100000000
            String str2;  //空字符串 / Fallback / DoCheckerboarding
            byte unknownByte0 = 0;

            if (v1_17_40_20_Android) {
                bitSet = ByteBufferUtil.getString(buf);
                str2 = ByteBufferUtil.getString(buf);
            } else {
                unknownByte0 = buf.get();
                str2 = ByteBufferUtil.getString(buf);
            }

            boolean bool0 = ByteBufferUtil.getBoolean(buf);
            short short0 = 0;
            short unknownCount2 = 0;
            if (bool0) {
                short0 = buf.getShort();
                unknownCount2 = buf.getShort();
            }
            for (int j = 0; j < unknownCount2; ++j) {
                String str3 = ByteBufferUtil.getString(buf);
                String str4 = ByteBufferUtil.getString(buf);
            }

            short flagModeCount = buf.getShort();
            List<FlagMode> flagModeList = readFlagModes(buf, flagModeCount);

            savePassInfo(Main.openFile(i + "_" + passName + "_Info.txt"), flagModeList, bitSet, str2, unknownByte0);

            short variantCount = buf.getShort();
            for (int l = 0; l < variantCount; ++l) {
                boolean bool = ByteBufferUtil.getBoolean(buf);
                short shaderFlagModeCount = buf.getShort();
                short shaderCodeCount = buf.getShort(); //2
                List<FlagMode> shaderFlagModeList = readFlagModes(buf, shaderFlagModeCount);

                for (int j = 0; j < shaderCodeCount; ++j) {
                    String shaderCodeType = ByteBufferUtil.getString(buf); //Vertex / Fragment / Unknown
                    String shaderCodePlatform = ByteBufferUtil.getString(buf); //
                    byte byte0 = buf.get(); //Vertex 0   Fragment 1   Unknown 3
                    byte byte1 = buf.get(); //8 (?)
                    List<ShaderInput> shaderInputList = readShaderInputList(buf);

                    byte[] unknown20Bytes = ByteBufferUtil.getBytes(buf, 20);

                    List<MaterialUniform> materialUniformList = readMaterialUniformList(buf);
                    byte[] shaderCode = ByteBufferUtil.getByteArray(buf);
                    byte byte2 = buf.get(); //0

                    byte unknownLength0; //1.17.10
                    short unknownShort0; //1.17.10
                    byte[] unknownBytes = null; //1.17.10

                    if (!v1_17_40_20_Android) {
                        unknownLength0 = buf.get();
                        unknownBytes = ByteBufferUtil.getBytes(buf, 2 + unknownLength0 * 2);
                    }

                    String fileName = i + "_" + passName + "_Variant_" + l + "_" + shaderCodeType + "_" + shaderCodePlatform;
                    Main.saveFile(fileName + ".code", shaderCode);
                    saveShaderCodeInfo(Main.openFile(fileName + "_Info.txt"), shaderInputList, materialUniformList, unknown20Bytes, unknownBytes);
                }
                Main.saveFile(i + "_" + passName + "_Variant_" + l + "_FlagMode.txt", Util.flagModeListToString(shaderFlagModeList));
            }
        }
        long end = buf.getLong();
        return end == MAGIC;
    }

    public void saveTo() {

    }

    private void savePassInfo(Writer writer, List<FlagMode> flagModeList, String bitSet, String str, byte unknownByte) {
        try {
            writer.write("UnknownData:\n");
            if (v1_17_40_20_Android) {
                writer.write(bitSet);
            } else {
                writer.write(Util.byteToHexString(unknownByte));
            }
            writer.write("\n");
            writer.write(str);
            writer.write("\n\n");
            writer.write("FlagMode:\n");
            writer.write(Util.flagModeListToString(flagModeList));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveShaderCodeInfo(Writer writer, List<ShaderInput> shaderInputList, List<MaterialUniform> materialUniformList, byte[] unknownBytes0, byte[] unknownBytes1) {
        try {
            writer.write("ShaderInput:\n");
            writer.write(Util.shaderInputListToString(shaderInputList));
            writer.write("Uniform:\n");
            writer.write(Util.uniformListToString(materialUniformList));
            writer.write("UnknownBytes0:\n");
            writer.write(Util.byteArrayToString(unknownBytes0));
            if (!v1_17_40_20_Android) {
                writer.write("\n");
                writer.write("UnknownBytes1:\n");
                writer.write(Util.byteArrayToString(unknownBytes1));
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static List<FlagMode> readFlagModes(ByteBuffer buf, int count) {
        List<FlagMode> flagModeList = new ArrayList<>(count);
        for (int j = 0; j < count; ++j) {
            FlagMode flagMode = new FlagMode(ByteBufferUtil.getString(buf), ByteBufferUtil.getString(buf));
            flagModeList.add(flagMode);
        }
        return flagModeList;
    }

    static List<ShaderInput> readShaderInputList(ByteBuffer buf) {
        short shaderInputCount = buf.getShort();
        List<ShaderInput> shaderInputList = new ArrayList<>(shaderInputCount);
        for (int k = 0; k < shaderInputCount; ++k) {
            String shaderInputName = ByteBufferUtil.getString(buf);
            ShaderInputType type = ShaderInputType.get(buf.get());
            byte[] unknown4Bytes = ByteBufferUtil.getBytes(buf, 4);
            byte unknownByte1 = buf.get();
            byte unknownByte2 = 0;
            if (unknownByte1 != 0) {
                unknownByte2 = buf.get();
            }
            ShaderInput shaderInput = new ShaderInput(shaderInputName, type, unknown4Bytes, unknownByte1, unknownByte2);
            shaderInputList.add(shaderInput);
        }
        return shaderInputList;
    }

    static List<MaterialUniform> readMaterialUniformList(ByteBuffer buf) {
        short uniformCount = buf.getShort();
        List<MaterialUniform> materialUniformList = new ArrayList<>(uniformCount);
        for (int k = 0; k < uniformCount; ++k) {
            byte nameLength = buf.get();
            String name = new String(ByteBufferUtil.getBytes(buf, nameLength), StandardCharsets.UTF_8);
            //UniformType type = UniformType.get(buf.get());
            byte type = buf.get();
            byte count = buf.get();
            MaterialUniform materialUniform = new MaterialUniform(name, type, count, ByteBufferUtil.getBytes(buf, 4));
            materialUniformList.add(materialUniform);
        }
        return materialUniformList;
    }
}


