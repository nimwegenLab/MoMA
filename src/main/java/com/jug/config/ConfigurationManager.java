package com.jug.config;

import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.jug.MoMA.*;
import static com.jug.development.featureflags.FeatureFlags.featureFlagDisableMaxCellDrop;


public class ConfigurationManager {
    public static int DEFAULT_GUI_POS_X = 100;
    private static File currentPropertyFile;

    /*********************************** CONFIG VALUES DEFINITION START ***********************************************/

    /**
     * Parameter: how many pixels wide is the image containing the selected
     * Growthlane?
     */
    public static int GL_WIDTH_IN_PIXELS = 20;

    public static int INTENSITY_FIT_RANGE_IN_PIXELS = 100;
    public static final int GL_PIXEL_PADDING_IN_VIEWS = 15;

    public static List<String> CELL_LABEL_LIST = new ArrayList<>(Arrays.asList("dead", "dying", "fading"));
    public static String CELL_LABELS = "dead;dying;fading";

    /**
     * Vertical center position on which the exit range defined with COMPONENT_EXIT_RANGE is centered.
     */
    public static int GL_OFFSET_TOP = 65;

    /**
     * Range over which the component cost is increased, when exiting the growthlane. This range is centered on
     * at the vertical position defined by GL_OFFSET_TOP.
     */
    public static float COMPONENT_EXIT_RANGE = 50;

    /**
     * String pointing at the U-Net TensorFlow model file that should be used for
     * classification during segmentation.
     */
    public static String SEGMENTATION_MODEL_PATH = "";

    /**
     *
     */
    public static int OPTIMISATION_INTERVAL_LENGTH = -1;

    /**
     * Properties for fitting the Cauchy/Mixture model to the intensity profile.
     */
    public static int INTENSITY_FIT_ITERATIONS = 1000; /* Number of iterations performed during fit. */
    public static double INTENSITY_FIT_PRECISION = 1e-3; /* Precision for which fitting will be finished. */
    public static double INTENSITY_FIT_INITIAL_WIDTH = 5.5; /* Starting width for the fit. */

    /**
     * One of the test for paper:
     * What happens if exit constraints are NOT part of the model?
     */
    public static final boolean DISABLE_EXIT_CONSTRAINTS = false;

    public static int MAX_CELL_DROP = -1; /* value is set using feature flag featureFlagUseMaxCellDrop */

    public static final float MAXIMUM_GROWTH_PER_FRAME = 0.2f;
    public static final float MAXIMUM_SHRINKAGE_PER_FRAME = 0.2f;

    /**
     * X-position of the main GUI-window. This value will be loaded from and
     * stored in the properties file!
     */
    public static int GUI_POS_X;

    /**
     * Y-position of the main GUI-window. This value will be loaded from and
     * stored in the properties file!
     */
    public static int GUI_POS_Y;

    /**
     * Width (in pixels) of the main GUI-window. This value will be loaded from
     * and stored in the properties file!
     */
    public static int GUI_WIDTH = 620;

    /**
     * Width (in pixels) of the main GUI-window. This value will be loaded from
     * and stored in the properties file!
     */
    public static int GUI_HEIGHT = 740;

    /**
     * Width (in pixels) of the console window. This value will be loaded from
     * and stored in the properties file!
     */
    public static int GUI_CONSOLE_WIDTH = 600;


    /**
     * Defines the number of time steps that will be shown side-by-side in the GUI.
     */
    public static int GUI_NUMBER_OF_SHOWN_TIMESTEPS = 7;

    /**
     * Shortest time in which we can expect a cell-doubling provided as number of frames.
     */
    public static double MAXIMUM_GROWTH_RATE = 1.5;

    /*********************************** CONFIG VALUES DEFINITION END *************************************************/

    public static void load(File optionalPropertyFile, File userMomaHomePropertyFile, File momaUserDirectory){
        props = loadParams(optionalPropertyFile, userMomaHomePropertyFile, momaUserDirectory);
        GL_WIDTH_IN_PIXELS = Integer.parseInt( props.getProperty( "GL_WIDTH_IN_PIXELS", Integer.toString( GL_WIDTH_IN_PIXELS ) ) );
        INTENSITY_FIT_RANGE_IN_PIXELS = Integer.parseInt( props.getProperty( "INTENSITY_FIT_RANGE_IN_PIXELS", Integer.toString( INTENSITY_FIT_RANGE_IN_PIXELS ) ) );
        GL_OFFSET_TOP = Integer.parseInt( props.getProperty( "GL_OFFSET_TOP", Integer.toString( GL_OFFSET_TOP ) ) );
        MAXIMUM_GROWTH_RATE = Double.parseDouble( props.getProperty( "MAXIMUM_GROWTH_RATE", Double.toString(MAXIMUM_GROWTH_RATE) ) );
        SEGMENTATION_MODEL_PATH = props.getProperty( "SEGMENTATION_MODEL_PATH", SEGMENTATION_MODEL_PATH);
        DEFAULT_PATH = props.getProperty( "DEFAULT_PATH", DEFAULT_PATH );
        CELL_LABELS = props.getProperty( "CELL_LABELS", CELL_LABELS);
        CELL_LABEL_LIST = new ArrayList<>(Arrays.asList(CELL_LABELS.split(";")));

        GUROBI_TIME_LIMIT = Double.parseDouble( props.getProperty( "GUROBI_TIME_LIMIT", Double.toString( GUROBI_TIME_LIMIT ) ) );
        GUROBI_MAX_OPTIMALITY_GAP = Double.parseDouble( props.getProperty( "GUROBI_MAX_OPTIMALITY_GAP", Double.toString( GUROBI_MAX_OPTIMALITY_GAP ) ) );

		/*
		  Default x-position of the main GUI-window. This value will be used if the
		  values in the properties file are not fitting on any of the currently
		  attached screens.
		 */
        GUI_POS_X = Integer.parseInt( props.getProperty( "GUI_POS_X", Integer.toString(DEFAULT_GUI_POS_X) ) );
        GUI_POS_Y = Integer.parseInt( props.getProperty( "GUI_POS_Y", Integer.toString(DEFAULT_GUI_POS_X) ) );
        GUI_WIDTH = Integer.parseInt( props.getProperty( "GUI_WIDTH", Integer.toString( GUI_WIDTH ) ) );
        GUI_HEIGHT = Integer.parseInt( props.getProperty( "GUI_HEIGHT", Integer.toString( GUI_HEIGHT ) ) );
        GUI_CONSOLE_WIDTH = Integer.parseInt( props.getProperty( "GUI_CONSOLE_WIDTH", Integer.toString( GUI_CONSOLE_WIDTH ) ) );
        GUI_NUMBER_OF_SHOWN_TIMESTEPS = Integer.parseInt( props.getProperty( "GUI_NUMBER_OF_SHOWN_TIMESTEPS", Integer.toString(GUI_NUMBER_OF_SHOWN_TIMESTEPS) ) );
        OPTIMISATION_INTERVAL_LENGTH = Integer.parseInt( props.getProperty( "OPTIMISATION_INTERVAL_LENGTH", Integer.toString(OPTIMISATION_INTERVAL_LENGTH) ));
        INTENSITY_FIT_ITERATIONS = Integer.parseInt(props.getProperty("INTENSITY_FIT_ITERATIONS", Integer.toString(INTENSITY_FIT_ITERATIONS)));
        INTENSITY_FIT_PRECISION = Double.parseDouble(props.getProperty("INTENSITY_FIT_PRECISION", Double.toString(INTENSITY_FIT_PRECISION)));
        INTENSITY_FIT_INITIAL_WIDTH = Double.parseDouble(props.getProperty("INTENSITY_FIT_INITIAL_WIDTH", Double.toString(INTENSITY_FIT_INITIAL_WIDTH)));

        /* process feature flags */
        if(featureFlagDisableMaxCellDrop)
        {
            ConfigurationManager.MAX_CELL_DROP = Integer.MAX_VALUE; // [px]; not in Props; if vertical distance between two Hyps is larger than this, the corresponding assignment never exists!!! (see e.g. addMappingAssignments)
        }
        else
        {
            ConfigurationManager.MAX_CELL_DROP = 50; // [px]; not in Props; if vertical distance between two Hyps is larger than this, the corresponding assignment never exists!!! (see e.g. addMappingAssignments)
        }

    }

    /**
     * Loads the file 'mm.properties' and returns an instance of
     * {@link Properties} containing the key-value pairs found in that file.
     *
     * @return instance of {@link Properties} containing the key-value pairs
     *         found in that file.
     */
    private static Properties loadParams(File optionalPropertyFile, File userMomaHomePropertyFile, File momaUserDirectory) {
        if (optionalPropertyFile != null) {
            if (optionalPropertyFile.exists() && optionalPropertyFile.isFile()) {
                try {
                    props = loadParameters(optionalPropertyFile);
                    currentPropertyFile = optionalPropertyFile;
                    return props;
                } catch (IOException e) {
                }
            } else {
                System.out.println("ERROR: The optional config file path does not exist:" + optionalPropertyFile.getPath());
            }
        }

        if(!userMomaHomePropertyFile.exists())
        {
            if(!momaUserDirectory.exists())
            {
                momaUserDirectory.mkdir();
            }
            // HERE WE NEED TO SETUP THE HOME-DIRECTORY SETTINGS, IF THEY DO NOT EXIST
            final File f = new File( "mm.properties" );
            if(f.isFile()){
                try {
                    FileUtils.copyFile(f, userMomaHomePropertyFile);
                } catch (IOException e) {
                    System.out.println( "Failed to copy user file to default property file to user moma directory: " +  momaUserDirectory.getPath());
                    e.printStackTrace();
                }
            }
            System.out.println( "Performed setup of new default property file here: " + userMomaHomePropertyFile.getAbsolutePath() );
        }

        try {
            currentPropertyFile = userMomaHomePropertyFile;
            return loadParameters(userMomaHomePropertyFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("ERROR: Failed to load property file.");
        return props;
    }

    /**
     * Load parameters from the provided config-file.
     * @param configFile
     * @return
     * @throws IOException
     */
    private static Properties loadParameters(File configFile) throws IOException {
        FileInputStream is = new FileInputStream(configFile);
        final Properties props = new Properties();
        props.load( is );
        return props;
    }

    /**
     * Save parameters to the currently used config file.
     */
    public static void saveParams(JFrame guiFrame) {
        saveParams(currentPropertyFile, guiFrame);
    }

    /**
     * Saves a file 'mm.properties' in the current folder. This file contains
     * all MotherMachine specific properties as key-value pairs.
     *
     */
    public static void saveParams(final File f, JFrame guiFrame) {
        try {
            final OutputStream out = new FileOutputStream( f );

            props.setProperty( "GL_WIDTH_IN_PIXELS", Integer.toString( GL_WIDTH_IN_PIXELS ) );
            props.setProperty( "INTENSITY_FIT_RANGE_IN_PIXELS", Integer.toString( INTENSITY_FIT_RANGE_IN_PIXELS ) );
            props.setProperty( "GL_OFFSET_TOP", Integer.toString( GL_OFFSET_TOP ) );
            props.setProperty( "MAXIMUM_GROWTH_RATE", Double.toString(MAXIMUM_GROWTH_RATE) );
            props.setProperty( "SEGMENTATION_MODEL_PATH", SEGMENTATION_MODEL_PATH);
            props.setProperty( "DEFAULT_PATH", DEFAULT_PATH );

            props.setProperty( "GUROBI_TIME_LIMIT", Double.toString( GUROBI_TIME_LIMIT ) );
            props.setProperty( "GUROBI_MAX_OPTIMALITY_GAP", Double.toString( GUROBI_MAX_OPTIMALITY_GAP ) );

            if ( guiFrame != null ) {
                GUI_POS_X = guiFrame.getX();
                GUI_POS_Y = guiFrame.getY();
                GUI_WIDTH = guiFrame.getWidth();
                GUI_HEIGHT = guiFrame.getHeight();
            }

            props.setProperty( "GUI_POS_X", Integer.toString( GUI_POS_X ) );
            props.setProperty( "GUI_POS_Y", Integer.toString( GUI_POS_Y ) );
            props.setProperty( "GUI_WIDTH", Integer.toString( GUI_WIDTH ) );
            props.setProperty( "GUI_HEIGHT", Integer.toString( GUI_HEIGHT ) );
            props.setProperty( "GUI_CONSOLE_WIDTH", Integer.toString( GUI_CONSOLE_WIDTH ) );

            props.setProperty("OPTIMISATION_INTERVAL_LENGTH", Integer.toString(OPTIMISATION_INTERVAL_LENGTH));

            props.setProperty("INTENSITY_FIT_ITERATIONS", Integer.toString(INTENSITY_FIT_ITERATIONS));
            props.setProperty("INTENSITY_FIT_PRECISION", Double.toString(INTENSITY_FIT_PRECISION));
            props.setProperty("INTENSITY_FIT_INITIAL_WIDTH", Double.toString(INTENSITY_FIT_INITIAL_WIDTH));

            props.store( out, "MotherMachine properties" );
        } catch ( final Exception e ) {
            e.printStackTrace();
        }
    }
}
