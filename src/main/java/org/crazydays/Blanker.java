package org.crazydays;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class Blanker {
    public final static float Z_STEP = 0.05f;

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
    private float[] wallThicknessTubeX;
    private float[] wallThicknessTubeY;
    private float[] wallThicknessImageX;
    private float[] wallThicknessImageY;

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
        wallThicknessTubeX = new float[rotationSteps()];
        wallThicknessTubeY = new float[rotationSteps()];
        wallThicknessImageX = new float[rotationSteps()];
        wallThicknessImageY = new float[rotationSteps()];

        for (int i = 0; i < rotationSteps(); i++) {
            tubeX[i] = calculateX(i, tubeRadius());
            tubeY[i] = calculateY(i, tubeRadius());
            imageX[i] = calculateX(i, imageRadius());
            imageY[i] = calculateY(i, imageRadius());
            funnelX[i] = calculateX(i, funnelRadius());
            funnelY[i] = calculateY(i, funnelRadius());
            roundMoldX[i] = calculateX(i, roundMoldRadius());
            roundMoldY[i] = calculateY(i, roundMoldRadius());
            wallThicknessTubeX[i] = calculateX(i, tubeRadius() + wallThickness());
            wallThicknessTubeY[i] = calculateY(i, tubeRadius() + wallThickness());
            wallThicknessImageX[i] = calculateX(i, imageRadius() + wallThickness());
            wallThicknessImageY[i] = calculateY(i, imageRadius() + wallThickness());
        }
    }

    private float calculateX(int step, float length) {
        return (float) Math.sin((2 * Math.PI) / rotationSteps() * step) * length;
    }

    private float calculateY(int step, float length) {
        return (float) Math.cos((2 * Math.PI) / rotationSteps() * step) * length;
    }

    private void generateSquareMold() {
        System.out.println("generating square mold");

        // back
        addFacet(
                vertex(-MOLD_THICKNESS, blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(-MOLD_THICKNESS, -blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(-MOLD_THICKNESS, -blankConfiguration.getSquareMold(), length() + (isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING)
        );
        addFacet(
                vertex(-MOLD_THICKNESS, blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(-MOLD_THICKNESS, -blankConfiguration.getSquareMold(), length() + (isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                vertex(-MOLD_THICKNESS, blankConfiguration.getSquareMold(), length() + (isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING)
        );

        // left
        addFacet(
                vertex(0.0f, -blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(-MOLD_THICKNESS, -blankConfiguration.getSquareMold(), length() + (isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                vertex(-MOLD_THICKNESS, -blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS))
        );
        addFacet(
                vertex(0.0f, -blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(0.0f, -blankConfiguration.getSquareMold(), length() + (isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                vertex(-MOLD_THICKNESS, -blankConfiguration.getSquareMold(), length() + (isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING)
        );

        // top
        addFacet(
                vertex(0.0f, -blankConfiguration.getSquareMold(), length() + (isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                vertex(-MOLD_THICKNESS, blankConfiguration.getSquareMold(), length() + (isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                vertex(-MOLD_THICKNESS, -blankConfiguration.getSquareMold(), length() + (isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING)
        );
        addFacet(
                vertex(0.0f, -blankConfiguration.getSquareMold(), length() + (isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                vertex(0.0f, blankConfiguration.getSquareMold(), length() + (isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                vertex(-MOLD_THICKNESS, blankConfiguration.getSquareMold(), length() + (isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING)
        );

        // right
        addFacet(
                vertex(0.0f, blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(-MOLD_THICKNESS, blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(-MOLD_THICKNESS, blankConfiguration.getSquareMold(), length() + (isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING)
        );
        addFacet(
                vertex(0.0f, blankConfiguration.getSquareMold(), -(MOLD_BOTTOM_PADDING + MOLD_THICKNESS)),
                vertex(-MOLD_THICKNESS, blankConfiguration.getSquareMold(), length() + (isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING),
                vertex(0.0f, blankConfiguration.getSquareMold(), length() + (isFunnel() ? FUNNEL_DEPTH : 0.0f) + MOLD_TOP_PADDING)
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
        System.out.println("generating round mold");
        float topZ = isFunnel() ? length() + FUNNEL_DEPTH + FUNNEL_HEIGHT : length();
        float funnelZ = length() + FUNNEL_DEPTH;
        float bottomZ = blankConfiguration.isCenter() ? -MOLD_BOTTOM_PADDING : 0;

        // top
        for (int i = 0; i < rotationSteps(); i++) {
            float x0 = isFunnel() ? funnelX(i) : x(i, false);
            float y0 = isFunnel() ? funnelY(i) : y(i, false);
            float x1 = isFunnel() ? funnelX(i + 1) : x(i + 1, false);
            float y1 = isFunnel() ? funnelY(i + 1) : y(i + 1, false);
            float X0 = isFunnel() ? roundMoldX(i) : wallThicknessX(i, false);
            float Y0 = isFunnel() ? roundMoldY(i) : wallThicknessY(i, false);
            float X1 = isFunnel() ? roundMoldX(i + 1) : wallThicknessX(i + 1, false);
            float Y1 = isFunnel() ? roundMoldY(i + 1) : wallThicknessY(i + 1, false);
            float X2 = wallThicknessX(i, false);
            float Y2 = wallThicknessY(i, false);
            float X3 = wallThicknessX(i + 1, false);
            float Y3 = wallThicknessY(i + 1, false);

            // rim
            addFacet(
                    vertex(x0, y0, topZ),
                    vertex(X0, Y0, topZ),
                    vertex(X1, Y1, topZ)
            );
            addFacet(
                    vertex(x0, y0, topZ),
                    vertex(X1, Y1, topZ),
                    vertex(x1, y1, topZ)
            );

            if (isFunnel()) {
                // vertical
                addFacet(
                        vertex(X0, Y0, topZ),
                        vertex(X0, Y0, funnelZ),
                        vertex(X1, Y1, funnelZ)
                );
                addFacet(
                        vertex(X0, Y0, topZ),
                        vertex(X1, Y1, funnelZ),
                        vertex(X1, Y1, topZ)
                );

                // angled
                addFacet(
                        vertex(X2, Y2, length()),
                        vertex(X1, Y1, funnelZ),
                        vertex(X0, Y0, funnelZ)
                );
                addFacet(
                        vertex(X2, Y2, length()),
                        vertex(X3, Y3, length()),
                        vertex(X1, Y1, funnelZ)
                );
            }
        }

        int x = (int) -(imageZero() / Z_STEP);
        float lastZ = 0.0f;
        for (float z = 0.0f; z < (length() - (2 * Z_STEP)); z += Z_STEP) {
            lastZ = z + Z_STEP;

            for (int i = 0; i < rotationSteps(); i++) {
                boolean isImage = isWithinWallThickness(x, i);
                boolean topImage = isWithinWallThickness(x - 1, i);
                boolean leftImage = isWithinWallThickness(x, i - 1);

                if (isImage != topImage) {
                    if (isImage) {
                        addFacet(
                                vertex(wallThicknessX(i, true), wallThicknessY(i, true), z),
                                vertex(wallThicknessX(i + 1, false), wallThicknessY(i + 1, false), z),
                                vertex(wallThicknessX(i + 1, true), wallThicknessY(i + 1, true), z)
                        );
                        addFacet(
                                vertex(wallThicknessX(i, true), wallThicknessY(i, true), z),
                                vertex(wallThicknessX(i, false), wallThicknessY(i, false), z),
                                vertex(wallThicknessX(i + 1, false), wallThicknessY(i + 1, false), z)
                        );
                    } else {
                        addFacet(
                                vertex(wallThicknessX(i, true), wallThicknessY(i, true), z),
                                vertex(wallThicknessX(i + 1, true), wallThicknessY(i + 1, true), z),
                                vertex(wallThicknessX(i + 1, false), wallThicknessY(i + 1, false), z)
                        );
                        addFacet(
                                vertex(wallThicknessX(i, true), wallThicknessY(i, true), z),
                                vertex(wallThicknessX(i + 1, false), wallThicknessY(i + 1, false), z),
                                vertex(wallThicknessX(i, false), wallThicknessY(i, false), z)
                        );
                    }
                }

                if (isImage != leftImage) {
                    if (isImage) {
                        addFacet(
                                vertex(wallThicknessX(i, true), wallThicknessY(i, true), z),
                                vertex(wallThicknessX(i, true), wallThicknessY(i, true), z + Z_STEP),
                                vertex(wallThicknessX(i, false), wallThicknessY(i, false), z + Z_STEP)
                        );
                        addFacet(
                                vertex(wallThicknessX(i, true), wallThicknessY(i, true), z),
                                vertex(wallThicknessX(i, false), wallThicknessY(i, false), z + Z_STEP),
                                vertex(wallThicknessX(i, false), wallThicknessY(i, false), z)
                        );
                    } else {
                        addFacet(
                                vertex(wallThicknessX(i, false), wallThicknessY(i, false), z),
                                vertex(wallThicknessX(i, false), wallThicknessY(i, false), z + Z_STEP),
                                vertex(wallThicknessX(i, true), wallThicknessY(i, true), z + Z_STEP)
                        );
                        addFacet(
                                vertex(wallThicknessX(i, false), wallThicknessY(i, false), z),
                                vertex(wallThicknessX(i, true), wallThicknessY(i, true), z + Z_STEP),
                                vertex(wallThicknessX(i, true), wallThicknessY(i, true), z)
                        );
                    }
                }

                addFacet(
                        vertex(wallThicknessX(i, isImage), wallThicknessY(i, isImage), z),
                        vertex(wallThicknessX(i + 1, isImage), wallThicknessY(i + 1, isImage), z),
                        vertex(wallThicknessX(i + 1, isImage), wallThicknessY(i + 1, isImage), z + Z_STEP)
                );

                addFacet(
                        vertex(wallThicknessX(i, isImage), wallThicknessY(i, isImage), z),
                        vertex(wallThicknessX(i + 1, isImage), wallThicknessY(i + 1, isImage), z + Z_STEP),
                        vertex(wallThicknessX(i, isImage), wallThicknessY(i, isImage), z + Z_STEP)
                );
            }

            x++;
        }
        if (lastZ < length()) {
            for (int i = 0; i < rotationSteps(); i++) {
                addFacet(
                        vertex(wallThicknessX(i, false), wallThicknessY(i, false), lastZ),
                        vertex(wallThicknessX(i + 1, false), wallThicknessY(i + 1, false), lastZ),
                        vertex(wallThicknessX(i + 1, false), wallThicknessY(i + 1, false), length())
                );

                addFacet(
                        vertex(wallThicknessX(i, false), wallThicknessY(i, false), lastZ),
                        vertex(wallThicknessX(i + 1, false), wallThicknessY(i + 1, false), length()),
                        vertex(wallThicknessX(i, false), wallThicknessY(i, false), length())
                );
            }
        }


        // bottom
        for (int i = 0; i < rotationSteps(); i++) {
            float x0 = x(i, false);
            float y0 = y(i, false);
            float x1 = x(i + 1, false);
            float y1 = y(i + 1, false);
            float X0 = wallThicknessX(i, false);
            float Y0 = wallThicknessY(i, false);
            float X1 = wallThicknessX(i + 1, false);
            float Y1 = wallThicknessY(i + 1, false);

            if (blankConfiguration.isCenter()) {
                addFacet(
                        vertex(0.0f, 0.0f, bottomZ),
                        vertex(X1, Y1, bottomZ),
                        vertex(X0, Y0, bottomZ)
                );
            } else {
                addFacet(
                        vertex(x0, y0, bottomZ),
                        vertex(X1, Y1, bottomZ),
                        vertex(X0, Y0, bottomZ)
                );
                addFacet(
                        vertex(x0, y0, bottomZ),
                        vertex(x1, y1, bottomZ),
                        vertex(X1, Y1, bottomZ)
                );
            }
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

            if (blankConfiguration.isRoundMold() && !blankConfiguration.isCenter()) {
                break;
            }

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
                    vertex(0.0f, -blankConfiguration.getSquareMold(), length()),
                    vertex(0.0f, -blankConfiguration.getSquareMold(), 0.0f)
            );
            addFacet(
                    vertex(0.0f, -tubeRadius(), 0.0f),
                    vertex(0.0f, -tubeRadius(), length()),
                    vertex(0.0f, -blankConfiguration.getSquareMold(), length())
            );

            addFacet(
                    vertex(0.0f, tubeRadius(), 0.0f),
                    vertex(0.0f, blankConfiguration.getSquareMold(), 0.0f),
                    vertex(0.0f, blankConfiguration.getSquareMold(), length())
            );
            addFacet(
                    vertex(0.0f, tubeRadius(), 0.0f),
                    vertex(0.0f, blankConfiguration.getSquareMold(), length()),
                    vertex(0.0f, tubeRadius(), length())
            );
        }

        if (imageZero() > 0.0f) {
            for (int i = 0; i < steps; i++) {
                addFacet(
                        vertex(x(i, false), y(i, false), 0.0f),
                        vertex(x(i + 1, false), y(i + 1, false), imageZero()),
                        vertex(x(i + 1, false), y(i + 1, false), 0.0f)
                );

                addFacet(
                        vertex(x(i, false), y(i, false), 0.0f),
                        vertex(x(i, false), y(i, false), imageZero()),
                        vertex(x(i + 1, false), y(i + 1, false), imageZero())
                );
            }
        }

        int x = 0;
        float lastZ = 0.0f;
        for (float z = imageZero(); z < (length() - (2 * Z_STEP)); z += Z_STEP) {
            lastZ = z + Z_STEP;

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
        if (lastZ < length()) {
            for (int i = 0; i < steps; i++) {
                // TODO: figure out if we need to do anything for the last line of image, but for now
                // we are going to assume the last line of the image is all at tube depth
                addFacet(
                        vertex(x(i, false), y(i, false), lastZ),
                        vertex(x(i + 1, false), y(i + 1, false), length()),
                        vertex(x(i + 1, false), y(i + 1, false), lastZ)
                );

                addFacet(
                        vertex(x(i, false), y(i, false), lastZ),
                        vertex(x(i, false), y(i, false), length()),
                        vertex(x(i + 1, false), y(i + 1, false), length())
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
                    vertex(0.0f, -tubeRadius(), length()),
                    vertex(0.0f, -imageRadius(), length() + FUNNEL_DEPTH),
                    vertex(0.0f, -blankConfiguration.getSquareMold(), length())
            );
            addFacet(
                    vertex(0.0f, -blankConfiguration.getSquareMold(), length()),
                    vertex(0.0f, -imageRadius(), length() + FUNNEL_DEPTH),
                    vertex(0.0f, -blankConfiguration.getSquareMold(), length() + FUNNEL_DEPTH)
            );

            // right
            addFacet(
                    vertex(0.0f, tubeRadius(), length()),
                    vertex(0.0f, blankConfiguration.getSquareMold(), length()),
                    vertex(0.0f, imageRadius(), length() + FUNNEL_DEPTH)
            );
            addFacet(
                    vertex(0.0f, blankConfiguration.getSquareMold(), length()),
                    vertex(0.0f, blankConfiguration.getSquareMold(), length() + FUNNEL_DEPTH),
                    vertex(0.0f, imageRadius(), length() + FUNNEL_DEPTH)
            );

            // above
            addFacet(
                    vertex(0.0f, blankConfiguration.getSquareMold(), length() + FUNNEL_DEPTH),
                    vertex(0.0f, -blankConfiguration.getSquareMold(), length() + FUNNEL_DEPTH + MOLD_TOP_PADDING),
                    vertex(0.0f, -blankConfiguration.getSquareMold(), length() + FUNNEL_DEPTH)
            );
            addFacet(
                    vertex(0.0f, blankConfiguration.getSquareMold(), length() + FUNNEL_DEPTH),
                    vertex(0.0f, blankConfiguration.getSquareMold(), length() + FUNNEL_DEPTH + MOLD_TOP_PADDING),
                    vertex(0.0f, -blankConfiguration.getSquareMold(), length() + FUNNEL_DEPTH + MOLD_TOP_PADDING)
            );
        }

        if (isFunnel()) {
            for (int i = 0; i < steps; i++) {
                float tx = x(i, false);
                float ty = y(i, false);
                float tX = x(i + 1, false);
                float tY = y(i + 1, false);
                float dx = funnelX(i);
                float dy = funnelY(i);
                float dX = funnelX(i + 1);
                float dY = funnelY(i + 1);
                float z = length();
                float Z = length() + FUNNEL_DEPTH;

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
            if (blankConfiguration.isPositive()) {
                for (int i = 0; i < steps; i++) {
                    float x = x(i, false);
                    float y = y(i, false);
                    float X = x(i + 1, false);
                    float Y = y(i + 1, false);

                    addFacet(
                            vertex(x, y, length()),
                            vertex(0.0f, 0.0f, length()),
                            vertex(X, Y, length())
                    );
                }
            }
        }
    }

    boolean isImage(int x, int y) {
        if (x < 0 || x >= image.getWidth()) {
            return false;
        } else if (y < 0) {
            return isImage(x, image.getHeight() + y);
        } else if (y >= image.getHeight()) {
            return isImage(x, y % image.getHeight());
        } else {
            return image.getRGB(x, y) == -16777216;
        }
    }

    boolean isWithinWallThickness(int x, int y) {
        int xPad = xPad(); // width
        int yPad = yPad(); // height

        for (int i = (x - (xPad / 2)); i < (x + (xPad / 2)); i++) {
            for (int j = (y - (yPad / 2)); j < (y + (yPad / 2)); j++) {
                if (isImage(i, j)) {
                    return true;
                }
            }
        }

        return false;
    }

    int xPad() {
        return (int) (wallThickness() / Z_STEP);
    }

    int yPad() {
        for (int i = 0; i < rotationSteps(); i++) {
            if (distance(x(0, true), y(0, true), x(i, true), y(i, true)) > wallThickness()) {
                return i;
            }
        }

        return rotationSteps();
    }

    float distance(float x0, float y0, float x1, float y1) {
        return (float) Math.sqrt(Math.pow(x1 - x0, 2) + Math.pow(y1 - y0, 2));
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

    boolean isFunnel() {
        return blankConfiguration.isFunnel();
    }

    float roundMoldX(int rotationStep) {
        return roundMoldX[rotationStep % rotationSteps()];
    }

    float roundMoldY(int rotationStep) {
        return roundMoldY[rotationStep % rotationSteps()];
    }

    float wallThicknessX(int rotationStep, boolean isImage) {
        if (isImage) {
            return wallThicknessImageX[rotationStep % rotationSteps()];
        } else {
            return wallThicknessTubeX[rotationStep % rotationSteps()];
        }
    }

    float wallThicknessY(int rotationStep, boolean isImage) {
        if (isImage) {
            return wallThicknessImageY[rotationStep % rotationSteps()];
        } else {
            return wallThicknessTubeY[rotationStep % rotationSteps()];
        }
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

    private float wallThickness() {
        return blankConfiguration.getWallThickness();
    }

    private float imageZero() {
        return blankConfiguration.getImageZero();
    }

    private float length() {
        return blankConfiguration.getLength();
    }

    private int rotationSteps() {
        return image.getHeight();
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
