package com.ddf.materialbintool.materials.definition;

import com.ddf.materialbintool.util.ByteBuf;

public class ShaderInput {
    public ShaderInputType type;
    public SemanticDescription semantic;
    public boolean isPerInstance;

    public ShaderInput() {
    }

    public void read(ByteBuf buf) {
        type = ShaderInputType.get(buf.readByte());
        semantic = new SemanticDescription();
        semantic.read(buf);
        isPerInstance = buf.readBoolean();
    }

    public void write(ByteBuf buf) {
        buf.writeByte(type.ordinal());
        semantic.write(buf);
        buf.writeBoolean(isPerInstance);
    }

    @Override
    public String toString() {
        return "ShaderInput{" +
                "type=" + type +
                ", semantic=" + semantic +
                ", isPerInstance=" + isPerInstance +
                '}';
    }
}
