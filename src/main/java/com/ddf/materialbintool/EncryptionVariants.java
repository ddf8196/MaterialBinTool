package com.ddf.materialbintool;

public enum EncryptionVariants {
    None(0x4E4F4E45), //NONE
    SimplePassphrase(0x534D504C), //SMPL
    KeyPair(0x4B595052), //KYPR
    Unknown(0);

    private final int signature;

    EncryptionVariants(int signature) {
        this.signature = signature;
    }

    public int getSignature() {
        return signature;
    }

    public static EncryptionVariants getBySignature(int value) {
        for (EncryptionVariants encryptionVariants : values()) {
            if (encryptionVariants.signature == value)
                return encryptionVariants;
        }
        return Unknown;
    }
}
