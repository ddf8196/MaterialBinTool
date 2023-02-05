package com.ddf.materialbintool.materials.definition;

import com.ddf.materialbintool.util.ByteBuf;

import java.util.Objects;

public class FlagMode {
    private String key;
    private String value;

    public FlagMode() {
    }

    public void read(ByteBuf buf) {
        key = buf.readStringLE();
        value = buf.readStringLE();
    }

    public void write(ByteBuf buf) {
        buf.writeStringLE(key);
        buf.writeStringLE(value);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlagMode flagMode = (FlagMode) o;
        return Objects.equals(key, flagMode.key) && Objects.equals(value, flagMode.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "FlagMode{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
