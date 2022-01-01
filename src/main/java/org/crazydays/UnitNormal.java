package org.crazydays;

public class UnitNormal extends Normal {
    public UnitNormal(float x, float y, float z) {
        super((float) (x / Math.sqrt(x * x + y * y + z * z)), (float) (y / Math.sqrt(x * x + y * y + z * z)), (float) (z / Math.sqrt(x * x + y * y + z * z)));
    }
}
