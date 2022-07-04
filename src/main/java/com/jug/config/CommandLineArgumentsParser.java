package com.jug.config;

import org.apache.commons.cli.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CommandLineArgumentsParser {
    private boolean runningAsFijiPlugin;
    private Path inputFolder;
    /**
     * Property file provided by user through as command-line option.
     */
    private Path optionalPropertyFile = null;
    private String reloadFolderPath = null;
    private int userDefinedMinTime = -1;
    private int userDefinedMaxTime = -1;
    // - - - - - - - - - - - - - -
    // Info about loaded data
    // - - - - - - - - - - - - - -
    private Path outputPath;
    private boolean GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY; /* variable GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY is a hack to allow loading/reading mm.properties first and then initialize */
    private boolean HEADLESS;

    private boolean reloadingData;
    private boolean trackOnly;

    public void setRunningAsFijiPlugin(boolean runningAsFijiPlugin){
        this.runningAsFijiPlugin = runningAsFijiPlugin;
    }

    public boolean getRunningAsFijiPlugin() {
        return runningAsFijiPlugin;
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

        final Option reloadOption = new Option( "rl", "reload", true, "reloads previously curated data; any additional arguments will be ignored" );

        final Option trackOnlyOption = new Option("trk", "trackonly", false, "run and save tracking without exporting cell measurement; must be combined with option -headless");

        final Option groundTruthGeneration = new Option( "gtexport", "ground_truth_export", false, "start user interface with possibility for exporting ground truth frames" );
        groundTruthGeneration.setRequired( false );

        final Option timeFirst = new Option( "tmin", "min_time", true, "first time-point to be processed" );
        timeFirst.setRequired( false );

        final Option timeLast = new Option( "tmax", "max_time", true, "last time-point to be processed" );
        timeLast.setRequired( false );

        final Option infolder = new Option( "i", "infolder", true, "folder to read data from" );
        infolder.setRequired( false );

        final Option outfolder = new Option( "o", "outfolder", true, "folder to write preprocessed data to (equals infolder if not given)" );
        outfolder.setRequired( false );

        final Option userProps = new Option( "p", "props", true, "properties file to be loaded (mm.properties)" );
        userProps.setRequired( false );

        options.addOption(help);
        options.addOption(headless);
        options.addOption(reloadOption);
        options.addOption(trackOnlyOption);
        options.addOption(groundTruthGeneration);
        options.addOption(timeFirst);
        options.addOption(timeLast);
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
                    "... [-p props-file] [-i in-folder] [-o out-folder] [-c <num-channels>] [-tmin idx] [-tmax idx] [-headless]",
                    "",
                    options,
                    "Error: " + e1.getMessage() );
            if (!runningAsFijiPlugin) {
                System.exit( 0 );
            } else {
                return;
            }
        }

        if ( cmd.hasOption( "help" ) ) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "... -i <in-folder> -o [out-folder] [-headless]", options );
            if (!runningAsFijiPlugin) {
                System.exit( 0 );
            } else {
                return;
            }
        }

        if ( cmd.hasOption( "h" ) ) {
            System.out.println( ">>> Starting MM in headless mode." );
            HEADLESS = true;
            if (!(cmd.hasOption("i") || cmd.hasOption("reload"))) {
                final HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "Headless-mode requires option '-i <in-folder>' or '-reload <in-folder>'", options );
                if (!runningAsFijiPlugin) {
                    System.exit( 0 );
                } else {
                    return;
                }
            }
        }

        if ( cmd.hasOption( "ground_truth_export" ) ) {
            GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY = true;
        }

        if (cmd.hasOption("reload")) {
            reloadingData = true;
            reloadFolderPath = cmd.getOptionValue("reload");
            return; /* if we are reloading previous analysis we do not need to read the cmd arguments below, which are mutually exclusive to reloading */
        }

        if (cmd.hasOption("trackonly")) {
            if (!cmd.hasOption("headless")) {
                System.out.println("Error: Option -trackonly must be combined with -headless.");
                System.exit(-1);
            }
            trackOnly = true;
        }

        if ( cmd.hasOption( "i" ) ) {
            inputFolder = Paths.get(cmd.getOptionValue("i"));
			/*
			if ( !inputFolder.isDirectory() ) {
				System.out.println( "Error: Input folder is not a directory!" );
				if (!running_as_Fiji_plugin) {
					System.exit( 2 );
				} else {
					return;
				}
			}*/
            if ( !Files.isReadable(inputFolder) ) {
                System.out.println( "Error: Input folder cannot be read!" );
                if (!runningAsFijiPlugin) {
                    System.exit( 2 );
                } else {
                    return;
                }
            }
        }

        Path outputFolder;
        if ( !cmd.hasOption( "o" ) ) {
            if ( inputFolder == null ) {
                System.out.println( "Error: Input folder not specified. Please use the -i argument to do so and check your command line arguments." );
                if (!runningAsFijiPlugin) {
                    System.exit( 3 );
                } else {
                    return;
                }
            }
            outputFolder = inputFolder;
            outputPath = outputFolder.toAbsolutePath();
        } else {
            outputFolder = Paths.get( cmd.getOptionValue( "o" ) );

            if ( !Files.isDirectory(outputFolder) ) {
                System.out.println( "Error: Output folder is not a directory." );
                if (!runningAsFijiPlugin) {
                    System.exit( 3 );
                } else {
                    return;
                }
            }
            if ( !Files.isWritable(outputFolder) ) {
                System.out.println( "Error: Output folder cannot be written to." );
                if (!runningAsFijiPlugin) {
                    System.exit( 3 );
                } else {
                    return;
                }
            }

            outputPath = outputFolder.toAbsolutePath();
        }

        if ( cmd.hasOption( "p" ) ) {
            optionalPropertyFile = Paths.get( cmd.getOptionValue( "p" ) );
        }

        if ( cmd.hasOption( "tmin" ) ) {
            userDefinedMinTime = Integer.parseInt( cmd.getOptionValue( "tmin" ) ); /* this has to be a user-setting in mm.properties for reproducibility, when loading previous curations */
        }
        if ( cmd.hasOption( "tmax" ) ) {
            userDefinedMaxTime = Integer.parseInt( cmd.getOptionValue( "tmax" ) ); /* this has to be a user-setting in mm.properties for reproducibility, when loading previous curations */
        }
    }

    public Path getInputFolder() {
        return inputFolder;
    }

    public boolean getIfRunningHeadless() {
        return HEADLESS;
    }

    public Path getOptionalPropertyFile(){
        return optionalPropertyFile;
    }

    public int getUserDefinedMinTime() {
        return userDefinedMinTime;
    }

    public int getUserDefinedMaxTime() { return userDefinedMaxTime; }

    public boolean getShowGroundTruthFunctionality(){
        return GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public boolean isReloadingData() {
        return reloadingData;
    }

    public boolean isTrackOnly() {
        return trackOnly;
    }

    public String getReloadFolderPath() {
        return reloadFolderPath;
    }
}
