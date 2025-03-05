package com.wsdltoswagger;

import org.apache.commons.cli.*;
import com.wsdltoswagger.converter.WSDLConverter;
import com.wsdltoswagger.util.Logger;
import java.io.File;

public class Main {
    private static final Logger logger = new Logger(Main.class);

    public static void main(String[] args) {
        Options options = new Options();

        Option input = Option.builder("i")
                .longOpt("input")
                .desc("Input WSDL file path")
                .hasArg()
                .required(true)
                .build();

        Option output = Option.builder("o")
                .longOpt("output")
                .desc("Output Swagger file path")
                .hasArg()
                .required(true)
                .build();

        options.addOption(input);
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);
            String inputPath = cmd.getOptionValue("input");
            String outputPath = cmd.getOptionValue("output");

            validateInputFile(inputPath);

            WSDLConverter converter = new WSDLConverter();
            converter.convert(inputPath, outputPath);

            logger.info("Conversion completed successfully!");

        } catch (ParseException e) {
            logger.error("Error parsing command line arguments: " + e.getMessage());
            formatter.printHelp("wsdl2swagger", options);
            System.exit(1);
        } catch (Exception e) {
            logger.error("Conversion failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void validateInputFile(String inputPath) throws IllegalArgumentException {
        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            throw new IllegalArgumentException("Input file does not exist: " + inputPath);
        }
        if (!inputFile.isFile()) {
            throw new IllegalArgumentException("Input path is not a file: " + inputPath);
        }
        if (!inputPath.toLowerCase().endsWith(".wsdl")) {
            throw new IllegalArgumentException("Input file must have .wsdl extension");
        }
    }
}
