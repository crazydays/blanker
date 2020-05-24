package org.crazydays;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Main {
    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("blanker").build().defaultHelp(true).description("Prepare pen blank STL with SVG from files");
        parser.addArgument("-u", "--units").choices("inch", "mm").setDefault("inch").help("Units");
        parser.addArgument("-l", "--length").type(Float.class).setDefault(2.0f).help("Blank length");
        parser.addArgument("-t", "--tube").type(Float.class).setDefault(0.25f).help("Tube diameter");
        parser.addArgument("-d", "--diameter").type(Float.class).setDefault(0.75).help("Blank diameter");
        parser.addArgument("-z", "--imageZero").type(Float.class).setDefault(.50).help("Image zero");
        parser.addArgument("-f", "--funnel").type(Boolean.class).setDefault(true).help("Pour funnel");
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
        Units units = Units.INCHES;
        if (rawUnits.equals("inch")) {
            units = Units.INCHES;
        } else if (rawUnits.equals("mm")) {
            units = Units.MILLIMETERS;
        }
        float length = ns.getFloat("length");
        float tube = ns.getFloat("tube");
        float diameter = ns.getFloat("diameter");

        boolean funnel = ns.getBoolean("funnel");

        float imageZero = ns.getFloat("imageZero");
        String imageFilename = ns.getString("image");

        String outputFilename = ns.getString("output");

        System.out.println("Generating blank");
        System.out.println("\tUnits: " + units.name());
        System.out.println(String.format("\tLength: %.3f", length));
        System.out.println(String.format("\tTube: %.3f", tube));
        System.out.println(String.format("\tDiameter: %.3f", diameter));
        System.out.println(String.format("\tFunnel: %b", funnel));
        System.out.println(String.format("\tImageZero: %.3f", imageZero));
        System.out.println(String.format("\tImageFilename: %s", imageFilename));
        System.out.println(String.format("\tOutputFilename: %s", outputFilename));

        BlankConfiguration blankConfiguration = new BlankConfiguration(units, length, tube, diameter, funnel);
        blankConfiguration.setImageZero(imageZero);
        blankConfiguration.setImageFilename(imageFilename);

        Blanker blanker = new Blanker(blankConfiguration, outputFilename);
        blanker.loadImage();
        blanker.generateStl();
        blanker.writeStl();
    }
}
