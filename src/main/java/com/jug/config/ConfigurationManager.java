package com.jug.config;

import com.jug.datahandling.Version;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.jug.development.featureflags.FeatureFlags.featureFlagDisableMaxCellDrop;
import static java.util.Objects.isNull;


public class ConfigurationManager implements ITrackingConfiguration, IUnetProcessingConfiguration, IComponentForestGeneratorConfiguration, IConfiguration {
    /**
     * Properties to configure app (loaded and saved to properties file!).
     */
    public Properties props = null;

    /**
     * The maximum time in seconds GUROBI is allowed to search for a good
     * tracking solution. (After that period of time GUROBI will stop and best
     * solution found so far will be used.)
     */
    public double GUROBI_TIME_LIMIT = 15.0;
    public double GUROBI_TIME_LIMIT_DURING_CURATION = 5.0;
    public boolean GUI_OPTIMIZE_ON_ILP_CHANGE = true;

    /**
     * The path to usually open JFileChoosers at (except for initial load
     * dialog).
     */
    public String DEFAULT_PATH = System.getProperty( "user.home" );

    /**
     * The path to save ground truth and time statistics to (yes, we write
     * papers!).
     */
    public String outputPath = DEFAULT_PATH;

    /**
     * Controls the total width of the GL image as shown in GUI. The total width is is given by:
     * ConfigurationManager.GL_WIDTH_IN_PIXELS + 2 * ConfigurationManager.GL_PIXEL_PADDING_IN_VIEWS
     * See also its usage in GrowthlaneViewer.java and MoMAGui.java
     */
    public final int GL_PIXEL_PADDING_IN_VIEWS = 5;

    /**
     * One of the test for paper:
     * What happens if exit constraints are NOT part of the model?
     */
    public static final boolean DISABLE_EXIT_CONSTRAINTS = false;

    /*********************************** CONFIG VALUES DEFINITION START ***********************************************/
    public float getMaximumGrowthPerFrame(){
        return MAXIMUM_GROWTH_PER_FRAME;
    }
    public final float MAXIMUM_GROWTH_PER_FRAME = 0.2f;
    public float getMaximumShrinkagePerFrame(){
        return MAXIMUM_SHRINKAGE_PER_FRAME;
    }
    public final float MAXIMUM_SHRINKAGE_PER_FRAME = 0.2f;
    public int DEFAULT_GUI_POS_X = 100;
    /**
     * Parameter: how many pixels wide is the image containing the selected
     * Growthlane?
     */
    public int GL_WIDTH_IN_PIXELS = 20;
    public int INTENSITY_FIT_RANGE_IN_PIXELS = 100;
    public List<String> CELL_LABEL_LIST = new ArrayList<>(Arrays.asList("dead", "dying", "fading"));
    public String CELL_LABELS = "dead;dying;fading";
    /**
     * This value is critical(!): Assignments with costs higher than this value will be ignored.
     */

    @Override
    public float getAssignmentCostCutoff() {
        return ASSIGNMENT_COST_CUTOFF;
    }
    public float ASSIGNMENT_COST_CUTOFF = Float.MAX_VALUE;
    /**
     * This value sets the fixed cost for lysis assignments. It is set so high, that it will not be considered for
     * assignment during optimization. However, it can be manually forced during curation.
     */
    @Override
    public float getLysisAssignmentCost() {
        return LYSIS_ASSIGNMENT_COST;
    }
    public float LYSIS_ASSIGNMENT_COST = 10.0f;
    /**
     * The minimal size in pixel for leaf components. Any possible components smaller than this will not be considered.
     */
    public int SIZE_MINIMUM_FOR_LEAF_COMPONENTS = 50;
    /**
     * The minimal size in pixel for root components. Any possible components smaller than this will not be considered.
     */
    public int SIZE_MINIMUM_FOR_ROOT_COMPONENTS = 50;
    /**
     * The maximal width allow for a component in pixels. Components with a width larger than this value will be removed.
     */
    public int MAXIMUM_COMPONENT_WIDTH = 50;
    /**
     * Vertical center position on which the exit range defined with COMPONENT_EXIT_RANGE is centered.
     */
    public int GL_OFFSET_TOP = 65;

    @Override
    public int getGlOffsetTop(){
        return GL_OFFSET_TOP;
    }

    /**
     * The number of pixels between adjacent components for which the two components will still be merged in that pixel
     * column.
     */
    public float THRESHOLD_FOR_COMPONENT_MERGING = 0;
    /**
     * Global threshold value for creating the mask for component generation from the probability mask.
     */
    public float THRESHOLD_FOR_COMPONENT_GENERATION = 0.5f;
    /**
     * Threshold value above which probability values will be set to one and hence components will not be split.
     */
    public float THRESHOLD_FOR_COMPONENT_SPLITTING = 1.0f;
    /**
     * Sets the vertical position at the top of the image up to where U-Net will process the image.
     */
    public int CELL_DETECTION_ROI_OFFSET_TOP = 0;
    /**
     * Range over which the component cost is increased, when exiting the growthlane. This range is centered on
     * at the vertical position defined by GL_OFFSET_TOP.
     */
    public float COMPONENT_EXIT_RANGE = 50;
    @Override
    public double getComponentExitRange(){
        return COMPONENT_EXIT_RANGE;
    }
    /**
     * String pointing at the U-Net TensorFlow model file that should be used for
     * classification during segmentation.
     */
    public String SEGMENTATION_MODEL_PATH = "";
    /**
     *
     */
    public int OPTIMISATION_INTERVAL_LENGTH = -1;
    /**
     * Properties for fitting the Cauchy/Mixture model to the intensity profile.
     */
    public int INTENSITY_FIT_ITERATIONS = 1000; /* Number of iterations performed during fit. */
    public double INTENSITY_FIT_PRECISION = 1e-3; /* Precision for which fitting will be finished. */
    public double INTENSITY_FIT_INITIAL_WIDTH = 5.5; /* Starting width for the fit. */

    public int getMaxCellDrop(){
        return MAX_CELL_DROP;
    }
    public int MAX_CELL_DROP = -1; /* value is set using feature flag featureFlagUseMaxCellDrop */
    /**
     * X-position of the main GUI-window. This value will be loaded from and
     * stored in the properties file!
     */
    public int GUI_POS_X;
    /**
     * Y-position of the main GUI-window. This value will be loaded from and
     * stored in the properties file!
     */
    public int GUI_POS_Y;
    /**
     * Width (in pixels) of the main GUI-window. This value will be loaded from
     * and stored in the properties file!
     */
    public int GUI_WIDTH = 620;
    /**
     * Width (in pixels) of the main GUI-window. This value will be loaded from
     * and stored in the properties file!
     */
    public int GUI_HEIGHT = 740;
    /**
     * Width (in pixels) of the console window. This value will be loaded from
     * and stored in the properties file!
     */
    public int GUI_CONSOLE_WIDTH = 600;
    /**
     * Defines the number of time steps that will be shown side-by-side in the GUI.
     */
    public int GUI_NUMBER_OF_SHOWN_TIMESTEPS = 7;
    /**
     * Shortest time in which we can expect a cell-doubling provided as number of frames.
     */
    public double MAXIMUM_GROWTH_RATE = 1.5; /* Note: This value is used through the interface ITrackingConfiguration. */
    public boolean GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY = false;
    private File currentPropertyFile;

    /**
     * Determine if assignment costs should be exported.
     */
    public boolean EXPORT_ASSIGNMENT_COSTS = false;

    /**
     * One of the test for paper:
     * What happens if exit constraints are NOT part of the model?
     */

    /**
     * Settings related to the measurement and export of the spine length.
     */
    public boolean EXPORT_SPINE_MEASUREMENT = false; /* set whether to perform the spine length measurement */
    public int SPINE_MEASUREMENT_ENDPOINT_ORIENTATION_AVERAGING_WINDOWSIZE = 5;
    public int SPINE_MEASUREMENT_POSITION_AVERAGING_MINIMUM_WINDOWSIZE = 5;
    public int SPINE_MEASUREMENT_POSITION_AVERAGING_MAXIMUM_WINDOWSIZE = 21;
    public double SPINE_MEASUREMENT_MEDIALLINE_OFFSET_FROM_CONTOUR_ENDS = 3.5;

    /**
     * Set if the area calculation based on the probability map should be exported.
     */
    public boolean EXPORT_PROBABILITY_AREA_MEASUREMENT = true; /* set whether to perform the spine length measurement */

    /**
     * Setting related to the measurement and export of the oriented bounding box length measurement.
     */
    public boolean EXPORT_ORIENTED_BOUNDING_BOX_MEASUREMENT = true; /* set whether to perform the oriented bounding box measurement */

    /**
     * Sets if the crossing constraints should be used.
     */
    public boolean USE_FEATURE_CROSSING_CONSTRAINTS = true;

    /**
     * Sets if the crossing constraints should be used.
     */
    public boolean USE_FEATURE_MIGRATION_COSTS = false;

    private int minTime = -1;
    private int maxTime = -1;

    public String datasetMomaVersion = "";

    public void setDatasetMomaVersion(Version version) {
        datasetMomaVersion = version.toString();
    }

    /*********************************** CONFIG VALUES DEFINITION END *************************************************/

    /**
     * Path to Moma setting directory
     */
    private final File momaUserDirectory = new File(System.getProperty("user.home") + "/.moma");

    /**
     * Property file in the moma directory the user.
     */
    private final File userMomaHomePropertyFile = new File(momaUserDirectory.getPath() + "/mm.properties");

    public void load(Path optionalPropertyFile) {
        load(optionalPropertyFile, userMomaHomePropertyFile, momaUserDirectory);
    }

    private void load(Path optionalPropertyFile, File userMomaHomePropertyFile, File momaUserDirectory) {
        props = loadParams(isNull(optionalPropertyFile) ? null : optionalPropertyFile.toFile(), userMomaHomePropertyFile, momaUserDirectory);

        datasetMomaVersion = props.getProperty("GENERATED_BY_MOMA_VERSION", datasetMomaVersion);
        minTime = Integer.parseInt(props.getProperty("TIME_RANGE_START", Integer.toString(minTime)));
        maxTime = Integer.parseInt(props.getProperty("TIME_RANGE_END", Integer.toString(maxTime)));
        GL_WIDTH_IN_PIXELS = Integer.parseInt(props.getProperty("GL_WIDTH_IN_PIXELS", Integer.toString(GL_WIDTH_IN_PIXELS)));
        INTENSITY_FIT_RANGE_IN_PIXELS = Integer.parseInt(props.getProperty("INTENSITY_FIT_RANGE_IN_PIXELS", Integer.toString(INTENSITY_FIT_RANGE_IN_PIXELS)));
        GL_OFFSET_TOP = Integer.parseInt(props.getProperty("GL_OFFSET_TOP", Integer.toString(GL_OFFSET_TOP)));
        ASSIGNMENT_COST_CUTOFF = Float.parseFloat(props.getProperty("ASSIGNMENT_COST_CUTOFF", Float.toString(ASSIGNMENT_COST_CUTOFF)));
        LYSIS_ASSIGNMENT_COST = Float.parseFloat(props.getProperty("LYSIS_ASSIGNMENT_COST", Float.toString(LYSIS_ASSIGNMENT_COST)));
        SIZE_MINIMUM_FOR_LEAF_COMPONENTS = Integer.parseInt(props.getProperty("SIZE_MINIMUM_FOR_LEAF_COMPONENTS", Integer.toString(SIZE_MINIMUM_FOR_LEAF_COMPONENTS)));
        SIZE_MINIMUM_FOR_ROOT_COMPONENTS = Integer.parseInt(props.getProperty("SIZE_MINIMUM_FOR_ROOT_COMPONENTS", Integer.toString(SIZE_MINIMUM_FOR_ROOT_COMPONENTS)));
        MAXIMUM_COMPONENT_WIDTH = Integer.parseInt(props.getProperty("MAXIMUM_COMPONENT_WIDTH", Integer.toString(MAXIMUM_COMPONENT_WIDTH)));
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
        GUROBI_TIME_LIMIT_DURING_CURATION = Double.parseDouble(props.getProperty("GUROBI_TIME_LIMIT_DURING_CURATION", Double.toString(GUROBI_TIME_LIMIT_DURING_CURATION)));

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

        USE_FEATURE_CROSSING_CONSTRAINTS = parseBooleanFromIntegerValue("USE_FEATURE_CROSSING_CONSTRAINTS", USE_FEATURE_CROSSING_CONSTRAINTS);

        USE_FEATURE_MIGRATION_COSTS = parseBooleanFromIntegerValue("USE_FEATURE_MIGRATION_COSTS", USE_FEATURE_MIGRATION_COSTS);

        /* process feature flags */
        if (featureFlagDisableMaxCellDrop) {
            this.MAX_CELL_DROP = Integer.MAX_VALUE; // [px]; not in Props; if vertical distance between two Hyps is larger than this, the corresponding assignment never exists!!! (see e.g. addMappingAssignments)
        } else {
            this.MAX_CELL_DROP = 50; // [px]; not in Props; if vertical distance between two Hyps is larger than this, the corresponding assignment never exists!!! (see e.g. addMappingAssignments)
        }

    }

    /**
     * Loads the file 'mm.properties' and returns an instance of
     * {@link Properties} containing the key-value pairs found in that file.
     *
     * @return instance of {@link Properties} containing the key-value pairs
     * found in that file.
     */
    private Properties loadParams(File optionalPropertyFile, File userMomaHomePropertyFile, File momaUserDirectory) {
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
    private Properties loadParameters(File configFile) throws IOException {
        FileInputStream is = new FileInputStream(configFile);
        final Properties props = new Properties();
        props.load(is);
        return props;
    }

    /**
     * Save parameters to the currently used config file.
     */
    public void saveParams(JFrame guiFrame) {
        saveParams(currentPropertyFile, guiFrame);
    }

    /**
     * Saves a file 'mm.properties' in the current folder. This file contains
     * all MotherMachine specific properties as key-value pairs.
     */
    public void saveParams(final File f, JFrame guiFrame) {
        try {
            final OutputStream out = new FileOutputStream(f);

            props.setProperty("GENERATED_BY_MOMA_VERSION", datasetMomaVersion);
            props.setProperty("TIME_RANGE_START", Integer.toString(minTime));
            props.setProperty("TIME_RANGE_END", Integer.toString(maxTime));
            props.setProperty("GL_WIDTH_IN_PIXELS", Integer.toString(GL_WIDTH_IN_PIXELS));
            props.setProperty("INTENSITY_FIT_RANGE_IN_PIXELS", Integer.toString(INTENSITY_FIT_RANGE_IN_PIXELS));
            props.setProperty("GL_OFFSET_TOP", Integer.toString(GL_OFFSET_TOP));
            props.setProperty("CELL_DETECTION_ROI_OFFSET_TOP", Integer.toString(CELL_DETECTION_ROI_OFFSET_TOP));
            props.setProperty("THRESHOLD_FOR_COMPONENT_MERGING", Float.toString(THRESHOLD_FOR_COMPONENT_MERGING));
            props.setProperty("ASSIGNMENT_COST_CUTOFF", Float.toString(ASSIGNMENT_COST_CUTOFF));
            props.setProperty("LYSIS_ASSIGNMENT_COST", Float.toString(LYSIS_ASSIGNMENT_COST));
            props.setProperty("SIZE_MINIMUM_FOR_LEAF_COMPONENTS", Integer.toString(SIZE_MINIMUM_FOR_LEAF_COMPONENTS));
            props.setProperty("SIZE_MINIMUM_FOR_ROOT_COMPONENTS", Integer.toString(SIZE_MINIMUM_FOR_ROOT_COMPONENTS));
            props.setProperty("MAXIMUM_COMPONENT_WIDTH", Integer.toString(MAXIMUM_COMPONENT_WIDTH));
            props.setProperty("MAXIMUM_GROWTH_RATE", Double.toString(MAXIMUM_GROWTH_RATE));
            props.setProperty("THRESHOLD_FOR_COMPONENT_GENERATION", Double.toString(THRESHOLD_FOR_COMPONENT_GENERATION));
            props.setProperty("THRESHOLD_FOR_COMPONENT_SPLITTING", Double.toString(THRESHOLD_FOR_COMPONENT_SPLITTING));
            props.setProperty("SEGMENTATION_MODEL_PATH", SEGMENTATION_MODEL_PATH);
            props.setProperty("DEFAULT_PATH", DEFAULT_PATH);

            props.setProperty("GUROBI_TIME_LIMIT", Double.toString(GUROBI_TIME_LIMIT));
            props.setProperty("GUROBI_TIME_LIMIT_DURING_CURATION", Double.toString(GUROBI_TIME_LIMIT_DURING_CURATION));

            if (!runningHeadless) { /* only get the guiFrame position and size, if MoMA is running with GUI */
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

    private boolean parseBooleanFromIntegerValue(String key, boolean defaultValue) {
        int defaultValueAsInt = defaultValue ? 1 : 0;
        return Integer.parseInt(props.getProperty(key, Integer.toString(defaultValueAsInt))) == 1;
    }

    private void setBooleanAsIntegerValue(Properties props, String key, boolean value) {
        props.setProperty(key, Integer.toString(value ? 1 : 0));
    }

    /************************ Config interfaces implementation **********************************/

    /**
     * Defines whether to use a limit for the maximum allowed growth rate.
     *
     * @return
     */
    public double getMaximumGrowthRate() {
        return MAXIMUM_GROWTH_RATE;
    }

    @Override
    public int getCellDetectionRoiOffsetTop() {
        return CELL_DETECTION_ROI_OFFSET_TOP;
    }

    @Override
    public Path getModelFilePath() {
        return Paths.get(SEGMENTATION_MODEL_PATH);
    }

    public int getSizeMinimumOfLeafComponent() {
        return SIZE_MINIMUM_FOR_LEAF_COMPONENTS;
    }

    public int getSizeMinimumOfParentComponent() {
        return SIZE_MINIMUM_FOR_ROOT_COMPONENTS;
    }

    public synchronized int getMaximumComponentWidth() {
        return MAXIMUM_COMPONENT_WIDTH;
    }

    public File getCurrentPropertyFile() {
        return currentPropertyFile;
    }

    public String getInputImagePath(){
        return props.getProperty( "IMPORT_PATH", System.getProperty( "user.home" ) );
    }

    public void setImagePath(String imagePath){
        props.setProperty( "IMPORT_PATH", imagePath);
    }

    /**
     * Set minimum value of the time-range that will be analyzed.
     * @param minTime
     */
    public void setMinTime(int minTime) {
        this.minTime = minTime;
    }

    /**
     * Get minimum value of the time-range that will be analyzed.
     */
    @Override
    public int getMinTime() {
        return minTime;
    }

    /**
     * Set maximum value of the time-range that will be analyzed.
     * @param maxTime
     */
    public void setMaxTime(int maxTime) {
        this.maxTime = maxTime;
    }

    /**
     * Get maximum value of the time-range that will be analyzed.
     */
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
        return props.getProperty("IMPORT_PATH") + "/--autosave.moma";
    }

    @Override
    public double getGurobiTimeLimit() {
        return GUROBI_TIME_LIMIT;
    }

    @Override
    public double getGurobiTimeLimitDuringCuration(){
        return GUROBI_TIME_LIMIT_DURING_CURATION;
    }

    boolean runningHeadless;
    public void setIfRunningHeadless(boolean runningHeadless){
        this.runningHeadless = runningHeadless;
    }

    @Override
    public boolean getIfRunningHeadless() {
        return runningHeadless;
    }

    private boolean isReloading = false;
    public boolean getIsReloading(){ return isReloading;}

    public void setIsReloading(boolean isReloading) {
        this.isReloading = isReloading;
    }

    /**
     * Returns the MoMA version that generated the dataset that is being loaded.
     * @return MoMA version that generated dataset
     */
    private String getDatasetMomaVersionString() {
        return datasetMomaVersion;
    }

    public Version getDatasetMomaVersion() {
        return new Version(getDatasetMomaVersionString());
    }

    public boolean getCrossingConstraintFeatureFlag() { return USE_FEATURE_CROSSING_CONSTRAINTS; }

    public boolean getMigrationCostFeatureFlag() { return USE_FEATURE_MIGRATION_COSTS; }

}
