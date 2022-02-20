package org.crazydays;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class Blanker {
    public final static float Z_STEP = 0.04f;

    public final static float MOLD_THICKNESS = 2.0f;
    public final static float MOLD_WIDTH = 25.0f;
    public final static float MOLD_BOTTOM_PADDING = 5.0f;
    public final static float MOLD_TOP_PADDING = 10.0f;

    public final static float CENTER_DEPTH = 5.0f;
    public final static float FUNNEL_DEPTH = 5.0f;
    public final static float FUNNEL_HEIGHT = 10.0f;
    public final static float FUNNEL_RADIUS = 9.96f;

    private final BlankConfiguration blankConfiguration;
    private final Blank blank;

    private BufferedImage image;

    private final String outputFilename;

    private float[] tubeX;
    private float[] tubeY;
    private float[] imageX;
    private float[] imageY;
    private float[] funnelX;
    private float[] funnelY;

    public Blanker(BlankConfiguration blankConfiguration, String outputFilename) {
        this.blankConfiguration = blankConfiguration;
        this.blank = new Blank();
        this.blank.setHeader(new StlHeader());
        this.outputFilename = outputFilename;
    }

    public void loadImage() {
        try {
            image = ImageIO.read(blankConfiguration.getImageFile());
            System.out.format("image height: %d width: %d\n", image.getHeight(), image.getWidth());
        } catch (IOException e) {
            System.err.println("IOException: " + e.getLocalizedMessage());
        }
    }

    public void generateStl() {
        System.out.format("rotation_steps: %d\n", rotationSteps());

        generateXYDimensions();

        if (blankConfiguration.isMold()) {
            generateMold();
        }

        generateTop();
        generateShaft();
        generateBottom();
    }

    private void generateXYDimensions() {
        System.out.println("dimensions");
        tubeX = new float[rotationSteps()];
        tubeY = new float[rotationSteps()];
        imageX = new float[rotationSteps()];
        imageY = new float[rotationSteps()];
        funnelX = new float[rotationSteps()];
        funnelY = new float[rotationSteps()];

        for (int i = 0; i < rotationSteps(); i++) {
            tubeX[i] = (float) Math.sin((2 * Math.PI) / rotationSteps() * i) * tubeRadius();
            tubeY[i] = (float) Math.cos((2 * Math.PI) / rotationSteps() * i) * tubeRadius();
            imageX[i] = (float) Math.sin((2 * Math.PI) / rotationSteps() * (i + 1)) * imageRadius();
            imageY[i] = (float) Math.cos((2 * Math.PI) / rotationSteps() * (i + 1)) * imageRadius();
            funnelX[i] = (float) Math.sin((2 * Math.PI) / rotationSteps() * i) * FUNNEL_RADIUS;
            funnelY[i] = (float) Math.cos((2 * Math.PI) / rotationSteps() * i) * FUNNEL_RADIUS;

            System.out.format(
                    "tube (x, y) => (%.3f, %.3f), image (x, y) => (%.3f, %.3f)\n",
                    tubeX[i], tubeY[i], imageX[i], imageY[i]);
       }
    }

    private void generateMold() {
        System.out.println("generating mold");

        // back
        blank.addFacet(new Facet(
                new Vertex(-MOLD_THICKNESS, MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                new Vertex(-MOLD_THICKNESS, -MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                new Vertex(-MOLD_THICKNESS, -MOLD_WIDTH, blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                0));
        blank.addFacet(new Facet(
                new Vertex(-MOLD_THICKNESS, MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                new Vertex(-MOLD_THICKNESS, -MOLD_WIDTH, blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                new Vertex(-MOLD_THICKNESS, MOLD_WIDTH, blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                0));

        // left
        blank.addFacet(new Facet(
                new Vertex(0.0f, -MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                new Vertex(-MOLD_THICKNESS, -MOLD_WIDTH, blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                new Vertex(-MOLD_THICKNESS, -MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                0));
        blank.addFacet(new Facet(
                new Vertex(0.0f, -MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                new Vertex(0.0f, -MOLD_WIDTH, blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                new Vertex(-MOLD_THICKNESS, -MOLD_WIDTH, blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                0));

        // top
        blank.addFacet(new Facet(
                new Vertex(0.0f, -MOLD_WIDTH, blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                new Vertex(-MOLD_THICKNESS, MOLD_WIDTH, blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                new Vertex(-MOLD_THICKNESS, -MOLD_WIDTH, blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                0));
        blank.addFacet(new Facet(
                new Vertex(0.0f, -MOLD_WIDTH, blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                new Vertex(0.0f, MOLD_WIDTH, blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                new Vertex(-MOLD_THICKNESS, MOLD_WIDTH, blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                0));

        // right
        blank.addFacet(new Facet(
                new Vertex(0.0f, MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                new Vertex(-MOLD_THICKNESS, MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                new Vertex(-MOLD_THICKNESS, MOLD_WIDTH, blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                0));
        blank.addFacet(new Facet(
                new Vertex(0.0f, MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                new Vertex(-MOLD_THICKNESS, MOLD_WIDTH, blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                new Vertex(0.0f, MOLD_WIDTH, blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                0));

        // bottom
        blank.addFacet(new Facet(
                new Vertex(MOLD_WIDTH, -MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                new Vertex(-MOLD_THICKNESS, -MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                new Vertex(-MOLD_THICKNESS, MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                0));
        blank.addFacet(new Facet(
                new Vertex(MOLD_WIDTH, -MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                new Vertex(-MOLD_THICKNESS, MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                new Vertex(MOLD_WIDTH, MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                0));

        // bottom left
        blank.addFacet(new Facet(
                new Vertex(0.0f, -MOLD_WIDTH, -MOLD_BOTTOM_PADDING),
                new Vertex(0.0f, -MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                new Vertex(MOLD_WIDTH, -MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                0));
        blank.addFacet(new Facet(
                new Vertex(0.0f, -MOLD_WIDTH, -MOLD_BOTTOM_PADDING),
                new Vertex(MOLD_WIDTH, -MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                new Vertex(MOLD_WIDTH, -MOLD_WIDTH, -MOLD_BOTTOM_PADDING),
                0));


        // bottom front
        blank.addFacet(new Facet(
                new Vertex(MOLD_WIDTH, -MOLD_WIDTH, -MOLD_BOTTOM_PADDING),
                new Vertex(MOLD_WIDTH, -MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                new Vertex(MOLD_WIDTH, MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                0));
        blank.addFacet(new Facet(
                new Vertex(MOLD_WIDTH, -MOLD_WIDTH, -MOLD_BOTTOM_PADDING),
                new Vertex(MOLD_WIDTH, MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                new Vertex(MOLD_WIDTH, MOLD_WIDTH, -MOLD_BOTTOM_PADDING),
                0));

        // bottom right
        blank.addFacet(new Facet(
                new Vertex(0.0f, MOLD_WIDTH, -MOLD_BOTTOM_PADDING),
                new Vertex(MOLD_WIDTH, MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                new Vertex(0.0f, MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                0));
        blank.addFacet(new Facet(
                new Vertex(0.0f, MOLD_WIDTH, -MOLD_BOTTOM_PADDING),
                new Vertex(MOLD_WIDTH, MOLD_WIDTH, -MOLD_BOTTOM_PADDING),
                new Vertex(MOLD_WIDTH, MOLD_WIDTH, -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                0));

        // bottom top
        blank.addFacet(new Facet(
                new Vertex(0.0f, MOLD_WIDTH, -MOLD_BOTTOM_PADDING),
                new Vertex(MOLD_WIDTH, -MOLD_WIDTH, -MOLD_BOTTOM_PADDING),
                new Vertex(MOLD_WIDTH, MOLD_WIDTH, -MOLD_BOTTOM_PADDING),
                0));
        blank.addFacet(new Facet(
                new Vertex(0.0f, MOLD_WIDTH, -MOLD_BOTTOM_PADDING),
                new Vertex(0.0f, -MOLD_WIDTH, -MOLD_BOTTOM_PADDING),
                new Vertex(MOLD_WIDTH, -MOLD_WIDTH, -MOLD_BOTTOM_PADDING),
                0));
    }

    private void generateTop() {
        System.out.println("generating top");

        int steps = blankConfiguration.isMold() ? rotationSteps() / 2 : rotationSteps();

        if (blankConfiguration.isMold()) {
            if (blankConfiguration.isCenter()) {
                blank.addFacet(
                        new Facet(
                                new Vertex(0.0f, 0.0f, CENTER_DEPTH),
                                new Vertex(0.0f, -tubeRadius(), 0.0f),
                                new Vertex(0.0f, tubeRadius(), 0.0f),
                                0));
            }

            blank.addFacet(
                    new Facet(
                            new Vertex(0.0f, MOLD_WIDTH, -MOLD_BOTTOM_PADDING),
                            new Vertex(0.0f, -MOLD_WIDTH, 0.0f),
                            new Vertex(0.0f, -MOLD_WIDTH, -MOLD_BOTTOM_PADDING),
                            0));
            blank.addFacet(
                    new Facet(
                            new Vertex(0.0f, MOLD_WIDTH, -MOLD_BOTTOM_PADDING),
                            new Vertex(0.0f, MOLD_WIDTH, 0.0f),
                            new Vertex(0.0f, -MOLD_WIDTH, 0.0f),
                            0));
        }

        for (int i = 0; i < steps; i++) {
            float x = x(i, false);
            float y = y(i, false);
            float X = x(i + 1, false);
            float Y = y(i + 1, false);
            float z = blankConfiguration.isCenter() ? CENTER_DEPTH : 0.0f;

            blank.addFacet(new Facet(
                    new Vertex(x, y, 0.0f),
                    new Vertex(X, Y, 0.0f),
                    new Vertex(0.0f, 0.0f, z),
                    0));
        }
    }

    private void generateShaft() {
        System.out.println("generating shaft");

        int steps = blankConfiguration.isMold() ? rotationSteps() / 2 : rotationSteps();

        if (blankConfiguration.isMold()) {
            blank.addFacet(
                    new Facet(
                            new Vertex(0.0f, -tubeRadius(), 0.0f),
                            new Vertex(0.0f, -MOLD_WIDTH, blankConfiguration.getLength()),
                            new Vertex(0.0f, -MOLD_WIDTH, 0.0f),
                            0));
            blank.addFacet(
                    new Facet(
                            new Vertex(0.0f, -tubeRadius(), 0.0f),
                            new Vertex(0.0f, -tubeRadius(), blankConfiguration.getLength()),
                            new Vertex(0.0f, -MOLD_WIDTH, blankConfiguration.getLength()),
                            0));

            blank.addFacet(
                    new Facet(
                            new Vertex(0.0f, tubeRadius(), 0.0f),
                            new Vertex(0.0f, MOLD_WIDTH, 0.0f),
                            new Vertex(0.0f, MOLD_WIDTH, blankConfiguration.getLength()),
                            0));
            blank.addFacet(
                    new Facet(
                            new Vertex(0.0f, tubeRadius(), 0.0f),
                            new Vertex(0.0f, MOLD_WIDTH, blankConfiguration.getLength()),
                            new Vertex(0.0f, tubeRadius(), blankConfiguration.getLength()),
                            0));

        }

        int x = 0;
        float lastZ = 0.0f;
        for (float z = 0.0f; z < (blankConfiguration.getLength() - (2 * Z_STEP)); z += Z_STEP) {
            lastZ = z + Z_STEP;

            // pad shaft until image zero
            if (z < blankConfiguration.getImageZero()) {
                for (int i = 0; i < steps; i++) {
                    blank.addFacet(new Facet(
                            new Vertex(x(i, false), y(i, false), z),
                            new Vertex(x(i + 1, false), y(i + 1, false), z + Z_STEP),
                            new Vertex(x(i + 1, false), y(i + 1, false), z),
                            0));

                    blank.addFacet(new Facet(
                            new Vertex(x(i, false), y(i, false), z),
                            new Vertex(x(i, false), y(i, false), z + Z_STEP),
                            new Vertex(x(i + 1, false), y(i + 1, false), z + Z_STEP),
                            0));
                }

                continue;
            }

            for (int i = 0; i < steps; i++) {
                boolean isImage = isImage(x, i);

                boolean topImage = isImage(x - 1, i);
                boolean leftImage = isImage(x, i - 1);

                if (isImage != topImage) {
                    if (isImage) {
                        blank.addFacet(new Facet(
                                new Vertex(x(i, true), y(i, true), z),
                                new Vertex(x(i + 1, true), y(i + 1, true), z),
                                new Vertex(x(i + 1, false), y(i + 1, false), z),
                                0));
                        blank.addFacet(new Facet(
                                new Vertex(x(i, true), y(i, true), z),
                                new Vertex(x(i + 1, false), y(i + 1, false), z),
                                new Vertex(x(i, false), y(i, false), z),
                                0));
                    } else {
                        blank.addFacet(new Facet(
                                new Vertex(x(i, true), y(i, true), z),
                                new Vertex(x(i + 1, false), y(i + 1, false), z),
                                new Vertex(x(i + 1, true), y(i + 1, true), z),
                                0));
                        blank.addFacet(new Facet(
                                new Vertex(x(i, true), y(i, true), z),
                                new Vertex(x(i, false), y(i, false), z),
                                new Vertex(x(i + 1, false), y(i + 1, false), z),
                                0));
                    }
                }

                if (isImage != leftImage) {
                    if (isImage) {
                        blank.addFacet(new Facet(
                                new Vertex(x(i, true), y(i, true), z),
                                new Vertex(x(i, false), y(i, false), z + Z_STEP),
                                new Vertex(x(i, true), y(i, true), z + Z_STEP),
                                0));
                        blank.addFacet(new Facet(
                                new Vertex(x(i, true), y(i, true), z),
                                new Vertex(x(i, false), y(i, false), z),
                                new Vertex(x(i, false), y(i, false), z + Z_STEP),
                                0));
                    } else {
                        blank.addFacet(new Facet(
                                new Vertex(x(i, false), y(i, false), z),
                                new Vertex(x(i, true), y(i, true), z + Z_STEP),
                                new Vertex(x(i, false), y(i, false), z + Z_STEP),
                                0));
                        blank.addFacet(new Facet(
                                new Vertex(x(i, false), y(i, false), z),
                                new Vertex(x(i, true), y(i, true), z),
                                new Vertex(x(i, true), y(i, true), z + Z_STEP),
                                0));
                    }
                }

                blank.addFacet(new Facet(
                        new Vertex(x(i, isImage), y(i, isImage), z),
                        new Vertex(x(i + 1, isImage), y(i + 1, isImage), z + Z_STEP),
                        new Vertex(x(i + 1, isImage), y(i + 1, isImage), z),
                        0));

                blank.addFacet(new Facet(
                        new Vertex(x(i, isImage), y(i, isImage), z),
                        new Vertex(x(i, isImage), y(i, isImage), z + Z_STEP),
                        new Vertex(x(i + 1, isImage), y(i + 1, isImage), z + Z_STEP),
                        0));
            }

            x++;
        }

        // it is possible that the Z has accumulated enough error due to floating
        // point math that it is no longer a multiple of Z_STEP and we have prematurely
        // popped out of the loop, so to ensure the bottom mates up nicely we are going
        // to insert a new row.
        if (lastZ < blankConfiguration.getLength()) {
            for (int i = 0; i < steps; i++) {
                // TODO: figure out if we need to do anything for the last line of image, but for now
                // we are going to assume the last line of the image is all at tube depth
                blank.addFacet(new Facet(
                        new Vertex(x(i, false), y(i, false), lastZ),
                        new Vertex(x(i + 1, false), y(i + 1, false), blankConfiguration.getLength()),
                        new Vertex(x(i + 1, false), y(i + 1, false), lastZ),
                        0));

                blank.addFacet(new Facet(
                        new Vertex(x(i, false), y(i, false), lastZ),
                        new Vertex(x(i, false), y(i, false), blankConfiguration.getLength()),
                        new Vertex(x(i + 1, false), y(i + 1, false), blankConfiguration.getLength()),
                        0));
            }
        }
    }

    private void generateBottom() {
        System.out.println("generate bottom");

        int steps = blankConfiguration.isMold() ? rotationSteps() / 2 : rotationSteps();

        if (blankConfiguration.isMold()) {
            // left
            blank.addFacet(
                    new Facet(
                            new Vertex(0.0f, -tubeRadius(), blankConfiguration.getLength()),
                            new Vertex(0.0f, -imageRadius(), blankConfiguration.getLength() + FUNNEL_DEPTH),
                            new Vertex(0.0f, -MOLD_WIDTH, blankConfiguration.getLength()),
                            0));
            blank.addFacet(
                    new Facet(
                            new Vertex(0.0f, -MOLD_WIDTH, blankConfiguration.getLength()),
                            new Vertex(0.0f, -imageRadius(), blankConfiguration.getLength() + FUNNEL_DEPTH),
                            new Vertex(0.0f, -MOLD_WIDTH, blankConfiguration.getLength() + FUNNEL_DEPTH),
                            0));

            // right
            blank.addFacet(
                    new Facet(
                            new Vertex(0.0f, tubeRadius(), blankConfiguration.getLength()),
                            new Vertex(0.0f, MOLD_WIDTH, blankConfiguration.getLength()),
                            new Vertex(0.0f, imageRadius(), blankConfiguration.getLength() + FUNNEL_DEPTH),
                            0));
            blank.addFacet(
                    new Facet(
                            new Vertex(0.0f, MOLD_WIDTH, blankConfiguration.getLength()),
                            new Vertex(0.0f, MOLD_WIDTH, blankConfiguration.getLength() + FUNNEL_DEPTH),
                            new Vertex(0.0f, imageRadius(), blankConfiguration.getLength() + FUNNEL_DEPTH),
                            0));

            // above
            blank.addFacet(
                    new Facet(
                            new Vertex(0.0f, MOLD_WIDTH, blankConfiguration.getLength() + FUNNEL_DEPTH),
                            new Vertex(0.0f, -MOLD_WIDTH, blankConfiguration.getLength() + FUNNEL_DEPTH + MOLD_TOP_PADDING),
                            new Vertex(0.0f, -MOLD_WIDTH, blankConfiguration.getLength() + FUNNEL_DEPTH),
                            0));
            blank.addFacet(
                    new Facet(
                            new Vertex(0.0f, MOLD_WIDTH, blankConfiguration.getLength() + FUNNEL_DEPTH),
                            new Vertex(0.0f, MOLD_WIDTH, blankConfiguration.getLength() + FUNNEL_DEPTH + MOLD_TOP_PADDING),
                            new Vertex(0.0f, -MOLD_WIDTH, blankConfiguration.getLength() + FUNNEL_DEPTH + MOLD_TOP_PADDING),
                            0));
        }

        if (blankConfiguration.isFunnel()) {
            for (int i = 0; i < steps; i++) {
                float tx = x(i, false);
                float ty = y(i, false);
                float tX = x(i + 1, false);
                float tY = y(i + 1, false);
                float dx = funnelX(i);
                float dy = funnelY(i);
                float dX = funnelX(i + 1);
                float dY = funnelY(i + 1);
                float z = blankConfiguration.getLength();
                float Z = blankConfiguration.getLength() + FUNNEL_DEPTH;

                blank.addFacet(new Facet(
                        new Vertex(tx, ty, z),
                        new Vertex(dX, dY, Z),
                        new Vertex(tX, tY, z),
                        0));

                blank.addFacet(new Facet(
                        new Vertex(dx, dy, Z),
                        new Vertex(dx, dy, Z + FUNNEL_HEIGHT),
                        new Vertex(dX, dY, Z + FUNNEL_HEIGHT),
                        0));

                blank.addFacet(new Facet(
                        new Vertex(dx, dy, Z),
                        new Vertex(dX, dY, Z + FUNNEL_HEIGHT),
                        new Vertex(dX, dY, Z),
                        0));

                blank.addFacet(new Facet(
                        new Vertex(tx, ty, z),
                        new Vertex(dx, dy, Z),
                        new Vertex(dX, dY, Z),
                        0));

                blank.addFacet(
                        new Facet(
                                new Vertex(dx, dy, Z + FUNNEL_HEIGHT),
                                new Vertex(0, 0, Z + FUNNEL_HEIGHT),
                                new Vertex(dX, dY, Z + FUNNEL_HEIGHT),
                                0));
            }
        } else {
            for (int i = 0; i < steps; i++) {
                float x = x(i, false);
                float y = y(i, false);
                float X = x(i + 1, false);
                float Y = y(i + 1, false);

                blank.addFacet(new Facet(
                        new Vertex(x, y, blankConfiguration.getLength()),
                        new Vertex(0.0f, 0.0f, blankConfiguration.getLength()),
                        new Vertex(X, Y, blankConfiguration.getLength()),
                        0));
            }
        }
    }

    boolean isImage(int x, int y) {
        if (x < 0 || y < 0) {
            // NOTE: figure this is the negative of the image, or wrap around
            // we might need to change this to the Y being based on rotation_steps.
            return isImage(image.getWidth() + x, image.getHeight() + y);
        } else if (x >= image.getWidth() || y >= image.getHeight()) {
            // NOTE: we are off the image so no image pixels
            return false;
        } else {
            return image.getRGB(x, y) == -16777216;
        }
    }

    float x(int rotationStep, boolean isImage) {
        if (isImage) {
            return imageX[rotationStep % rotationSteps()];
        } else {
            return tubeX[rotationStep % rotationSteps()];
        }
    }

    float y(int rotationStep, boolean isImage) {
        if (isImage) {
            return imageY[rotationStep % rotationSteps()];
        } else {
            return tubeY[rotationStep % rotationSteps()];
        }
    }

    float funnelX(int rotationStep) {
        return funnelX[rotationStep % rotationSteps()];
    }

    float funnelY(int rotationStep) {
        return funnelY[rotationStep % rotationSteps()];
    }

    private float tubeRadius() {
        return blankConfiguration.getTube() / 2.0f;
    }

    private float imageRadius() {
        return blankConfiguration.getDiameter() / 2.0f;
    }

    private int rotationSteps() {
//        return (int) ((Math.PI * blankConfiguration.getImageZero()) / Z_STEP);
        return (int) image.getHeight();
    }

    public void writeStl() {
        try {
            OutputStream stream = new FileOutputStream(new File(outputFilename));
            stream.write(blank.getBytes());
            stream.close();
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
