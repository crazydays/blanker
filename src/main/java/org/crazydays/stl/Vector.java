package org.crazydays.stl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Vector implements Comparable<Vector> {
    private float x;
    private float y;
    private float z;

    public Vector(Vertex a, Vertex b) {
        this.x = b.getX() - a.getX();
        this.y = b.getY() - a.getY();
        this.z = b.getZ() - a.getZ();
    }

    public Vector(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    byte[] getBytes() {
        return ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN).putFloat(x).putFloat(y).putFloat(z).array();
    }

    @Override
    public String toString() {
        return String.format("%s x: %f, y: %f, z: %f", getClass().getSimpleName(), getX(), getY(), getZ());
    }

    @Override
    public int compareTo(Vector o) {
        switch (Float.compare(x, o.x)) {
            case 0:
                switch (Float.compare(y, o.y)) {
                    case 0:
                        return Float.compare(z, o.z);
                    case -1:
                        return -1;
                    case 1:
                        return 1;
                    default:
                        throw new UnsupportedOperationException("");
                }
            case -1:
                return -1;
            case 1:
                return 1;
            default:
                throw new UnsupportedOperationException("");
        }
    }
}
