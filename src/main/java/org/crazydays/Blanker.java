package org.crazydays;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class Blanker {
    public final static float Z_STEP = 0.04f;

    private BlankConfiguration blankConfiguration;
    private Blank blank;

    private BufferedImage image;

    private String outputFilename;

    public Blanker(BlankConfiguration blankConfiguration, String outputFilename) {
        this.blankConfiguration = blankConfiguration;
        this.blank = new Blank();
        this.blank.setHeader(new StlHeader());
        this.outputFilename = outputFilename;
    }

    public void loadImage() {
        try {
            image = ImageIO.read(blankConfiguration.getImagefile());
            System.out.println("image.height: " + image.getHeight());
            System.out.println("image.width: " + image.getWidth());
        } catch (IOException e) {
            System.err.println("IOException: " + e.getLocalizedMessage());
        }
    }

    public void generateStl() {
        System.out.println(String.format("rotation_steps: %d", rotationStep()));

        generateTop();
        generateShaft();
        generateBottom();
    }

    private void generateTop() {
        System.out.println("Top");
        for (int i = 0; i < rotationStep(); i++) {
            float x = (float) Math.sin((2 * Math.PI) / rotationStep() * i) * (blankConfiguration.getTube() / 2);
            float y = (float) Math.cos((2 * Math.PI) / rotationStep() * i) * (blankConfiguration.getTube() / 2);
            float X = (float) Math.sin((2 * Math.PI) / rotationStep() * (i + 1)) * (blankConfiguration.getTube() / 2);
            float Y = (float) Math.cos((2 * Math.PI) / rotationStep() * (i + 1)) * (blankConfiguration.getTube() / 2);

            blank.addFacet(new Facet(new Normal(0.0f, -1.0f, 0.0f), new Vertex(0, 0, 0.0f),  new Vertex(x, y, 0.0f), new Vertex(X, Y, 0.0f),0));
        }
    }

    private void generateShaft() {
        System.out.println("Shaft");
        int imageColumn = 0;

        float[] normalX = new float[rotationStep()];
        float[] normalY = new float[rotationStep()];

        float[] currentLeadingX = new float[rotationStep()];
        float[] currentLeadingY = new float[rotationStep()];
        float[] currentTrailingX = new float[rotationStep()];
        float[] currentTrailingY = new float[rotationStep()];

        float[] lowerLeadingX = new float[rotationStep()];
        float[] lowerLeadingY = new float[rotationStep()];
        float[] lowerTrailingX = new float[rotationStep()];
        float[] lowerTrailingY = new float[rotationStep()];

        float[] upperLeadingX = new float[rotationStep()];
        float[] upperLeadingY = new float[rotationStep()];
        float[] upperTrailingX = new float[rotationStep()];
        float[] upperTrailingY = new float[rotationStep()];

        boolean[] previousRaised = new boolean[rotationStep()];
        boolean[] currentRaised = new boolean[rotationStep()];

        float imageOffset = (blankConfiguration.getLength() - (image.getWidth() * Z_STEP)) / 2;

        for (float z = 0.0f; z < blankConfiguration.getLength(); z += Z_STEP) {
            if (z > imageOffset) {
                imageColumn += 1;
            }

            // generate data
            for (int i = 0; i < rotationStep(); i++) {
                previousRaised[i] = currentRaised[i];

                // TODO: fix normalization calculation
                normalX[i] = (float) Math.sin((2 * Math.PI) / rotationStep() * (i - Z_STEP / 2));
                normalY[i] = (float) Math.cos((2 * Math.PI) / rotationStep() * (i - Z_STEP / 2));

                currentRaised[i] = z > imageOffset && imageColumn < image.getWidth() && i < image.getHeight() && image.getRGB(imageColumn, i) == -16777216;

                upperLeadingX[i] = (float) Math.sin((2 * Math.PI) / rotationStep() * i) * (blankConfiguration.getDiameter() / 2);
                upperLeadingY[i] = (float) Math.cos((2 * Math.PI) / rotationStep() * i) * (blankConfiguration.getDiameter() / 2);
                upperTrailingX[i] = (float) Math.sin((2 * Math.PI) / rotationStep() * (i - 1)) * (blankConfiguration.getDiameter() / 2);
                upperTrailingY[i] = (float) Math.cos((2 * Math.PI) / rotationStep() * (i - 1)) * (blankConfiguration.getDiameter() / 2);
                lowerLeadingX[i] = (float) Math.sin((2 * Math.PI) / rotationStep() * i) * (blankConfiguration.getTube() / 2);
                lowerLeadingY[i] = (float) Math.cos((2 * Math.PI) / rotationStep() * i) * (blankConfiguration.getTube() / 2);
                lowerTrailingX[i] = (float) Math.sin((2 * Math.PI) / rotationStep() * (i - 1)) * (blankConfiguration.getTube() / 2);
                lowerTrailingY[i] = (float) Math.cos((2 * Math.PI) / rotationStep() * (i - 1)) * (blankConfiguration.getTube() / 2);

                if (currentRaised[i]) {
                    currentLeadingX[i] = upperLeadingX[i];
                    currentLeadingY[i] = upperLeadingY[i];
                    currentTrailingX[i] = upperTrailingX[i];
                    currentTrailingY[i] = upperTrailingY[i];
                } else {
                    currentLeadingX[i] = lowerLeadingX[i];
                    currentLeadingY[i] = lowerLeadingY[i];
                    currentTrailingX[i] = lowerTrailingX[i];
                    currentTrailingY[i] = lowerTrailingY[i];
                }
            }

            // add facets
            for (int i = 0; i < rotationStep(); i++) {
                if (currentRaised[i] && !currentRaised[(i + rotationStep() - 1) % rotationStep()]) {
                    // setup up level: top
                    // TODO: invalid normal
                    blank.addFacet(new Facet(new Normal(normalX[i], normalY[i], 0.0f), new Vertex(upperTrailingX[i], upperTrailingY[i], z), new Vertex(lowerTrailingX[i], lowerTrailingY[i], z), new Vertex(upperTrailingX[i], upperTrailingY[i], z + Z_STEP), 0));
                    blank.addFacet(new Facet(new Normal(normalX[i], normalY[i], 0.0f), new Vertex(lowerTrailingX[i], lowerTrailingY[i], z + Z_STEP), new Vertex(upperTrailingX[i], upperTrailingY[i], z + Z_STEP), new Vertex(lowerTrailingX[i], lowerTrailingY[i], z), 0));
                }

                if (currentRaised[i] && !previousRaised[i]) {
                    // step up level: front face
                    blank.addFacet(new Facet(new Normal(0.0f, 0.0f, 1.0f), new Vertex(lowerLeadingX[i], lowerLeadingY[i], z), new Vertex(lowerTrailingX[i], lowerTrailingY[i], z), new Vertex(upperTrailingX[i], upperTrailingY[i], z), 0));
                    blank.addFacet(new Facet(new Normal(0.0f, 0.0f, 1.0f), new Vertex(lowerLeadingX[i], lowerLeadingY[i], z), new Vertex(upperTrailingX[i], upperTrailingY[i], z), new Vertex(upperLeadingX[i], upperLeadingY[i], z), 0));
                }

                if (!currentRaised[i] && previousRaised[i]) {
                    // step down level: back face
                    blank.addFacet(new Facet(new Normal(0.0f, 0.0f, -1.0f), new Vertex(upperTrailingX[i], upperTrailingY[i], z), new Vertex(lowerLeadingX[i], lowerLeadingY[i], z), new Vertex(upperLeadingX[i], upperLeadingY[i], z), 0));
                    blank.addFacet(new Facet(new Normal(0.0f, 0.0f, -1.0f), new Vertex(upperTrailingX[i], upperTrailingY[i], z), new Vertex(lowerTrailingX[i], lowerTrailingY[i], z), new Vertex(lowerLeadingX[i], lowerLeadingY[i], z), 0));
                }

                if (currentRaised[i] && !currentRaised[(i + 1) % rotationStep()]) {
                    // setup up level: top
                    // TODO: invalid normal
                    blank.addFacet(new Facet(new Normal(normalX[i], normalY[i], 0.0f), new Vertex(upperLeadingX[i], upperLeadingY[i], z), new Vertex(upperLeadingX[i], upperLeadingY[i], z + Z_STEP), new Vertex(lowerLeadingX[i], lowerLeadingY[i], z), 0));
                    blank.addFacet(new Facet(new Normal(normalX[i], normalY[i], 0.0f), new Vertex(lowerLeadingX[i], lowerLeadingY[i], z + Z_STEP), new Vertex(lowerLeadingX[i], lowerLeadingY[i], z), new Vertex(upperLeadingX[i], upperLeadingY[i], z + Z_STEP), 0));
                }

                blank.addFacet(new Facet(new Normal(normalX[i], normalY[i], 0.0f), new Vertex(currentLeadingX[i], currentLeadingY[i], z), new Vertex(currentTrailingX[i], currentTrailingY[i], z), new Vertex(currentTrailingX[i], currentTrailingY[i], z + Z_STEP), 0));
                blank.addFacet(new Facet(new Normal(normalX[i], normalY[i], 0.0f), new Vertex(currentLeadingX[i], currentLeadingY[i], z), new Vertex(currentTrailingX[i], currentTrailingY[i], z + Z_STEP), new Vertex(currentLeadingX[i], currentLeadingY[i], z + Z_STEP), 0));
            }
        }
    }

    private void generateBottom() {
        System.out.println("Bottom");
        if (blankConfiguration.getFunnel()) {
            for (int i = 0; i < rotationStep(); i++) {
                float tx = (float) Math.sin((2 * Math.PI) / rotationStep() * i) * (blankConfiguration.getTube() / 2);
                float ty = (float) Math.cos((2 * Math.PI) / rotationStep() * i) * (blankConfiguration.getTube() / 2);
                float tX = (float) Math.sin((2 * Math.PI) / rotationStep() * (i - 1)) * (blankConfiguration.getTube() / 2);
                float tY = (float) Math.cos((2 * Math.PI) / rotationStep() * (i - 1)) * (blankConfiguration.getTube() / 2);
                float dx = (float) Math.sin((2 * Math.PI) / rotationStep() * i) * (blankConfiguration.getDiameter() / 2);
                float dy = (float) Math.cos((2 * Math.PI) / rotationStep() * i) * (blankConfiguration.getDiameter() / 2);
                float dX = (float) Math.sin((2 * Math.PI) / rotationStep() * (i - 1)) * (blankConfiguration.getDiameter() / 2);
                float dY = (float) Math.cos((2 * Math.PI) / rotationStep() * (i - 1)) * (blankConfiguration.getDiameter() / 2);
                float z = blankConfiguration.getLength();
                float Z = blankConfiguration.getLength() + 5.0f;

                // TODO: fix normal
                blank.addFacet(new Facet(new Normal(0.0f, 0.0f, -1.0f), new Vertex(tX, tY, z), new Vertex(dX, dY, Z), new Vertex(tx, ty, z), 0));
                blank.addFacet(new Facet(new Normal(0.0f, 0.0f, -1.0f), new Vertex(dx, dy, Z), new Vertex(tx, ty, z), new Vertex(dX, dY, Z), 0));

                blank.addFacet(new Facet(new Normal(0.0f, 0.0f, 1.0f), new Vertex(0, 0, Z), new Vertex(dx, dy, Z), new Vertex(dX, dY, Z), 0));
            }
        } else {
            for (int i = 0; i < rotationStep(); i++) {
                float x = (float) Math.sin((2 * Math.PI) / rotationStep() * i) * (blankConfiguration.getTube() / 2);
                float y = (float) Math.cos((2 * Math.PI) / rotationStep() * i) * (blankConfiguration.getTube() / 2);
                float X = (float) Math.sin((2 * Math.PI) / rotationStep() * (i + 1)) * (blankConfiguration.getTube() / 2);
                float Y = (float) Math.cos((2 * Math.PI) / rotationStep() * (i + 1)) * (blankConfiguration.getTube() / 2);

                blank.addFacet(new Facet(new Normal(0.0f, 1.0f, 0.0f), new Vertex(0, 0, blankConfiguration.getLength()), new Vertex(X, Y, blankConfiguration.getLength()), new Vertex(x, y, blankConfiguration.getLength()), 0));
            }
        }
    }

    private int rotationStep() {
        return (int) ((Math.PI * blankConfiguration.getImageZero()) / Z_STEP);
    }

    public void writeStl() {
        try {
            OutputStream stream = new FileOutputStream(new File(outputFilename));
            stream.write(blank.getBytes());
            stream.close();
        } catch (FileNotFoundException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    // CUBE
    //        // back
    //        blank.addFacet(new Facet(new Normal(0.0f, -1.0f, 0.0f), new Vertex(0.0f, 0.0f, 0.0f), new Vertex(0.0f, 1.0f, 0.0f), new Vertex(1.0f, 1.0f, 0.0f), 0));
    //        blank.addFacet(new Facet(new Normal(0.0f, -1.0f, 0.0f), new Vertex(0.0f, 0.0f, 0.0f), new Vertex(1.0f, 1.0f, 0.0f), new Vertex(1.0f, 0.0f, 0.0f), 0));
    //
    //        // bottom
    //        blank.addFacet(new Facet(new Normal(0.0f, -1.0f, 0.0f), new Vertex(0.0f, 0.0f, 0.0f), new Vertex(1.0f, 0.0f, 0.0f), new Vertex(1.0f, 0.0f, 1.0f), 0));
    //        blank.addFacet(new Facet(new Normal(0.0f, -1.0f, 0.0f), new Vertex(0.0f, 0.0f, 0.0f), new Vertex(1.0f, 0.0f, 1.0f), new Vertex(0.0f, 0.0f, 1.0f), 0));
    //
    //        // left
    //        blank.addFacet(new Facet(new Normal(-1.0f, 0.0f, 0.0f), new Vertex(0.0f, 0.0f, 0.0f), new Vertex(0.0f, 0.0f, 1.0f), new Vertex(0.0f, 1.0f, 1.0f), 0));
    //        blank.addFacet(new Facet(new Normal(-1.0f, 0.0f, 0.0f), new Vertex(0.0f, 0.0f, 0.0f), new Vertex(0.0f, 1.0f, 1.0f), new Vertex(0.0f, 1.0f, 0.0f), 0));
    //
    //        // top
    //        blank.addFacet(new Facet(new Normal(0.0f, 1.0f, 0.0f), new Vertex(0.0f, 1.0f, 1.0f), new Vertex(1.0f, 1.0f, 0.0f), new Vertex(0.0f, 1.0f, 0.0f), 0));
    //        blank.addFacet(new Facet(new Normal(0.0f, 1.0f, 0.0f), new Vertex(0.0f, 1.0f, 1.0f), new Vertex(1.0f, 1.0f, 1.0f), new Vertex(1.0f, 1.0f, 0.0f), 0));
    //
    //        // right
    //        blank.addFacet(new Facet(new Normal(1.0f, 0.0f, 0.0f), new Vertex(1.0f, 1.0f, 1.0f), new Vertex(1.0f, 0.0f, 0.0f), new Vertex(1.0f, 1.0f, 0.0f), 0));
    //        blank.addFacet(new Facet(new Normal(1.0f, 0.0f, 0.0f), new Vertex(1.0f, 1.0f, 1.0f), new Vertex(1.0f, 0.0f, 1.0f), new Vertex(1.0f, 0.0f, 0.0f), 0));
    //
    //        // front
    //        blank.addFacet(new Facet(new Normal(0.0f, 0.0f, 1.0f), new Vertex(1.0f, 1.0f, 1.0f), new Vertex(0.0f, 0.0f, 1.0f), new Vertex(1.0f, 0.0f, 1.0f), 0));
    //        blank.addFacet(new Facet(new Normal(0.0f, 0.0f, 1.0f), new Vertex(1.0f, 1.0f, 1.0f), new Vertex(0.0f, 1.0f, 1.0f), new Vertex(0.0f, 0.0f, 1.0f), 0));
}
