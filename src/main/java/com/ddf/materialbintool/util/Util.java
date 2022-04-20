package com.ddf.materialbintool.util;

import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;

public class Util {
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
