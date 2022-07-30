package com.ddf.materialbintool.main.util;

import java.util.AbstractList;

public class ByteArrayList extends AbstractList<Byte> {
    private final byte[] array;

    public ByteArrayList(byte[] array) {
        this.array = array;
    }

    @Override
    public Byte get(int index) {
        return array[index];
    }

    @Override
    public int size() {
        return array.length;
    }
}
