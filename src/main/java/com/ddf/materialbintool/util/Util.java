package com.ddf.materialbintool.util;

import com.ddf.materialbintool.definition.FlagMode;
import com.ddf.materialbintool.definition.ShaderInput;
import com.ddf.materialbintool.materials.MaterialUniform;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;

import java.util.List;

public class Util {
    public static String flagModeListToString(List<FlagMode> flagModeList) {
        if (flagModeList.size() <= 0)
            return "";
        StringBuilder stringBuilder = new StringBuilder();
        for (FlagMode flagMode : flagModeList) {
            stringBuilder
                    .append(flagMode.getKey())
                    .append("=")
                    .append(flagMode.getValue())
                    .append("\n");
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    public static String shaderInputListToString(List<ShaderInput> shaderInputList) {
        if (shaderInputList.size() <= 0)
            return "";
        StringBuilder stringBuilder = new StringBuilder();
        for (ShaderInput shaderInput : shaderInputList) {
            stringBuilder
                    .append("name=")
                    .append(shaderInput.getName())
                    .append(" type=")
                    .append(shaderInput.getType().name())
                    .append(" unknownBytes0=")
                    .append(byteArrayToString(shaderInput.getUnknownBytes0()))
                    .append(" unknownBool1=")
                    .append(shaderInput.isUnknownBool1());
            if (shaderInput.isUnknownBool1())
                stringBuilder
                        .append(" unknownByte0=")
                        .append(byteToHexString(shaderInput.getUnknownByte0()));
            stringBuilder.append("\n");
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    public static String uniformListToString(List<MaterialUniform> uniformList) {
        if (uniformList.size() <= 0)
            return "";
        StringBuilder stringBuilder = new StringBuilder();
        for (MaterialUniform materialUniform : uniformList) {
            stringBuilder
                    .append("name=")
                    .append(materialUniform.getName())
                    .append(" type=")
                    .append(materialUniform.getType())
                    .append(" count=")
                    .append(materialUniform.getCount())
                    .append(" unknownBytes=")
                    .append(byteArrayToString(materialUniform.getUnknownBytes0()))
                    .append("\n");
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    public static String byteArrayToString(byte[] array) {
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

    public static String byteToHexString(byte b) {
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
