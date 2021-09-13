package com.jug.config;

import com.jug.MoMA;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import static com.jug.MoMA.*;


public class ConfigurationManager {
    public static int DEFAULT_GUI_POS_X = 100;

    public static void load(File optionalPropertyFile, File currentPropertyFile, File userMomaHomePropertyFile, File momaUserDirectory){
        props = loadParams(optionalPropertyFile, currentPropertyFile, userMomaHomePropertyFile, momaUserDirectory);
        BGREM_TEMPLATE_XMIN = Integer.parseInt( props.getProperty( "BGREM_TEMPLATE_XMIN", Integer.toString( BGREM_TEMPLATE_XMIN ) ) );
        BGREM_TEMPLATE_XMAX = Integer.parseInt( props.getProperty( "BGREM_TEMPLATE_XMAX", Integer.toString( BGREM_TEMPLATE_XMAX ) ) );
        BGREM_X_OFFSET = Integer.parseInt( props.getProperty( "BGREM_X_OFFSET", Integer.toString( BGREM_X_OFFSET ) ) );
        GL_WIDTH_IN_PIXELS = Integer.parseInt( props.getProperty( "GL_WIDTH_IN_PIXELS", Integer.toString( GL_WIDTH_IN_PIXELS ) ) );
        MOTHER_CELL_BOTTOM_TRICK_MAX_PIXELS = Integer.parseInt( props.getProperty( "MOTHER_CELL_BOTTOM_TRICK_MAX_PIXELS", Integer.toString( MOTHER_CELL_BOTTOM_TRICK_MAX_PIXELS ) ) );
        INTENSITY_FIT_RANGE_IN_PIXELS = Integer.parseInt( props.getProperty( "INTENSITY_FIT_RANGE_IN_PIXELS", Integer.toString( INTENSITY_FIT_RANGE_IN_PIXELS ) ) );
        GL_OFFSET_TOP = Integer.parseInt( props.getProperty( "GL_OFFSET_TOP", Integer.toString( GL_OFFSET_TOP ) ) );
        GL_OFFSET_LATERAL = Integer.parseInt( props.getProperty( "GL_OFFSET_LATERAL", Integer.toString( GL_OFFSET_LATERAL ) ) );
        MIN_CELL_LENGTH = Integer.parseInt( props.getProperty( "MIN_CELL_LENGTH", Integer.toString( MIN_CELL_LENGTH ) ) );
        MIN_GAP_CONTRAST = Float.parseFloat( props.getProperty( "MIN_GAP_CONTRAST", Float.toString( MIN_GAP_CONTRAST ) ) );
        SIGMA_PRE_SEGMENTATION_X = Float.parseFloat( props.getProperty( "SIGMA_PRE_SEGMENTATION_X", Float.toString( SIGMA_PRE_SEGMENTATION_X ) ) );
        SIGMA_PRE_SEGMENTATION_Y = Float.parseFloat( props.getProperty( "SIGMA_PRE_SEGMENTATION_Y", Float.toString( SIGMA_PRE_SEGMENTATION_Y ) ) );
        SIGMA_GL_DETECTION_X = Float.parseFloat( props.getProperty( "SIGMA_GL_DETECTION_X", Float.toString( SIGMA_GL_DETECTION_X ) ) );
        SIGMA_GL_DETECTION_Y = Float.parseFloat( props.getProperty( "SIGMA_GL_DETECTION_Y", Float.toString( SIGMA_GL_DETECTION_Y ) ) );
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
    private static Properties loadParams(File optionalPropertyFile, File currentPropertyFile, File userMomaHomePropertyFile, File momaUserDirectory) {
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
}
