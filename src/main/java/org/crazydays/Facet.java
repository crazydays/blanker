package org.crazydays;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/*
 * https://www.fabbers.com/tech/STL_Format#Sct_binary
 */
public class Facet {
    private Normal normal;
    private Vertex a;
    private Vertex b;
    private Vertex c;
    private Integer attributeByteCount;

    public Facet(Normal normal, Vertex a, Vertex b, Vertex c, Integer attributeByteCount) {
        this.normal = normal;
        this.a = a;
        this.b = b;
        this.c = c;
        this.attributeByteCount = attributeByteCount;
    }

    public byte[] getBytes() {
        return ByteBuffer.allocate(50).put(normal.getBytes()).put(a.getBytes()).put(b.getBytes()).put(c.getBytes()).order(ByteOrder.LITTLE_ENDIAN).putShort(attributeByteCount.shortValue()).array();
    }
}
