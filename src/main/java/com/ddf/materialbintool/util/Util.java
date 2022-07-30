package com.ddf.materialbintool.util;

import com.ddf.materialbintool.thirdparty.bouncycastle.crypto.engines.AESEngine;
import com.ddf.materialbintool.thirdparty.bouncycastle.crypto.modes.GCMBlockCipher;
import com.ddf.materialbintool.thirdparty.bouncycastle.crypto.params.AEADParameters;
import com.ddf.materialbintool.thirdparty.bouncycastle.crypto.params.KeyParameter;

import java.util.Arrays;

public class Util {
    public static byte[] decrypt(byte[] key, byte[] iv, byte[] ciphertext) {
        byte[] result = new byte[ciphertext.length];
        try {
            KeyParameter keyParameter = new KeyParameter(key);
            AEADParameters aeadParameters = new AEADParameters(keyParameter, 96, Arrays.copyOf(iv, 12));

            GCMBlockCipher gcmBlockCipher = new GCMBlockCipher(new AESEngine());
            gcmBlockCipher.init(false, aeadParameters);
            gcmBlockCipher.processBytes(ciphertext, 0, ciphertext.length, result, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static byte[] encrypt(byte[] key, byte[] iv, byte[] plaintext) {
        byte[] result = new byte[plaintext.length];
        try {
            KeyParameter keyParameter = new KeyParameter(key);
            AEADParameters aeadParameters = new AEADParameters(keyParameter, 96, Arrays.copyOf(iv, 12));

            GCMBlockCipher gcmBlockCipher = new GCMBlockCipher(new AESEngine());
            gcmBlockCipher.init(true, aeadParameters);
            gcmBlockCipher.processBytes(plaintext, 0, plaintext.length, result, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
