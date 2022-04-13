package com.jug.config;

import org.apache.commons.cli.*;

import java.io.File;

public class CommandLineArgumentsParser {
    private boolean running_as_Fiji_plugin;
    private File inputFolder;
    /**
     * Property file provided by user through as command-line option.
     */
    private File optionalPropertyFile = null;
    private int userDefinedMinTime;
    private int userDefinedMaxTime;
    // - - - - - - - - - - - - - -
    // Info about loaded data
    // - - - - - - - - - - - - - -
    private int initialOptimizationRange = -1;
    private String STATS_OUTPUT_PATH;
    private boolean GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY; /* variable GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY is a hack to allow loading/reading mm.properties first and then initialize */
    private boolean HEADLESS;


    public CommandLineArgumentsParser(boolean running_as_Fiji_plugin) {
        this.running_as_Fiji_plugin = running_as_Fiji_plugin;
    }

    public void parse(final String[] args){
        // ===== command line parsing ======================================================================

        // create Options object & the parser
        final Options options = new Options();
        final CommandLineParser parser = new DefaultParser();
        // defining command line options
        final Option help = new Option( "help", "print this message" );

        final Option headless = new Option( "h", "headless", false, "start without user interface (note: input-folder must be given!)" );
        headless.setRequired( false );

        final Option groundTruthGeneration = new Option( "gtexport", "ground_truth_export", false, "start user interface with possibility for exporting ground truth frames" );
        groundTruthGeneration.setRequired( false );

        final Option timeFirst = new Option( "tmin", "min_time", true, "first time-point to be processed" );
        timeFirst.setRequired( false );

        final Option timeLast = new Option( "tmax", "max_time", true, "last time-point to be processed" );
        timeLast.setRequired( false );

        final Option optRange = new Option( "optrange", "optimization_range", true, "initial optimization range" );
        optRange.setRequired( false );

        final Option infolder = new Option( "i", "infolder", true, "folder to read data from" );
        infolder.setRequired( false );

        final Option outfolder = new Option( "o", "outfolder", true, "folder to write preprocessed data to (equals infolder if not given)" );
        outfolder.setRequired( false );

        final Option userProps = new Option( "p", "props", true, "properties file to be loaded (mm.properties)" );
        userProps.setRequired( false );

        options.addOption(help);
        options.addOption(headless);
        options.addOption(groundTruthGeneration);
        options.addOption(timeFirst);
        options.addOption(timeLast);
        options.addOption(optRange);
        options.addOption(infolder);
        options.addOption(outfolder);
        options.addOption(userProps);
        // get the commands parsed
        CommandLine cmd = null;
        try {
            cmd = parser.parse( options, args );
        } catch ( final ParseException e1 ) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                    "... [-p props-file] -i in-folder [-o out-folder] [-c <num-channels>] [-tmin idx] [-tmax idx] [-optrange num-frames] [-headless]",
                    "",
                    options,
                    "Error: " + e1.getMessage() );
            if (!running_as_Fiji_plugin) {
                System.exit( 0 );
            } else {
                return;
            }
        }

        if ( cmd.hasOption( "help" ) ) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "... -i <in-folder> -o [out-folder] [-headless]", options );
            if (!running_as_Fiji_plugin) {
                System.exit( 0 );
            } else {
                return;
            }
        }

        if ( cmd.hasOption( "h" ) ) {
            System.out.println( ">>> Starting MM in headless mode." );
            HEADLESS = true;
            if ( !cmd.hasOption( "i" ) ) {
                final HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "Headless-mode requires option '-i <in-folder>'...", options );
                if (!running_as_Fiji_plugin) {
                    System.exit( 0 );
                } else {
                    return;
                }
            }
        }

        if ( cmd.hasOption( "ground_truth_export" ) ) {
            GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY = true;
        }

        if ( cmd.hasOption( "i" ) ) {
            inputFolder = new File( cmd.getOptionValue( "i" ) );
			/*
			if ( !inputFolder.isDirectory() ) {
				System.out.println( "Error: Input folder is not a directory!" );
				if (!running_as_Fiji_plugin) {
					System.exit( 2 );
				} else {
					return;
				}
			}*/
            if ( !inputFolder.canRead() ) {
                System.out.println( "Error: Input folder cannot be read!" );
                if (!running_as_Fiji_plugin) {
                    System.exit( 2 );
                } else {
                    return;
                }
            }
        }

        File outputFolder;
        if ( !cmd.hasOption( "o" ) ) {
            if ( inputFolder == null ) {
                System.out.println( "Error: Output folder would be set to a 'null' input folder! Please check your command line arguments..." );
                if (!running_as_Fiji_plugin) {
                    System.exit( 3 );
                } else {
                    return;
                }
            }
            outputFolder = inputFolder;
            STATS_OUTPUT_PATH = outputFolder.getAbsolutePath();
        } else {
            outputFolder = new File( cmd.getOptionValue( "o" ) );

            if ( !outputFolder.isDirectory() ) {
                System.out.println( "Error: Output folder is not a directory!" );
                if (!running_as_Fiji_plugin) {
                    System.exit( 3 );
                } else {
                    return;
                }
            }
            if ( !outputFolder.canWrite() ) {
                System.out.println( "Error: Output folder cannot be written to!" );
                if (!running_as_Fiji_plugin) {
                    System.exit( 3 );
                } else {
                    return;
                }
            }

            STATS_OUTPUT_PATH = outputFolder.getAbsolutePath();
        }

        if ( cmd.hasOption( "p" ) ) {
            optionalPropertyFile = new File( cmd.getOptionValue( "p" ) );
        }

        if ( cmd.hasOption( "tmin" ) ) {
            userDefinedMinTime = Integer.parseInt( cmd.getOptionValue( "tmin" ) ); /* this has to be a user-setting in mm.properties for reproducibility, when loading previous curations */
        }
        if ( cmd.hasOption( "tmax" ) ) {
            userDefinedMaxTime = Integer.parseInt( cmd.getOptionValue( "tmax" ) ); /* this has to be a user-setting in mm.properties for reproducibility, when loading previous curations */
        }

        if ( cmd.hasOption( "optrange" ) ) {
            initialOptimizationRange = Integer.parseInt( cmd.getOptionValue( "optrange" ) );
        }
    }

    public File getInputFolder() {
        return inputFolder;
    }

    public boolean getIfRunningHeadless() {
        return HEADLESS;
    }

    public File getOptionalPropertyFile(){
        return optionalPropertyFile;
    }

    public int getUserDefinedMinTime() {
        return userDefinedMinTime;
    }

    public int getUserDefinedMaxTime() {
        return userDefinedMaxTime;
    }

    public boolean getShowGroundTruthFunctionality(){
        return GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY;
    }

    /**
     * @return the initial optimization range, -1 if it is infinity.
     */
    public int getInitialOptimizationRange() {
        return initialOptimizationRange;
    }
}
