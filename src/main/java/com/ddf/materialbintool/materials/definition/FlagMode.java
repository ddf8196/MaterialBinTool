package com.ddf.materialbintool.materials.definition;

import com.ddf.materialbintool.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;

public class FlagMode {
    private String key;
    private String value;

    public FlagMode() {
    }

    public void readFrom(ByteBuf buf) {
        key = ByteBufUtil.readString(buf);
        value = ByteBufUtil.readString(buf);
    }

    public void writeTo(ByteBuf buf) {
        ByteBufUtil.writeString(buf, key);
        ByteBufUtil.writeString(buf, value);
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
    public String toString() {
        return "FlagMode{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
