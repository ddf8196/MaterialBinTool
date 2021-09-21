package com.ddf.materialbintool;

import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;

import java.util.List;

public class Util {
    static String flagModeListToString(List<FlagMode> flagModeList, boolean comment) {
        if (flagModeList.size() <= 0)
            return "";
        StringBuilder stringBuilder = new StringBuilder(comment ? "//FlagMode\n" : "");
        for (FlagMode flagMode : flagModeList) {
            if (comment)
                stringBuilder.append("//");
            stringBuilder
                    .append(flagMode.str1)
                    .append(" = ")
                    .append(flagMode.str2)
                    .append("\n");
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    static String shaderInputListToString(List<ShaderInput> shaderInputList) {
        if (shaderInputList.size() <= 0)
            return "";
        StringBuilder stringBuilder = new StringBuilder("//ShaderInput\n");
        for (ShaderInput shaderInput : shaderInputList) {
            stringBuilder
                    .append("//name=")
                    .append(shaderInput.name)
                    .append(" type=")
                    .append(shaderInput.type.name())
                    .append(" unknownBytes=")
                    .append(byteArrayToString(shaderInput.unknown4Bytes))
                    .append(" unknownByte1=")
                    .append(byteToHexString(shaderInput.unknownByte1));
            if (shaderInput.unknownByte1 != 0)
                stringBuilder
                        .append(" unknownByte2=")
                        .append(byteToHexString(shaderInput.unknownByte2));
            stringBuilder.append("\n");
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    static String uniformListToString(List<MaterialUniform> uniformList) {
        if (uniformList.size() <= 0)
            return "";
        StringBuilder stringBuilder = new StringBuilder("//Uniform\n");
        for (MaterialUniform materialUniform : uniformList) {
            stringBuilder
                    .append("//name=")
                    .append(materialUniform.name)
                    .append(" type=")
                    .append(materialUniform.type.name())
                    .append(" count=")
                    .append(materialUniform.count)
                    .append(" unknownBytes=")
                    .append(byteArrayToString(materialUniform.unknown4Bytes))
                    .append("\n");
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    static String byteArrayToString(byte[] array) {
        StringBuilder stringBuilder = new StringBuilder("[");
        boolean first = true;
        for (byte b : array) {
            if (!first)
                stringBuilder.append(" ");
            else
                first = false;
            stringBuilder.append(byteToHexString(b));
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    static String byteToHexString(byte b) {
        String str = Integer.toHexString(b & 0xFF);
        if (str.length() < 2)
            str = "0" + str;
        return str;
    }

    public static byte[] decrypt(byte[] key, byte[] iv, byte[] encrypted) {
        byte[] decrypted = new byte[encrypted.length];

        byte[] tmp = new byte[12];
        System.arraycopy(iv, 0, tmp, 0, tmp.length);
        iv = tmp;

        try {
            KeyParameter keyParameter = new KeyParameter(key);
            AEADParameters aeadParameters = new AEADParameters(keyParameter, 96, iv);

            GCMBlockCipher gcmBlockCipher = new GCMBlockCipher(new AESEngine());
            gcmBlockCipher.init(false, aeadParameters);
            gcmBlockCipher.processBytes(encrypted, 0, encrypted.length, decrypted, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decrypted;
    }
}
