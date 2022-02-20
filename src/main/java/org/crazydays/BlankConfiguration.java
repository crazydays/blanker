package org.crazydays;

import java.io.File;

public class BlankConfiguration {
    private final Units units;
    private final float length;
    private final float tube;
    private final float diameter;
    private final boolean center;
    private final boolean funnel;
    private final boolean mold;
    private float imageZero;
    private String imageFilename;

    public BlankConfiguration(Units units, float length, float tube, float diameter, boolean center, boolean funnel, boolean mold) {
        this.units = units;
        this.length = length;
        this.tube = tube;
        this.diameter = diameter;
        this.center = center;
        this.funnel = funnel;
        this.mold = mold;
    }

    public Units getUnits() {
        return units;
    }

    public float getLength() {
        switch (units) {
            case INCHES:
                return length * 25.4f;
            case MILLIMETERS:
                return length;
            default:
                return length;
        }
    }

    public float getTube() {
        switch (units) {
            case INCHES:
                return tube * 25.4f;
            case MILLIMETERS:
                return tube;
            default:
                return tube;
        }
    }

    public float getDiameter() {
        switch (units) {
            case INCHES:
                return diameter * 25.4f;
            case MILLIMETERS:
                return diameter;
            default:
                return diameter;
        }
    }

    public boolean isCenter() {
        return center;
    }

    public boolean isFunnel() {
        return funnel;
    }

    public boolean isMold() {
        return mold;
    }

    public void setImageZero(float imageZero) {
        this.imageZero = imageZero;
    }

    public float getImageZero() {
        switch (units) {
            case INCHES:
                return imageZero * 25.4f;
            case MILLIMETERS:
                return imageZero;
            default:
                return imageZero;
        }
    }

    public void setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
    }

    public File getImageFile() {
        return new File(imageFilename);
    }
}
