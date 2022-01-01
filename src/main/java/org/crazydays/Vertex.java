package org.crazydays;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Vertex {
    private float x;
    private float y;
    private float z;

    public Vertex(float x, float y, float z) {
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
}
