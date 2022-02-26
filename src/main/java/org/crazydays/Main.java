package org.crazydays;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Main {
    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("blanker").build().defaultHelp(true).description("Prepare pen blank STL with SVG from files");
        parser.addArgument("-u", "--units").choices("inch", "mm").setDefault("mm").help("Units");
        parser.addArgument("-l", "--length").type(Float.class).setDefault(2.0f).help("Blank length");
        parser.addArgument("-t", "--tube").type(Float.class).setDefault(0.25f).help("Tube diameter");
        parser.addArgument("-d", "--diameter").type(Float.class).setDefault(0.75).help("Blank diameter");
        parser.addArgument("-z", "--imageZero").type(Float.class).setDefault(.50).help("Image zero");
        parser.addArgument("-c", "--center").type(Boolean.class).setDefault(true).help("Mold center");
        parser.addArgument("-f", "--funnel").type(Float.class).setDefault(0.0f).help("Pour funnel diameter");
        parser.addArgument("-m", "--mold").type(Float.class).setDefault(0.0f).help("Tube mold diameter");
        parser.addArgument("-s", "--squareMold").type(Float.class).setDefault(0.0f).help("Square mold width");
        parser.addArgument("-p", "--positive").type(Boolean.class).setDefault(true).help("Positive");
        parser.addArgument("-w", "--wallThickness").type(Float.class).setDefault(0.0f).help("Wall Thickness");
        parser.addArgument("-i", "--image").type(String.class).help("SVG image file");
        parser.addArgument("-o", "--output").type(String.class).setDefault("blank.stl").help("Output STL file");

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        String rawUnits = ns.getString("units");
        Units units = null;
        if (rawUnits.equals("inch")) {
            units = Units.INCHES;
        } else if (rawUnits.equals("mm")) {
            units = Units.MILLIMETERS;
        } else {
            System.err.printf("Unknown units: %s%n", rawUnits);
            System.exit(-1);
        }

        float length = ns.getFloat("length");
        float tube = ns.getFloat("tube");
        float diameter = ns.getFloat("diameter");

        boolean center = ns.getBoolean("center");
        float funnel = ns.getFloat("funnel");
        float roundMold = ns.getFloat("mold");
        float squareMold = ns.getFloat("squareMold");
        boolean positive = ns.getBoolean("positive");
        float wallThickness = ns.getFloat("wallThickness");

        float imageZero = ns.getFloat("imageZero");
        String imageFilename = ns.getString("image");

        String outputFilename = ns.getString("output");

        System.out.format("Generating blank\n");
        System.out.format("\tUnits: %s\n", units.name());
        System.out.format("\tLength: %.3f\n", length);
        System.out.format("\tTube: %.3f\n", tube);
        System.out.format("\tDiameter: %.3f\n", diameter);
        System.out.format("\tCenter: %b\n", center);
        System.out.format("\tFunnel: %.3f\n", funnel);
        System.out.format("\tRound Mold: %.3f\n", roundMold);
        System.out.format("\tSquare Mold: %.3f\n", squareMold);
        System.out.format("\tPositive: %b\n", positive);
        System.out.format("\tWall Thickness: %.3f\n", wallThickness);
        System.out.format("\tImageZero: %.3f\n", imageZero);
        System.out.format("\tImageFilename: %s\n", imageFilename);
        System.out.format("\tOutputFilename: %s\n", outputFilename);

        BlankConfiguration blankConfiguration = new BlankConfiguration(units, length, tube, diameter, center, funnel, roundMold, squareMold, positive, wallThickness);
        blankConfiguration.setImageZero(imageZero);
        blankConfiguration.setImageFilename(imageFilename);

        Blanker blanker = new Blanker(blankConfiguration, outputFilename);
        blanker.loadImage();
        blanker.generateStl();
        blanker.writeStl();
    }
}
