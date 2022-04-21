package com.ddf.materialbintool.materials.definition;

import com.ddf.materialbintool.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;


public class PropertyField {
    private String name;
    private int unknownInt0;
    private short type;
    private boolean hasData;
    private float[] vectorData;
    private byte[] matrixData;

    public PropertyField() {
    }

    public void readFrom(ByteBuf buf) {
        name = ByteBufUtil.readString(buf);

        type = buf.readShortLE();
        switch (type) {
            case 2: { //Vec4
                unknownInt0 = buf.readIntLE();
                hasData = buf.readBoolean();
                if (hasData) {
                    vectorData = new float[] {
                            buf.readFloatLE(),
                            buf.readFloatLE(),
                            buf.readFloatLE(),
                            buf.readFloatLE()
                    };
                }
                break;
            }
            case 3: { //Mat3
                unknownInt0 = buf.readIntLE();
                hasData = buf.readBoolean();
                if (hasData) {
                    matrixData = ByteBufUtil.readBytes(buf, 36);
                }
                break;
            }
            case 4: { //Mat4
                unknownInt0 = buf.readIntLE();
                hasData = buf.readBoolean();
                if (hasData) {
                    matrixData = ByteBufUtil.readBytes(buf, 64);
                }
                break;
            }
            case 5: { //ExternalUniformDeclaration
                break;
            }
        }
    }

    public void writeTo(ByteBuf buf) {
        ByteBufUtil.writeString(buf, name);
        buf.writeShortLE(type);
        switch (type) {
            case 2: { //Vec4
                buf.writeIntLE(unknownInt0);
                buf.writeBoolean(hasData);
                if (hasData) {
                    for (float f : vectorData) {
                        buf.writeFloatLE(f);
                    }
                }
                break;
            }
            case 3:   //Mat3
            case 4: { //Mat4
                buf.writeIntLE(unknownInt0);
                buf.writeBoolean(hasData);
                if (hasData) {
                    ByteBufUtil.writeByteArray(buf, matrixData);
                }
                break;
            }
            case 5: { //ExternalUniformDeclaration
                break;
            }
        }
    }
}
