package com.ddf.materialbintool.materials.definition;

import com.ddf.materialbintool.util.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class BadgerUniformBlock {
    public String name;
    public short size;
    public byte reg;
    public List<Member> members;

    public void read(ByteBuf buf) {
        name = buf.readStringLE();
        size = buf.readShortLE();
        reg = buf.readByte();

        short count = buf.readShortLE();
        members = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Member member = new Member();
            member.read(buf);
            members.add(member);
        }
    }

    public void write(ByteBuf buf) {
        buf.writeStringLE(name);
        buf.writeShortLE(size);
        buf.writeByte(reg);

        buf.writeShortLE(members.size());
        for (Member member : members) {
            member.write(buf);
        }
    }

    public static class Member {
        public String name;
        public byte type;
        public short count;

        public void read(ByteBuf buf) {
            name = buf.readStringLE();
            type = buf.readByte();
            count = buf.readShortLE();
        }

        public void write(ByteBuf buf) {
            buf.writeStringLE(name);
            buf.writeByte(type);
            buf.writeShortLE(count);
        }
    }
}
