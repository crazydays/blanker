package org.crazydays;

import java.io.File;

public class BlankConfiguration {
    private Units units;
    private float length;
    private float tube;
    private float diameter;
    private boolean funnel;

    private float imageZero;
    private String imageFilename;

    public BlankConfiguration(Units units, float length, float tube, float diameter, boolean funnel) {
        this.units = units;
        this.length = length;
        this.tube = tube;
        this.diameter = diameter;
        this.funnel = funnel;
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

    public boolean getFunnel() {
        return funnel;
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

    public File getImagefile() {
        return new File(imageFilename);
    }
}
