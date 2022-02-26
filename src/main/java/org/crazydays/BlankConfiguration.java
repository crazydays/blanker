package org.crazydays;

import java.io.File;

public class BlankConfiguration {
    private final Units units;
    private final float length;
    private final float tube;
    private final float diameter;
    private final boolean center;
    private final float funnel;
    private final float roundMold;
    private final float squareMold;
    private final boolean positive;
    private float wallThickness;
    private float imageZero;
    private String imageFilename;

    public BlankConfiguration(Units units, float length, float tube, float diameter, boolean center, float funnel, float roundMold, float squareMold, boolean positive, float wallThickness) {
        this.units = units;
        this.length = length;
        this.tube = tube;
        this.diameter = diameter;
        this.center = center;
        this.funnel = funnel;
        this.roundMold = roundMold;
        this.squareMold = squareMold;
        this.positive = positive;
        this.wallThickness = wallThickness;
    }

    protected Units getUnits() {
        return units;
    }

    public float getLength() {
        switch (getUnits()) {
            case INCHES:
                return length * 25.4f;
            case MILLIMETERS:
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
            default:
                return diameter;
        }
    }

    public boolean isCenter() {
        return center;
    }

    public float getFunnel() {
        return funnel;
    }

    public boolean isFunnel() {
        return getFunnel() > 0.0f;
    }

    public float getRoundMold() {
        return roundMold;
    }

    public boolean isRoundMold() {
        return roundMold > 0.0f;
    }

    public float getSquareMold() {
        return squareMold;
    }

    public boolean isSquareMold() {
        return squareMold > 0.0f;
    }

    public boolean isPositive() {
        if (isRoundMold()) {
            return false;
        } else {
            return positive;
        }
    }

    public float getWallThickness() {
        return wallThickness;
    }

    public boolean isWallThickness() {
        return wallThickness > 0.0f;
    }

    public void setImageZero(float imageZero) {
        this.imageZero = imageZero;
    }

    public float getImageZero() {
        switch (units) {
            case INCHES:
                return imageZero * 25.4f;
            case MILLIMETERS:
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
