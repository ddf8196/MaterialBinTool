package com.ddf.materialbintool.materials.definition;

import com.ddf.materialbintool.util.ByteBuf;

public class PropertyField {
    private int num;
    private short type;
    private boolean hasData;
    private float[] vectorData;
    private byte[] matrixData;

    public PropertyField() {
    }

    public void read(ByteBuf buf) {
        type = buf.readShortLE();
        switch (type) {
            case 2: { //Vec4
                num = buf.readIntLE();
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
                num = buf.readIntLE();
                hasData = buf.readBoolean();
                if (hasData) {
                    matrixData = buf.readBytes(36);
                }
                break;
            }
            case 4: { //Mat4
                num = buf.readIntLE();
                hasData = buf.readBoolean();
                if (hasData) {
                    matrixData = buf.readBytes(64);
                }
                break;
            }
            case 5: { //ExternalUniformDeclaration
                break;
            }
        }
    }

    public void write(ByteBuf buf, String name) {
        buf.writeShortLE(type);
        switch (type) {
            case 0: { //Nothing
                String warning = "Nothing set in the property, implausible. See: " + name;
                break;
            }
            case 2: { //Vec4
                buf.writeIntLE(num);
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
                buf.writeIntLE(num);
                buf.writeBoolean(hasData);
                if (hasData) {
                    buf.writeBytes(matrixData);
                }
                break;
            }
            case 5: { //ExternalUniformDeclaration
                break;
            }
        }
    }
}
