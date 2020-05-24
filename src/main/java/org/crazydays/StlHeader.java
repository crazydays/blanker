package org.crazydays;

import java.nio.ByteBuffer;

public class StlHeader {
    public byte[] getBytes() {
        return ByteBuffer.allocate(80).put("Blanker".getBytes()).array();
    }
}
