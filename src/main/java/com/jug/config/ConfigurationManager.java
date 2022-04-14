package com.jug.config;

import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.jug.development.featureflags.FeatureFlags.featureFlagDisableMaxCellDrop;


public class ConfigurationManager implements ITrackingConfiguration, IUnetProcessingConfiguration, IComponentTreeGeneratorConfiguration, IConfiguration {
    /**
     * Properties to configure app (loaded and saved to properties file!).
     */
    public static Properties props = null;

    /**
     * The maximum time in seconds GUROBI is allowed to search for a good
     * tracking solution. (After that period of time GUROBI will stop and best
     * solution found so far will be used.)
     */
    public static double GUROBI_TIME_LIMIT = 15.0;
    public static double GUROBI_MAX_OPTIMALITY_GAP = 0.99;

    public static boolean GUI_OPTIMIZE_ON_ILP_CHANGE = true;

    /**
     * The path to usually open JFileChoosers at (except for initial load
     * dialog).
     */
    public static String DEFAULT_PATH = System.getProperty( "user.home" );

    /**
     * The path to save ground truth and time statistics to (yes, we write
     * papers!).
     */
    public static String STATS_OUTPUT_PATH = DEFAULT_PATH;

    /**
     * Controls the total width of the GL image as shown in GUI. The total width is is given by:
     * ConfigurationManager.GL_WIDTH_IN_PIXELS + 2 * ConfigurationManager.GL_PIXEL_PADDING_IN_VIEWS
     * See also its usage in GrowthlaneViewer.java and MoMAGui.java
     */
    public static final int GL_PIXEL_PADDING_IN_VIEWS = 5;

    /**
     * One of the test for paper:
     * What happens if exit constraints are NOT part of the model?
     */
    public static final boolean DISABLE_EXIT_CONSTRAINTS = false;

    /*********************************** CONFIG VALUES DEFINITION START ***********************************************/
    public static final float MAXIMUM_GROWTH_PER_FRAME = 0.2f;
    public static final float MAXIMUM_SHRINKAGE_PER_FRAME = 0.2f;
    public static int DEFAULT_GUI_POS_X = 100;
    /**
     * Parameter: how many pixels wide is the image containing the selected
     * Growthlane?
     */
    public static int GL_WIDTH_IN_PIXELS = 20;
    public static int INTENSITY_FIT_RANGE_IN_PIXELS = 100;
    public static List<String> CELL_LABEL_LIST = new ArrayList<>(Arrays.asList("dead", "dying", "fading"));
    public static String CELL_LABELS = "dead;dying;fading";
    /**
     * This value is critical(!): Assignments with costs higher than this value will be ignored.
     */
    public static float ASSIGNMENT_COST_CUTOFF = Float.MAX_VALUE;
    /**
     * This value sets the fixed cost for lysis assignments. It is set so high, that it will not be considered for
     * assignment during optimization. However, it can be manually forced during curation.
     */
    public static float LYSIS_ASSIGNMENT_COST = 10.0f;
    /**
     * The minimal size in pixel for leaf components. Any possible components smaller than this will not be considered.
     */
    public static int SIZE_MINIMUM_FOR_LEAF_COMPONENTS = 50;
    /**
     * The minimal size in pixel for root components. Any possible components smaller than this will not be considered.
     */
    public static int SIZE_MINIMUM_FOR_ROOT_COMPONENTS = 50;
    /**
     * Vertical center position on which the exit range defined with COMPONENT_EXIT_RANGE is centered.
     */
    public static int GL_OFFSET_TOP = 65;
    /**
     * The number of pixels between adjacent components for which the two components will still be merged in that pixel
     * column.
     */
    public static float THRESHOLD_FOR_COMPONENT_MERGING = 0;
    /**
     * Global threshold value for creating the mask for component generation from the probability mask.
     */
    public static float THRESHOLD_FOR_COMPONENT_GENERATION = 0.5f;
    /**
     * Threshold value above which probability values will be set to one and hence components will not be split.
     */
    public static float THRESHOLD_FOR_COMPONENT_SPLITTING = 1.0f;
    /**
     * Sets the vertical position at the top of the image up to where U-Net will process the image.
     */
    public static int CELL_DETECTION_ROI_OFFSET_TOP = 0;
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
    public static int MAX_CELL_DROP = -1; /* value is set using feature flag featureFlagUseMaxCellDrop */
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
    public static double MAXIMUM_GROWTH_RATE = 1.5; /* Note: This value is used through the interface ITrackingConfiguration. */
    public static boolean GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY = false;
    private static File currentPropertyFile;

    /**
     * Determine if assignment costs should be exported.
     */
    public static boolean EXPORT_ASSIGNMENT_COSTS = false;

    /**
     * One of the test for paper:
     * What happens if exit constraints are NOT part of the model?
     */

    /**
     * Settings related to the measurement and export of the spine length.
     */
    public static boolean EXPORT_SPINE_MEASUREMENT = false; /* set whether to perform the spine length measurement */
    public static int SPINE_MEASUREMENT_ENDPOINT_ORIENTATION_AVERAGING_WINDOWSIZE = 5;
    public static int SPINE_MEASUREMENT_POSITION_AVERAGING_MINIMUM_WINDOWSIZE = 5;
    public static int SPINE_MEASUREMENT_POSITION_AVERAGING_MAXIMUM_WINDOWSIZE = 21;
    public static double SPINE_MEASUREMENT_MEDIALLINE_OFFSET_FROM_CONTOUR_ENDS = 3.5;

    /**
     * Set if the area calculation based on the probability map should be exported.
     */
    public static boolean EXPORT_PROBABILITY_AREA_MEASUREMENT = true; /* set whether to perform the spine length measurement */

    /**
     * Setting related to the measurement and export of the oriented bounding box length measurement.
     */
    public static boolean EXPORT_ORIENTED_BOUNDING_BOX_MEASUREMENT = true; /* set whether to perform the oriented bounding box measurement */

    private int minTime;
    private int maxTime;

    /*********************************** CONFIG VALUES DEFINITION END *************************************************/

    public static void load(File optionalPropertyFile, File userMomaHomePropertyFile, File momaUserDirectory) {
        props = loadParams(optionalPropertyFile, userMomaHomePropertyFile, momaUserDirectory);

        GL_WIDTH_IN_PIXELS = Integer.parseInt(props.getProperty("GL_WIDTH_IN_PIXELS", Integer.toString(GL_WIDTH_IN_PIXELS)));
        INTENSITY_FIT_RANGE_IN_PIXELS = Integer.parseInt(props.getProperty("INTENSITY_FIT_RANGE_IN_PIXELS", Integer.toString(INTENSITY_FIT_RANGE_IN_PIXELS)));
        GL_OFFSET_TOP = Integer.parseInt(props.getProperty("GL_OFFSET_TOP", Integer.toString(GL_OFFSET_TOP)));
        ASSIGNMENT_COST_CUTOFF = Float.parseFloat(props.getProperty("ASSIGNMENT_COST_CUTOFF", Float.toString(ASSIGNMENT_COST_CUTOFF)));
        LYSIS_ASSIGNMENT_COST = Float.parseFloat(props.getProperty("LYSIS_ASSIGNMENT_COST", Float.toString(LYSIS_ASSIGNMENT_COST)));
        SIZE_MINIMUM_FOR_LEAF_COMPONENTS = Integer.parseInt(props.getProperty("SIZE_MINIMUM_FOR_LEAF_COMPONENTS", Integer.toString(SIZE_MINIMUM_FOR_LEAF_COMPONENTS)));
        SIZE_MINIMUM_FOR_ROOT_COMPONENTS = Integer.parseInt(props.getProperty("SIZE_MINIMUM_FOR_ROOT_COMPONENTS", Integer.toString(SIZE_MINIMUM_FOR_ROOT_COMPONENTS)));
        CELL_DETECTION_ROI_OFFSET_TOP = Integer.parseInt(props.getProperty("CELL_DETECTION_ROI_OFFSET_TOP", Integer.toString(CELL_DETECTION_ROI_OFFSET_TOP)));
        THRESHOLD_FOR_COMPONENT_MERGING = Float.parseFloat(props.getProperty("THRESHOLD_FOR_COMPONENT_MERGING", Float.toString(THRESHOLD_FOR_COMPONENT_MERGING)));
        THRESHOLD_FOR_COMPONENT_GENERATION = Float.parseFloat(props.getProperty("THRESHOLD_FOR_COMPONENT_GENERATION", Float.toString(THRESHOLD_FOR_COMPONENT_GENERATION)));
        THRESHOLD_FOR_COMPONENT_SPLITTING = Float.parseFloat(props.getProperty("THRESHOLD_FOR_COMPONENT_SPLITTING", Float.toString(THRESHOLD_FOR_COMPONENT_SPLITTING)));
        MAXIMUM_GROWTH_RATE = Double.parseDouble(props.getProperty("MAXIMUM_GROWTH_RATE", Double.toString(MAXIMUM_GROWTH_RATE)));
        SEGMENTATION_MODEL_PATH = props.getProperty("SEGMENTATION_MODEL_PATH", SEGMENTATION_MODEL_PATH);
        DEFAULT_PATH = props.getProperty("DEFAULT_PATH", DEFAULT_PATH);
        CELL_LABELS = props.getProperty("CELL_LABELS", CELL_LABELS);
        CELL_LABEL_LIST = new ArrayList<>(Arrays.asList(CELL_LABELS.split(";")));

        GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY = parseBooleanFromIntegerValue("GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY", GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY);
        EXPORT_ASSIGNMENT_COSTS = parseBooleanFromIntegerValue("EXPORT_ASSIGNMENT_COSTS", EXPORT_ASSIGNMENT_COSTS);

        GUROBI_TIME_LIMIT = Double.parseDouble(props.getProperty("GUROBI_TIME_LIMIT", Double.toString(GUROBI_TIME_LIMIT)));
        GUROBI_MAX_OPTIMALITY_GAP = Double.parseDouble(props.getProperty("GUROBI_MAX_OPTIMALITY_GAP", Double.toString(GUROBI_MAX_OPTIMALITY_GAP)));

        EXPORT_SPINE_MEASUREMENT = parseBooleanFromIntegerValue("EXPORT_SPINE_MEASUREMENT", EXPORT_SPINE_MEASUREMENT);
        EXPORT_ORIENTED_BOUNDING_BOX_MEASUREMENT = parseBooleanFromIntegerValue("EXPORT_ORIENTED_BOUNDING_BOX_MEASUREMENT", EXPORT_ORIENTED_BOUNDING_BOX_MEASUREMENT);
        SPINE_MEASUREMENT_ENDPOINT_ORIENTATION_AVERAGING_WINDOWSIZE = Integer.parseInt(props.getProperty("SPINE_MEASUREMENT_ENDPOINT_ORIENTATION_AVERAGING_WINDOWSIZE", Integer.toString(SPINE_MEASUREMENT_ENDPOINT_ORIENTATION_AVERAGING_WINDOWSIZE)));
        SPINE_MEASUREMENT_POSITION_AVERAGING_MINIMUM_WINDOWSIZE = Integer.parseInt(props.getProperty("SPINE_MEASUREMENT_POSITION_AVERAGING_MINIMUM_WINDOWSIZE", Integer.toString(SPINE_MEASUREMENT_POSITION_AVERAGING_MINIMUM_WINDOWSIZE)));
        SPINE_MEASUREMENT_POSITION_AVERAGING_MAXIMUM_WINDOWSIZE = Integer.parseInt(props.getProperty("SPINE_MEASUREMENT_POSITION_AVERAGING_MAXIMUM_WINDOWSIZE", Integer.toString(SPINE_MEASUREMENT_POSITION_AVERAGING_MAXIMUM_WINDOWSIZE)));

        EXPORT_PROBABILITY_AREA_MEASUREMENT = parseBooleanFromIntegerValue("EXPORT_PROBABILITY_AREA_MEASUREMENT", EXPORT_PROBABILITY_AREA_MEASUREMENT);

        SPINE_MEASUREMENT_MEDIALLINE_OFFSET_FROM_CONTOUR_ENDS = Double.parseDouble(props.getProperty("SPINE_MEASUREMENT_MEDIALLINE_OFFSET_FROM_CONTOUR_ENDS", Double.toString(SPINE_MEASUREMENT_MEDIALLINE_OFFSET_FROM_CONTOUR_ENDS)));

		/*
		  Default x-position of the main GUI-window. This value will be used if the
		  values in the properties file are not fitting on any of the currently
		  attached screens.
		 */
        GUI_POS_X = Integer.parseInt(props.getProperty("GUI_POS_X", Integer.toString(DEFAULT_GUI_POS_X)));
        GUI_POS_Y = Integer.parseInt(props.getProperty("GUI_POS_Y", Integer.toString(DEFAULT_GUI_POS_X)));
        GUI_WIDTH = Integer.parseInt(props.getProperty("GUI_WIDTH", Integer.toString(GUI_WIDTH)));
        GUI_HEIGHT = Integer.parseInt(props.getProperty("GUI_HEIGHT", Integer.toString(GUI_HEIGHT)));
        GUI_CONSOLE_WIDTH = Integer.parseInt(props.getProperty("GUI_CONSOLE_WIDTH", Integer.toString(GUI_CONSOLE_WIDTH)));
        GUI_NUMBER_OF_SHOWN_TIMESTEPS = Integer.parseInt(props.getProperty("GUI_NUMBER_OF_SHOWN_TIMESTEPS", Integer.toString(GUI_NUMBER_OF_SHOWN_TIMESTEPS)));
        OPTIMISATION_INTERVAL_LENGTH = Integer.parseInt(props.getProperty("OPTIMISATION_INTERVAL_LENGTH", Integer.toString(OPTIMISATION_INTERVAL_LENGTH)));
        INTENSITY_FIT_ITERATIONS = Integer.parseInt(props.getProperty("INTENSITY_FIT_ITERATIONS", Integer.toString(INTENSITY_FIT_ITERATIONS)));
        INTENSITY_FIT_PRECISION = Double.parseDouble(props.getProperty("INTENSITY_FIT_PRECISION", Double.toString(INTENSITY_FIT_PRECISION)));
        INTENSITY_FIT_INITIAL_WIDTH = Double.parseDouble(props.getProperty("INTENSITY_FIT_INITIAL_WIDTH", Double.toString(INTENSITY_FIT_INITIAL_WIDTH)));

        /* process feature flags */
        if (featureFlagDisableMaxCellDrop) {
            ConfigurationManager.MAX_CELL_DROP = Integer.MAX_VALUE; // [px]; not in Props; if vertical distance between two Hyps is larger than this, the corresponding assignment never exists!!! (see e.g. addMappingAssignments)
        } else {
            ConfigurationManager.MAX_CELL_DROP = 50; // [px]; not in Props; if vertical distance between two Hyps is larger than this, the corresponding assignment never exists!!! (see e.g. addMappingAssignments)
        }

    }

    /**
     * Loads the file 'mm.properties' and returns an instance of
     * {@link Properties} containing the key-value pairs found in that file.
     *
     * @return instance of {@link Properties} containing the key-value pairs
     * found in that file.
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

        if (!userMomaHomePropertyFile.exists()) {
            if (!momaUserDirectory.exists()) {
                momaUserDirectory.mkdir();
            }
            // HERE WE NEED TO SETUP THE HOME-DIRECTORY SETTINGS, IF THEY DO NOT EXIST
            final File f = new File("mm.properties");
            if (f.isFile()) {
                try {
                    FileUtils.copyFile(f, userMomaHomePropertyFile);
                } catch (IOException e) {
                    System.out.println("Failed to copy user file to default property file to user moma directory: " + momaUserDirectory.getPath());
                    e.printStackTrace();
                }
            }
            System.out.println("Performed setup of new default property file here: " + userMomaHomePropertyFile.getAbsolutePath());
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
     *
     * @param configFile
     * @return
     * @throws IOException
     */
    private static Properties loadParameters(File configFile) throws IOException {
        FileInputStream is = new FileInputStream(configFile);
        final Properties props = new Properties();
        props.load(is);
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
     */
    public static void saveParams(final File f, JFrame guiFrame) {
        try {
            final OutputStream out = new FileOutputStream(f);

            props.setProperty("GL_WIDTH_IN_PIXELS", Integer.toString(GL_WIDTH_IN_PIXELS));
            props.setProperty("INTENSITY_FIT_RANGE_IN_PIXELS", Integer.toString(INTENSITY_FIT_RANGE_IN_PIXELS));
            props.setProperty("GL_OFFSET_TOP", Integer.toString(GL_OFFSET_TOP));
            props.setProperty("CELL_DETECTION_ROI_OFFSET_TOP", Integer.toString(CELL_DETECTION_ROI_OFFSET_TOP));
            props.setProperty("THRESHOLD_FOR_COMPONENT_MERGING", Float.toString(THRESHOLD_FOR_COMPONENT_MERGING));
            props.setProperty("ASSIGNMENT_COST_CUTOFF", Float.toString(ASSIGNMENT_COST_CUTOFF));
            props.setProperty("LYSIS_ASSIGNMENT_COST", Float.toString(LYSIS_ASSIGNMENT_COST));
            props.setProperty("SIZE_MINIMUM_FOR_LEAF_COMPONENTS", Integer.toString(SIZE_MINIMUM_FOR_LEAF_COMPONENTS));
            props.setProperty("SIZE_MINIMUM_FOR_ROOT_COMPONENTS", Integer.toString(SIZE_MINIMUM_FOR_ROOT_COMPONENTS));
            props.setProperty("MAXIMUM_GROWTH_RATE", Double.toString(MAXIMUM_GROWTH_RATE));
            props.setProperty("THRESHOLD_FOR_COMPONENT_GENERATION", Double.toString(THRESHOLD_FOR_COMPONENT_GENERATION));
            props.setProperty("THRESHOLD_FOR_COMPONENT_SPLITTING", Double.toString(THRESHOLD_FOR_COMPONENT_SPLITTING));
            props.setProperty("SEGMENTATION_MODEL_PATH", SEGMENTATION_MODEL_PATH);
            props.setProperty("DEFAULT_PATH", DEFAULT_PATH);

            props.setProperty("GUROBI_TIME_LIMIT", Double.toString(GUROBI_TIME_LIMIT));
            props.setProperty("GUROBI_MAX_OPTIMALITY_GAP", Double.toString(GUROBI_MAX_OPTIMALITY_GAP));

            if (guiFrame != null) {
                GUI_POS_X = guiFrame.getX();
                GUI_POS_Y = guiFrame.getY();
                GUI_WIDTH = guiFrame.getWidth();
                GUI_HEIGHT = guiFrame.getHeight();
            }

            props.setProperty("GUI_POS_X", Integer.toString(GUI_POS_X));
            props.setProperty("GUI_POS_Y", Integer.toString(GUI_POS_Y));
            props.setProperty("GUI_WIDTH", Integer.toString(GUI_WIDTH));
            props.setProperty("GUI_HEIGHT", Integer.toString(GUI_HEIGHT));
            props.setProperty("GUI_CONSOLE_WIDTH", Integer.toString(GUI_CONSOLE_WIDTH));

            props.setProperty("OPTIMISATION_INTERVAL_LENGTH", Integer.toString(OPTIMISATION_INTERVAL_LENGTH));

            props.setProperty("INTENSITY_FIT_ITERATIONS", Integer.toString(INTENSITY_FIT_ITERATIONS));
            props.setProperty("INTENSITY_FIT_PRECISION", Double.toString(INTENSITY_FIT_PRECISION));
            props.setProperty("INTENSITY_FIT_INITIAL_WIDTH", Double.toString(INTENSITY_FIT_INITIAL_WIDTH));

            setBooleanAsIntegerValue(props, "GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY", GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY);

            setBooleanAsIntegerValue(props, "EXPORT_ASSIGNMENT_COSTS", EXPORT_ASSIGNMENT_COSTS);

            setBooleanAsIntegerValue(props, "EXPORT_SPINE_MEASUREMENT", EXPORT_SPINE_MEASUREMENT);
            setBooleanAsIntegerValue(props, "EXPORT_ORIENTED_BOUNDING_BOX_MEASUREMENT", EXPORT_ORIENTED_BOUNDING_BOX_MEASUREMENT);
            setBooleanAsIntegerValue(props, "EXPORT_PROBABILITY_AREA_MEASUREMENT", EXPORT_PROBABILITY_AREA_MEASUREMENT);

            props.setProperty("SPINE_MEASUREMENT_ENDPOINT_ORIENTATION_AVERAGING_WINDOWSIZE", Integer.toString(SPINE_MEASUREMENT_ENDPOINT_ORIENTATION_AVERAGING_WINDOWSIZE));
            props.setProperty("SPINE_MEASUREMENT_POSITION_AVERAGING_MINIMUM_WINDOWSIZE", Integer.toString(SPINE_MEASUREMENT_POSITION_AVERAGING_MINIMUM_WINDOWSIZE));
            props.setProperty("SPINE_MEASUREMENT_POSITION_AVERAGING_MAXIMUM_WINDOWSIZE", Integer.toString(SPINE_MEASUREMENT_POSITION_AVERAGING_MAXIMUM_WINDOWSIZE));

            props.setProperty("SPINE_MEASUREMENT_MEDIALLINE_OFFSET_FROM_CONTOUR_ENDS", Double.toString(SPINE_MEASUREMENT_MEDIALLINE_OFFSET_FROM_CONTOUR_ENDS));

            props.store(out, "MotherMachine properties");
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean parseBooleanFromIntegerValue(String key, boolean defaultValue) {
        int defaultValueAsInt = defaultValue ? 1 : 0;
        return Integer.parseInt(props.getProperty(key, Integer.toString(defaultValueAsInt))) == 1;
    }

    private static void setBooleanAsIntegerValue(Properties props, String key, boolean value) {
        props.setProperty(key, Integer.toString(value ? 1 : 0));
    }

    /************************ Config interfaces implementation **********************************/

    /**
     * Defines whether to use a limit for the maximum allowed growth rate.
     *
     * @return
     */
    public boolean filterAssignmentsByMaximalGrowthRate() {
        return !GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY; /* If we are using the GT export functionality, we do not want to filter by size difference, because this might remove relevant components. */
    }

    public double getMaximumGrowthRate() {
        return MAXIMUM_GROWTH_RATE;
    }

    public int getCellDetectionRoiOffsetTop() {
        return CELL_DETECTION_ROI_OFFSET_TOP;
    }

    public int getSizeMinimumOfLeafComponent() {
        return SIZE_MINIMUM_FOR_LEAF_COMPONENTS;
    }

    public int getSizeMinimumOfParentComponent() {
        return SIZE_MINIMUM_FOR_ROOT_COMPONENTS;
    }

    public File getCurrentPropertyFile() {
        return currentPropertyFile;
    }

    public String getImagePath(){
        return props.getProperty( "import_path", System.getProperty( "user.home" ) );
    }

    public void setImagePath(String imagePath){
        props.setProperty( "import_path", imagePath);
    }

    /**
     * Set the minimum value of the time-range that will be analyzed.
     * @param minTime
     */
    public void setMinTime(int minTime) {
        this.minTime = minTime;
    }

    @Override
    public int getMinTime() {
        return minTime;
    }

    /**
     * Set the maximum value of the time-range that will be analyzed.
     * @param maxTime
     */
    public void setMaxTime(int maxTime) {
        this.maxTime = maxTime;
    }

    @Override
    public int getMaxTime() {
        return maxTime;
    }

    @Override
    public boolean getRunIlpOnChange() {
        return GUI_OPTIMIZE_ON_ILP_CHANGE;
    }

    @Override
    public void setRunIlpOnChange(boolean runOnChange) {
        GUI_OPTIMIZE_ON_ILP_CHANGE = runOnChange;
    }

    @Override
    public String getPathForAutosaving() {
        return props.getProperty("import_path") + "/--autosave.moma";
    }

    public void setOutputPath(String outputPath) {
        STATS_OUTPUT_PATH = outputPath;
    }

    public String getOutputPath() {
        return STATS_OUTPUT_PATH;
    }

    public String getDefaultPath() {
        return DEFAULT_PATH;
    }

    @Override
    public double getGurobiTimeLimit() {
        return GUROBI_TIME_LIMIT;
    }

    @Override
    public double getGurobiMaxOptimalityGap() {
        return GUROBI_MAX_OPTIMALITY_GAP;
    }
}
