package org.crazydays.stl;

import java.nio.ByteBuffer;

public class Header {
    private String application;
    private String name;

    public Header(String application, String name) {
        this.application = application;
        this.name = name;
    }

    private String _getHeaderString() {
        return String.format("%s,name:%s", application, name).substring(0, 80);
    }

    public byte[] getBytes() {
        return ByteBuffer.allocate(80).put(_getHeaderString().getBytes()).array();
    }
}
