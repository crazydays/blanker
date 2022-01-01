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

    public Facet(Vertex a, Vertex b, Vertex c, Integer attributeByteCount) {
        this.normal = calculateUnitNormal(a, b, c);
        this.a = a;
        this.b = b;
        this.c = c;
        this.attributeByteCount = attributeByteCount;
    }

    private Normal calculateUnitNormal(Vertex a, Vertex b, Vertex c) {
        Vector v1 = new Vector(a, c);
        Vector v2 = new Vector(b, c);
        return new UnitNormal(v1.getY() * v2.getZ() - v1.getZ() * v2.getY(), v1.getZ() * v2.getX() - v1.getX() * v2.getZ(), v1.getX() * v2.getY() - v1.getY() * v2.getX());
    }

    public Normal getNormal() {
        return normal;
    }

    public Vertex getA() {
        return a;
    }

    public Vertex getB() {
        return b;
    }

    public Vertex getC() {
        return c;
    }

    public Integer getAttributeByeCount() {
        return attributeByteCount;
    }
    public byte[] getBytes() {
        return ByteBuffer.allocate(50).put(normal.getBytes()).put(a.getBytes()).put(b.getBytes()).put(c.getBytes()).order(ByteOrder.LITTLE_ENDIAN).putShort(attributeByteCount.shortValue()).array();
    }

    @Override
    public String toString() {
        return String.format("%s\n\tnormal: %s\n\ta: %s\n\tb: %s\n\tc: %s\n\tattributeByteCount: %d", getClass().getSimpleName(), getNormal(), getA(), getB(), getC(), getAttributeByeCount());
    }
}
