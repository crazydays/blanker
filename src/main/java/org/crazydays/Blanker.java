package org.crazydays;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class Blanker {
    public final static float Z_STEP = 0.04f;

    public final static float MOLD_THICKNESS = 2.0f;
    public final static float MOLD_BOTTOM_PADDING = 5.0f;
    public final static float MOLD_TOP_PADDING = 10.0f;

    public final static float CENTER_DEPTH = 5.0f;
    public final static float FUNNEL_DEPTH = 5.0f;
    public final static float FUNNEL_HEIGHT = 10.0f;

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
    private float[] roundMoldX;
    private float[] roundMoldY;

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

        if (blankConfiguration.isSquareMold()) {
            generateSquareMold();
        }

        if (blankConfiguration.isRoundMold()) {
            generateRoundMold();
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
        roundMoldX = new float[rotationSteps()];
        roundMoldY = new float[rotationSteps()];

        for (int i = 0; i < rotationSteps(); i++) {
            tubeX[i] = (float) Math.sin((2 * Math.PI) / rotationSteps() * i) * tubeRadius();
            tubeY[i] = (float) Math.cos((2 * Math.PI) / rotationSteps() * i) * tubeRadius();
            imageX[i] = (float) Math.sin((2 * Math.PI) / rotationSteps() * (i + 1)) * imageRadius();
            imageY[i] = (float) Math.cos((2 * Math.PI) / rotationSteps() * (i + 1)) * imageRadius();
            funnelX[i] = (float) Math.sin((2 * Math.PI) / rotationSteps() * i) * funnelRadius();
            funnelY[i] = (float) Math.cos((2 * Math.PI) / rotationSteps() * i) * funnelRadius();
            roundMoldX[i] = (float) Math.sin((2 * Math.PI) / rotationSteps() * i) * roundMoldRadius();
            roundMoldY[i] = (float) Math.cos((2 * Math.PI) / rotationSteps() * i) * roundMoldRadius();

            System.out.format(
                    "tube (x, y) => (%.3f, %.3f), image (x, y) => (%.3f, %.3f)\n",
                    tubeX[i], tubeY[i], imageX[i], imageY[i]);
            System.out.format(
                    "funnel (x, y) => (%.3f, %.3f), roundMold (x, y) => (%.3f, %.3f)\n",
                    funnelX[i], funnelY[i], roundMoldX[i], roundMoldY[i]);
       }
    }

    private void generateSquareMold() {
        System.out.println("generating square mold");

        // back
        addFacet(
                vertex(-MOLD_THICKNESS, blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(-MOLD_THICKNESS, -blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(-MOLD_THICKNESS, -blankConfiguration.getSquareMold(), blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING)
        );
        addFacet(
                vertex(-MOLD_THICKNESS, blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(-MOLD_THICKNESS, -blankConfiguration.getSquareMold(), blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                vertex(-MOLD_THICKNESS, blankConfiguration.getSquareMold(), blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING)
        );

        // left
        addFacet(
                vertex(0.0f, -blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(-MOLD_THICKNESS, -blankConfiguration.getSquareMold(), blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                vertex(-MOLD_THICKNESS, -blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS))
        );
        addFacet(
                vertex(0.0f, -blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(0.0f, -blankConfiguration.getSquareMold(), blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                vertex(-MOLD_THICKNESS, -blankConfiguration.getSquareMold(), blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING)
        );

        // top
        addFacet(
                vertex(0.0f, -blankConfiguration.getSquareMold(), blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                vertex(-MOLD_THICKNESS, blankConfiguration.getSquareMold(), blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                vertex(-MOLD_THICKNESS, -blankConfiguration.getSquareMold(), blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING)
        );
        addFacet(
                vertex(0.0f, -blankConfiguration.getSquareMold(), blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                vertex(0.0f, blankConfiguration.getSquareMold(), blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                vertex(-MOLD_THICKNESS, blankConfiguration.getSquareMold(), blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING)
        );

        // right
        addFacet(
                vertex(0.0f, blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(-MOLD_THICKNESS, blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(-MOLD_THICKNESS, blankConfiguration.getSquareMold(), blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING)
        );
        addFacet(
                vertex(0.0f, blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(-MOLD_THICKNESS, blankConfiguration.getSquareMold(), blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                vertex(0.0f, blankConfiguration.getSquareMold(), blankConfiguration.getLength() + (blankConfiguration.isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING)
        );

        // bottom
        addFacet(
                vertex(blankConfiguration.getSquareMold(), -blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(-MOLD_THICKNESS, -blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(-MOLD_THICKNESS, blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS))
        );
        addFacet(
                vertex(blankConfiguration.getSquareMold(), -blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(-MOLD_THICKNESS, blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(blankConfiguration.getSquareMold(), blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS))
        );

        // bottom left
        addFacet(
                vertex(0.0f, -blankConfiguration.getSquareMold(), -MOLD_BOTTOM_PADDING),
                vertex(0.0f, -blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(blankConfiguration.getSquareMold(), -blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS))
        );
        addFacet(
                vertex(0.0f, -blankConfiguration.getSquareMold(), -MOLD_BOTTOM_PADDING),
                vertex(blankConfiguration.getSquareMold(), -blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(blankConfiguration.getSquareMold(), -blankConfiguration.getSquareMold(), -MOLD_BOTTOM_PADDING)
        );


        // bottom front
        addFacet(
                vertex(blankConfiguration.getSquareMold(), -blankConfiguration.getSquareMold(), -MOLD_BOTTOM_PADDING),
                vertex(blankConfiguration.getSquareMold(), -blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(blankConfiguration.getSquareMold(), blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS))
        );
        addFacet(
                vertex(blankConfiguration.getSquareMold(), -blankConfiguration.getSquareMold(), -MOLD_BOTTOM_PADDING),
                vertex(blankConfiguration.getSquareMold(), blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(blankConfiguration.getSquareMold(), blankConfiguration.getSquareMold(), -MOLD_BOTTOM_PADDING)
        );

        // bottom right
        addFacet(
                vertex(0.0f, blankConfiguration.getSquareMold(), -MOLD_BOTTOM_PADDING),
                vertex(blankConfiguration.getSquareMold(), blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(0.0f, blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS))
        );
        addFacet(
                vertex(0.0f, blankConfiguration.getSquareMold(), -MOLD_BOTTOM_PADDING),
                vertex(blankConfiguration.getSquareMold(), blankConfiguration.getSquareMold(), -MOLD_BOTTOM_PADDING),
                vertex(blankConfiguration.getSquareMold(), blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS))
        );

        // bottom top
        addFacet(
                vertex(0.0f, blankConfiguration.getSquareMold(), -MOLD_BOTTOM_PADDING),
                vertex(blankConfiguration.getSquareMold(), -blankConfiguration.getSquareMold(), -MOLD_BOTTOM_PADDING),
                vertex(blankConfiguration.getSquareMold(), blankConfiguration.getSquareMold(), -MOLD_BOTTOM_PADDING)
        );
        addFacet(
                vertex(0.0f, blankConfiguration.getSquareMold(), -MOLD_BOTTOM_PADDING),
                vertex(0.0f, -blankConfiguration.getSquareMold(), -MOLD_BOTTOM_PADDING),
                vertex(blankConfiguration.getSquareMold(), -blankConfiguration.getSquareMold(), -MOLD_BOTTOM_PADDING)
        );
    }

    private void generateRoundMold() {
        int steps = rotationSteps();
        float z = -MOLD_BOTTOM_PADDING;
        float Z = blankConfiguration.isFunnel() ? blankConfiguration.getLength() + FUNNEL_DEPTH + FUNNEL_HEIGHT: blankConfiguration.getLength();

        for (int i = 0; i < steps; i++) {
            float x0 = blankConfiguration.isFunnel() ? funnelX(i) : x(i, false);
            float y0 = blankConfiguration.isFunnel() ? funnelY(i) : y(i, false);
            float x1 = blankConfiguration.isFunnel() ? funnelX(i + 1) : x(i + 1, false);
            float y1 = blankConfiguration.isFunnel() ? funnelY(i + 1) : y(i + 1, false);
            float X0 = roundMoldX(i);
            float Y0 = roundMoldY(i);
            float X1 = roundMoldX(i + 1);
            float Y1 = roundMoldY(i + 1);

            // top
            addFacet(
                    vertex(x0, y0, Z),
                    vertex(X0, Y0, Z),
                    vertex(X1, Y1, Z)
            );
            addFacet(
                    vertex(x0, y0, Z),
                    vertex(X1, Y1, Z),
                    vertex(x1, y1, Z)
            );

            // sides
            addFacet(
                    vertex(X0, Y0, Z),
                    vertex(X0, Y0, z),
                    vertex(X1, Y1, z)
            );
            addFacet(
                    vertex(X0, Y0, Z),
                    vertex(X1, Y1, z),
                    vertex(X1, Y1, Z)
            );

            // bottom
            addFacet(
                    vertex(0.0f, 0.0f, z),
                    vertex(X1, Y1, z),
                    vertex(X0, Y0, z)
            );

        }
    }

    private void generateTop() {
        System.out.println("generating top");

        int steps = blankConfiguration.isSquareMold() ? rotationSteps() / 2 : rotationSteps();

        if (blankConfiguration.isSquareMold()) {
            if (blankConfiguration.isCenter()) {
                addFacet(
                        vertex(0.0f, 0.0f, CENTER_DEPTH),
                        vertex(0.0f, -tubeRadius(), 0.0f),
                        vertex(0.0f, tubeRadius(), 0.0f)
                );
            }

            addFacet(
                    vertex(0.0f, blankConfiguration.getSquareMold(), -MOLD_BOTTOM_PADDING),
                    vertex(0.0f, -blankConfiguration.getSquareMold(), 0.0f),
                    vertex(0.0f, -blankConfiguration.getSquareMold(), -MOLD_BOTTOM_PADDING)
            );
            addFacet(
                    vertex(0.0f, blankConfiguration.getSquareMold(), -MOLD_BOTTOM_PADDING),
                    vertex(0.0f, blankConfiguration.getSquareMold(), 0.0f),
                    vertex(0.0f, -blankConfiguration.getSquareMold(), 0.0f)
            );
        }

        for (int i = 0; i < steps; i++) {
            float x = x(i, false);
            float y = y(i, false);
            float X = x(i + 1, false);
            float Y = y(i + 1, false);
            float z = blankConfiguration.isCenter() ? CENTER_DEPTH : 0.0f;

            addFacet(
                    vertex(x, y, 0.0f),
                    vertex(X, Y, 0.0f),
                    vertex(0.0f, 0.0f, z)
            );
        }
    }

    private void generateShaft() {
        System.out.println("generating shaft");

        int steps = blankConfiguration.isSquareMold() ? rotationSteps() / 2 : rotationSteps();

        if (blankConfiguration.isSquareMold()) {
            addFacet(
                    vertex(0.0f, -tubeRadius(), 0.0f),
                    vertex(0.0f, -blankConfiguration.getSquareMold(), blankConfiguration.getLength()),
                    vertex(0.0f, -blankConfiguration.getSquareMold(), 0.0f)
            );
            addFacet(
                    vertex(0.0f, -tubeRadius(), 0.0f),
                    vertex(0.0f, -tubeRadius(), blankConfiguration.getLength()),
                    vertex(0.0f, -blankConfiguration.getSquareMold(), blankConfiguration.getLength())
            );

            addFacet(
                    vertex(0.0f, tubeRadius(), 0.0f),
                    vertex(0.0f, blankConfiguration.getSquareMold(), 0.0f),
                    vertex(0.0f, blankConfiguration.getSquareMold(), blankConfiguration.getLength())
            );
            addFacet(
                    vertex(0.0f, tubeRadius(), 0.0f),
                    vertex(0.0f, blankConfiguration.getSquareMold(), blankConfiguration.getLength()),
                    vertex(0.0f, tubeRadius(), blankConfiguration.getLength())
            );
        }

        int x = 0;
        float lastZ = 0.0f;
        for (float z = 0.0f; z < (blankConfiguration.getLength() - (2 * Z_STEP)); z += Z_STEP) {
            lastZ = z + Z_STEP;

            // pad shaft until image zero
            if (z < blankConfiguration.getImageZero()) {
                for (int i = 0; i < steps; i++) {
                    addFacet(
                            vertex(x(i, false), y(i, false), z),
                            vertex(x(i + 1, false), y(i + 1, false), z + Z_STEP),
                            vertex(x(i + 1, false), y(i + 1, false), z)
                    );

                    addFacet(
                            vertex(x(i, false), y(i, false), z),
                            vertex(x(i, false), y(i, false), z + Z_STEP),
                            vertex(x(i + 1, false), y(i + 1, false), z + Z_STEP)
                    );
                }

                continue;
            }

            for (int i = 0; i < steps; i++) {
                boolean isImage = isImage(x, i);

                boolean topImage = isImage(x - 1, i);
                boolean leftImage = isImage(x, i - 1);

                if (isImage != topImage) {
                    if (isImage) {
                        addFacet(
                                vertex(x(i, true), y(i, true), z),
                                vertex(x(i + 1, true), y(i + 1, true), z),
                                vertex(x(i + 1, false), y(i + 1, false), z)
                        );
                        addFacet(
                                vertex(x(i, true), y(i, true), z),
                                vertex(x(i + 1, false), y(i + 1, false), z),
                                vertex(x(i, false), y(i, false), z)
                        );
                    } else {
                        addFacet(
                                vertex(x(i, true), y(i, true), z),
                                vertex(x(i + 1, false), y(i + 1, false), z),
                                vertex(x(i + 1, true), y(i + 1, true), z)
                        );
                        addFacet(
                                vertex(x(i, true), y(i, true), z),
                                vertex(x(i, false), y(i, false), z),
                                vertex(x(i + 1, false), y(i + 1, false), z)
                        );
                    }
                }

                if (isImage != leftImage) {
                    if (isImage) {
                        addFacet(
                                vertex(x(i, true), y(i, true), z),
                                vertex(x(i, false), y(i, false), z + Z_STEP),
                                vertex(x(i, true), y(i, true), z + Z_STEP)
                        );
                        addFacet(
                                vertex(x(i, true), y(i, true), z),
                                vertex(x(i, false), y(i, false), z),
                                vertex(x(i, false), y(i, false), z + Z_STEP)
                        );
                    } else {
                        addFacet(
                                vertex(x(i, false), y(i, false), z),
                                vertex(x(i, true), y(i, true), z + Z_STEP),
                                vertex(x(i, false), y(i, false), z + Z_STEP)
                        );
                        addFacet(
                                vertex(x(i, false), y(i, false), z),
                                vertex(x(i, true), y(i, true), z),
                                vertex(x(i, true), y(i, true), z + Z_STEP)
                        );
                    }
                }

                addFacet(
                        vertex(x(i, isImage), y(i, isImage), z),
                        vertex(x(i + 1, isImage), y(i + 1, isImage), z + Z_STEP),
                        vertex(x(i + 1, isImage), y(i + 1, isImage), z)
                );

                addFacet(
                        vertex(x(i, isImage), y(i, isImage), z),
                        vertex(x(i, isImage), y(i, isImage), z + Z_STEP),
                        vertex(x(i + 1, isImage), y(i + 1, isImage), z + Z_STEP)
                );
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
                addFacet(
                        vertex(x(i, false), y(i, false), lastZ),
                        vertex(x(i + 1, false), y(i + 1, false), blankConfiguration.getLength()),
                        vertex(x(i + 1, false), y(i + 1, false), lastZ)
                );

                addFacet(
                        vertex(x(i, false), y(i, false), lastZ),
                        vertex(x(i, false), y(i, false), blankConfiguration.getLength()),
                        vertex(x(i + 1, false), y(i + 1, false), blankConfiguration.getLength())
                );
            }
        }
    }

    private void generateBottom() {
        System.out.println("generate bottom");

        int steps = blankConfiguration.isSquareMold() ? rotationSteps() / 2 : rotationSteps();

        if (blankConfiguration.isSquareMold()) {
            // left
            addFacet(
                    vertex(0.0f, -tubeRadius(), blankConfiguration.getLength()),
                    vertex(0.0f, -imageRadius(), blankConfiguration.getLength() + FUNNEL_DEPTH),
                    vertex(0.0f, -blankConfiguration.getSquareMold(), blankConfiguration.getLength())
            );
            addFacet(
                    vertex(0.0f, -blankConfiguration.getSquareMold(), blankConfiguration.getLength()),
                    vertex(0.0f, -imageRadius(), blankConfiguration.getLength() + FUNNEL_DEPTH),
                    vertex(0.0f, -blankConfiguration.getSquareMold(), blankConfiguration.getLength() + FUNNEL_DEPTH)
            );

            // right
            addFacet(
                    vertex(0.0f, tubeRadius(), blankConfiguration.getLength()),
                    vertex(0.0f, blankConfiguration.getSquareMold(), blankConfiguration.getLength()),
                    vertex(0.0f, imageRadius(), blankConfiguration.getLength() + FUNNEL_DEPTH)
            );
            addFacet(
                    vertex(0.0f, blankConfiguration.getSquareMold(), blankConfiguration.getLength()),
                    vertex(0.0f, blankConfiguration.getSquareMold(), blankConfiguration.getLength() + FUNNEL_DEPTH),
                    vertex(0.0f, imageRadius(), blankConfiguration.getLength() + FUNNEL_DEPTH)
            );

            // above
            addFacet(
                    vertex(0.0f, blankConfiguration.getSquareMold(), blankConfiguration.getLength() + FUNNEL_DEPTH),
                    vertex(0.0f, -blankConfiguration.getSquareMold(), blankConfiguration.getLength() + FUNNEL_DEPTH + MOLD_TOP_PADDING),
                    vertex(0.0f, -blankConfiguration.getSquareMold(), blankConfiguration.getLength() + FUNNEL_DEPTH)
            );
            addFacet(
                    vertex(0.0f, blankConfiguration.getSquareMold(), blankConfiguration.getLength() + FUNNEL_DEPTH),
                    vertex(0.0f, blankConfiguration.getSquareMold(), blankConfiguration.getLength() + FUNNEL_DEPTH + MOLD_TOP_PADDING),
                    vertex(0.0f, -blankConfiguration.getSquareMold(), blankConfiguration.getLength() + FUNNEL_DEPTH + MOLD_TOP_PADDING)
            );
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

                addFacet(
                        vertex(tx, ty, z),
                        vertex(dX, dY, Z),
                        vertex(tX, tY, z)
                );

                addFacet(
                        vertex(dx, dy, Z),
                        vertex(dx, dy, Z + FUNNEL_HEIGHT),
                        vertex(dX, dY, Z + FUNNEL_HEIGHT)
                );

                addFacet(
                        vertex(dx, dy, Z),
                        vertex(dX, dY, Z + FUNNEL_HEIGHT),
                        vertex(dX, dY, Z)
                );

                addFacet(
                        vertex(tx, ty, z),
                        vertex(dx, dy, Z),
                        vertex(dX, dY, Z)
                );

                if (blankConfiguration.isPositive()) {
                    addFacet(
                            vertex(dx, dy, Z + FUNNEL_HEIGHT),
                            vertex(0, 0, Z + FUNNEL_HEIGHT),
                            vertex(dX, dY, Z + FUNNEL_HEIGHT)
                    );
                }
            }
        } else {
            for (int i = 0; i < steps; i++) {
                float x = x(i, false);
                float y = y(i, false);
                float X = x(i + 1, false);
                float Y = y(i + 1, false);

                addFacet(
                        vertex(x, y, blankConfiguration.getLength()),
                        vertex(0.0f, 0.0f, blankConfiguration.getLength()),
                        vertex(X, Y, blankConfiguration.getLength())
                );
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

    float roundMoldX(int rotationStep) {
        return roundMoldX[rotationStep % rotationSteps()];
    }

    float roundMoldY(int rotationStep) {
        return roundMoldY[rotationStep % rotationSteps()];
    }

    private float tubeRadius() {
        return blankConfiguration.getTube() / 2.0f;
    }

    private float imageRadius() {
        return blankConfiguration.getDiameter() / 2.0f;
    }

    private float funnelRadius() {
        return blankConfiguration.getFunnel() / 2.0f;
    }

    private float roundMoldRadius() {
        return blankConfiguration.getRoundMold() / 2.0f;
    }

    private int rotationSteps() {
        return (int) image.getHeight();
    }

    private Vertex vertex(float x, float y, float z) {
        return new Vertex(x, y, z);
    }

    private void addFacet(Vertex a, Vertex b, Vertex c) {
        if (blankConfiguration.isPositive()) {
            blank.addFacet(new Facet(a, b, c, 0));
        } else {
            blank.addFacet(new Facet(a, c, b, 0));
        }
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
