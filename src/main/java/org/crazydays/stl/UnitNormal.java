package org.crazydays.stl;

public class UnitNormal extends Normal {
    public UnitNormal(float x, float y, float z) {
        super(_unitize(x, x, y, z), _unitize(y, x, y, z), _unitize(z, x, y, z));
    }

    private static float _unitize(float value, float x, float y, float z) {
        return (float) (value / Math.sqrt(x * x + y * y + z * z));
    }
}
