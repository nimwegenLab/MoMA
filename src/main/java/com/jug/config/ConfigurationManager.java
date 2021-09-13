package com.jug.config;

//import com.jug.MoMA;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.jug.MoMA.*;


public class ConfigurationManager {
    public static int DEFAULT_GUI_POS_X = 100;
    private static File currentPropertyFile;

    /*********************************** CONFIG VALUES DEFINITION START ***********************************************/

//    /**
//     * Maximum offset in x direction (with respect to growth line center) to
//     * take the background intensities from that will be subtracted from the
//     * growth line.
//     */
//    public static int BGREM_TEMPLATE_XMAX = 35;
//
//    /**
//     * Minimum offset in x direction (with respect to growth line center) to
//     * take the background intensities from that will be subtracted from the
//     * growth line.
//     */
//    public static int BGREM_TEMPLATE_XMIN = 20;
//
//    /**
//     * Offsets in +- x direction (with respect to growth line center) where the
//     * measured background values will be subtracted from.
//     */
//    public static int BGREM_X_OFFSET = 35;

    /**
     * Parameter: how many pixels wide is the image containing the selected
     * GrowthLine?
     */
    public static int GL_WIDTH_IN_PIXELS = 20;

    /**
     * Parameter: sigma for gaussian blurring in x-direction of the raw image
     * data. Used while searching the gaps between bacteria.
     */
    public static float SIGMA_PRE_SEGMENTATION_X = 0f;
    public static float SIGMA_PRE_SEGMENTATION_Y = 0f;

    public static int INTENSITY_FIT_RANGE_IN_PIXELS = 100;
    public static final int GL_PIXEL_PADDING_IN_VIEWS = 15;
    public static int MOTHER_CELL_BOTTOM_TRICK_MAX_PIXELS = 10;

    public static List<String> CELL_LABEL_LIST = new ArrayList<>(Arrays.asList("dead", "dying", "fading"));
    public static String CELL_LABELS = "dead;dying;fading";

//    /**
//     * Parameter: sigma for gaussian blurring in x-direction of the raw image
//     * data. Used while searching the growth line centers.
//     */
//    public static float SIGMA_GL_DETECTION_X = 20f;
//    public static float SIGMA_GL_DETECTION_Y = 0f;

    /**
     * Parameter: later border in pixels - well centers detected too close to
     * the left and right image border will be neglected. Reason: detection not
     * reliable if well is truncated.
     */
    public static int GL_OFFSET_LATERAL = 20;

    /**
     * Vertical center position on which the exit range defined with {@link COMPONENT_EXIT_RANGE} is centered.
     */
    public static int GL_OFFSET_TOP = 65;

    /**
     * Range over which the component cost is increased, when exiting the growthlane. This range is centered on
     * at the vertical position defined by {@link GL_OFFSET_TOP}.
     */
    public static float COMPONENT_EXIT_RANGE = 50;

    /**
     * Prior knowledge: minimal length of detected cells
     */
    public static int MIN_CELL_LENGTH = 18;

    /**
     * Prior knowledge: minimal contrast of a gap (also used for MSERs)
     */
    public static float MIN_GAP_CONTRAST = 0.02f; // This is set to a very low
    // value that will basically
    // not filter anything...

    /**
     * When using the learned classification boosted paramaxflow segmentation,
     * how much of the midline data obtained by the 'simple' linescan +
     * component tree segmentation should mix in? Rational: if the
     * classification is flat, the original (simple) mehod might still offer
     * some modulation!
     */
    public static float SEGMENTATION_MIX_CT_INTO_PMFRF = 0.25f;

    /**
     * String pointing at the weka-segmenter model file that should be used for
     * classification during segmentation.
     */
    public static String SEGMENTATION_MODEL_PATH = "CellGapClassifier.model";

    /**
     * Global switches for export options
     */
    public static boolean EXPORT_DO_TRACK_EXPORT = false;
    public static boolean EXPORT_USER_INPUTS = true;
    public static boolean EXPORT_INCLUDE_HISTOGRAMS = false;
    public static boolean EXPORT_INCLUDE_QUANTILES = false;
    public static boolean EXPORT_INCLUDE_COL_INTENSITY_SUMS = true;
    public static boolean EXPORT_INCLUDE_PIXEL_INTENSITIES = false;

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

    /*********************************** CONFIG VALUES DEFINITION END *************************************************/

    public static void load(File optionalPropertyFile, File userMomaHomePropertyFile, File momaUserDirectory){
        props = loadParams(optionalPropertyFile, userMomaHomePropertyFile, momaUserDirectory);
//        BGREM_TEMPLATE_XMIN = Integer.parseInt( props.getProperty( "BGREM_TEMPLATE_XMIN", Integer.toString( BGREM_TEMPLATE_XMIN ) ) );
//        BGREM_TEMPLATE_XMAX = Integer.parseInt( props.getProperty( "BGREM_TEMPLATE_XMAX", Integer.toString( BGREM_TEMPLATE_XMAX ) ) );
//        BGREM_X_OFFSET = Integer.parseInt( props.getProperty( "BGREM_X_OFFSET", Integer.toString( BGREM_X_OFFSET ) ) );
        GL_WIDTH_IN_PIXELS = Integer.parseInt( props.getProperty( "GL_WIDTH_IN_PIXELS", Integer.toString( GL_WIDTH_IN_PIXELS ) ) );
        MOTHER_CELL_BOTTOM_TRICK_MAX_PIXELS = Integer.parseInt( props.getProperty( "MOTHER_CELL_BOTTOM_TRICK_MAX_PIXELS", Integer.toString( MOTHER_CELL_BOTTOM_TRICK_MAX_PIXELS ) ) );
        INTENSITY_FIT_RANGE_IN_PIXELS = Integer.parseInt( props.getProperty( "INTENSITY_FIT_RANGE_IN_PIXELS", Integer.toString( INTENSITY_FIT_RANGE_IN_PIXELS ) ) );
        GL_OFFSET_TOP = Integer.parseInt( props.getProperty( "GL_OFFSET_TOP", Integer.toString( GL_OFFSET_TOP ) ) );
        GL_OFFSET_LATERAL = Integer.parseInt( props.getProperty( "GL_OFFSET_LATERAL", Integer.toString( GL_OFFSET_LATERAL ) ) );
        MIN_CELL_LENGTH = Integer.parseInt( props.getProperty( "MIN_CELL_LENGTH", Integer.toString( MIN_CELL_LENGTH ) ) );
        MIN_GAP_CONTRAST = Float.parseFloat( props.getProperty( "MIN_GAP_CONTRAST", Float.toString( MIN_GAP_CONTRAST ) ) );
        SIGMA_PRE_SEGMENTATION_X = Float.parseFloat( props.getProperty( "SIGMA_PRE_SEGMENTATION_X", Float.toString( SIGMA_PRE_SEGMENTATION_X ) ) );
        SIGMA_PRE_SEGMENTATION_Y = Float.parseFloat( props.getProperty( "SIGMA_PRE_SEGMENTATION_Y", Float.toString( SIGMA_PRE_SEGMENTATION_Y ) ) );
//        SIGMA_GL_DETECTION_X = Float.parseFloat( props.getProperty( "SIGMA_GL_DETECTION_X", Float.toString( SIGMA_GL_DETECTION_X ) ) );
//        SIGMA_GL_DETECTION_Y = Float.parseFloat( props.getProperty( "SIGMA_GL_DETECTION_Y", Float.toString( SIGMA_GL_DETECTION_Y ) ) );
        SEGMENTATION_MIX_CT_INTO_PMFRF = Float.parseFloat( props.getProperty( "SEGMENTATION_MIX_CT_INTO_PMFRF", Float.toString( SEGMENTATION_MIX_CT_INTO_PMFRF ) ) );
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

        EXPORT_DO_TRACK_EXPORT = props.getProperty( "EXPORT_DO_TRACK_EXPORT", Integer.toString(EXPORT_DO_TRACK_EXPORT?1:0) ).equals("1");
        EXPORT_USER_INPUTS = props.getProperty( "EXPORT_USER_INPUTS", Integer.toString(EXPORT_USER_INPUTS?1:0) ).equals("1");
        EXPORT_INCLUDE_HISTOGRAMS = props.getProperty( "EXPORT_INCLUDE_HISTOGRAMS", Integer.toString(EXPORT_INCLUDE_HISTOGRAMS?1:0) ).equals("1");
        EXPORT_INCLUDE_QUANTILES = props.getProperty( "EXPORT_INCLUDE_QUANTILES", Integer.toString(EXPORT_INCLUDE_QUANTILES?1:0) ).equals("1");
        EXPORT_INCLUDE_COL_INTENSITY_SUMS = props.getProperty( "EXPORT_INCLUDE_COL_INTENSITY_SUMS", Integer.toString(EXPORT_INCLUDE_COL_INTENSITY_SUMS?1:0) ).equals("1");
        EXPORT_INCLUDE_PIXEL_INTENSITIES = props.getProperty( "EXPORT_INCLUDE_PIXEL_INTENSITIES", Integer.toString(EXPORT_INCLUDE_PIXEL_INTENSITIES?1:0) ).equals("1");


        OPTIMISATION_INTERVAL_LENGTH = Integer.parseInt( props.getProperty( "OPTIMISATION_INTERVAL_LENGTH", Integer.toString(OPTIMISATION_INTERVAL_LENGTH) ));

        INTENSITY_FIT_ITERATIONS = Integer.parseInt(props.getProperty("INTENSITY_FIT_ITERATIONS", Integer.toString(INTENSITY_FIT_ITERATIONS)));
        INTENSITY_FIT_PRECISION = Double.parseDouble(props.getProperty("INTENSITY_FIT_PRECISION", Double.toString(INTENSITY_FIT_PRECISION)));
        INTENSITY_FIT_INITIAL_WIDTH = Double.parseDouble(props.getProperty("INTENSITY_FIT_INITIAL_WIDTH", Double.toString(INTENSITY_FIT_INITIAL_WIDTH)));
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

//            props.setProperty( "BGREM_TEMPLATE_XMIN", Integer.toString( BGREM_TEMPLATE_XMIN ) );
//            props.setProperty( "BGREM_TEMPLATE_XMAX", Integer.toString( BGREM_TEMPLATE_XMAX ) );
//            props.setProperty( "BGREM_X_OFFSET", Integer.toString( BGREM_X_OFFSET ) );
            props.setProperty( "GL_WIDTH_IN_PIXELS", Integer.toString( GL_WIDTH_IN_PIXELS ) );
            props.setProperty( "MOTHER_CELL_BOTTOM_TRICK_MAX_PIXELS", Integer.toString( MOTHER_CELL_BOTTOM_TRICK_MAX_PIXELS ) );
            props.setProperty( "INTENSITY_FIT_RANGE_IN_PIXELS", Integer.toString( INTENSITY_FIT_RANGE_IN_PIXELS ) );
            props.setProperty( "GL_OFFSET_TOP", Integer.toString( GL_OFFSET_TOP ) );
            props.setProperty( "GL_OFFSET_LATERAL", Integer.toString( GL_OFFSET_LATERAL ) );
            props.setProperty( "MIN_CELL_LENGTH", Integer.toString( MIN_CELL_LENGTH ) );
            props.setProperty( "MIN_GAP_CONTRAST", Double.toString( MIN_GAP_CONTRAST ) );
            props.setProperty( "SIGMA_PRE_SEGMENTATION_X", Double.toString( SIGMA_PRE_SEGMENTATION_X ) );
            props.setProperty( "SIGMA_PRE_SEGMENTATION_Y", Double.toString( SIGMA_PRE_SEGMENTATION_Y ) );
//            props.setProperty( "SIGMA_GL_DETECTION_X", Double.toString( SIGMA_GL_DETECTION_X ) );
//            props.setProperty( "SIGMA_GL_DETECTION_Y", Double.toString( SIGMA_GL_DETECTION_Y ) );
            props.setProperty( "SEGMENTATION_MIX_CT_INTO_PMFRF", Double.toString( SEGMENTATION_MIX_CT_INTO_PMFRF ) );
            props.setProperty( "SEGMENTATION_MODEL_PATH", SEGMENTATION_MODEL_PATH);
            props.setProperty( "DEFAULT_PATH", DEFAULT_PATH );

            props.setProperty( "GUROBI_TIME_LIMIT", Double.toString( GUROBI_TIME_LIMIT ) );
            props.setProperty( "GUROBI_MAX_OPTIMALITY_GAP", Double.toString( GUROBI_MAX_OPTIMALITY_GAP ) );

//            if ( !MoMA.HEADLESS ) {
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


            props.setProperty( "EXPORT_DO_TRACK_EXPORT", Integer.toString(EXPORT_DO_TRACK_EXPORT?1:0) );
            props.setProperty( "EXPORT_USER_INPUTS", Integer.toString(EXPORT_USER_INPUTS?1:0) );
            props.setProperty( "EXPORT_INCLUDE_HISTOGRAMS", Integer.toString(EXPORT_INCLUDE_HISTOGRAMS?1:0) );
            props.setProperty( "EXPORT_INCLUDE_QUANTILES", Integer.toString(EXPORT_INCLUDE_QUANTILES?1:0) );
            props.setProperty( "EXPORT_INCLUDE_COL_INTENSITY_SUMS", Integer.toString(EXPORT_INCLUDE_COL_INTENSITY_SUMS?1:0) );
            props.setProperty( "EXPORT_INCLUDE_PIXEL_INTENSITIES", Integer.toString(EXPORT_INCLUDE_PIXEL_INTENSITIES?1:0) );

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
