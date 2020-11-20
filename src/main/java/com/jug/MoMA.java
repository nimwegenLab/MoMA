package com.jug;

import com.jug.gui.MoMAGui;
import com.jug.gui.MoMAModel;
import com.jug.util.DataMover;
import com.jug.util.FloatTypeImgLoader;
import com.jug.util.componenttree.UnetProcessor;
import gurobi.GRBEnv;
import gurobi.GRBException;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import net.imagej.patcher.LegacyInjector;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.apache.commons.io.FilenameUtils.removeExtension;

/*
  Main class for the MotherMachine project.
 */

/**
 * @author jug
 */
public class MoMA {


	static {
		LegacyInjector.preinit();
	}

	/**
	 * Identifier of current version
	 */
	public static final String VERSION_STRING = "MoMA_1.0.0";

	// -------------------------------------------------------------------------------------
	// statics
	// -------------------------------------------------------------------------------------
	public static MoMA instance;
	public static boolean HEADLESS = false;
	public static boolean running_as_Fiji_plugin = false;

	/**
	 * Parameter: sigma for gaussian blurring in x-direction of the raw image
	 * data. Used while searching the growth line centers.
	 */
	private static float SIGMA_GL_DETECTION_X = 20f;
	private static float SIGMA_GL_DETECTION_Y = 0f;

	/**
	 * Parameter: sigma for gaussian blurring in x-direction of the raw image
	 * data. Used while searching the gaps between bacteria.
	 */
	private static float SIGMA_PRE_SEGMENTATION_X = 0f;
	private static float SIGMA_PRE_SEGMENTATION_Y = 0f;

	/**
	 * Parameter: how many pixels wide is the image containing the selected
	 * GrowthLine?
	 */
	public static int GL_WIDTH_IN_PIXELS = 20;
	public static int INTENSITY_FIT_RANGE_IN_PIXELS = 100;
	public static final int GL_PIXEL_PADDING_IN_VIEWS = 15;
	public static int MOTHER_CELL_BOTTOM_TRICK_MAX_PIXELS = 10;

	/**
	 * Parameter: later border in pixels - well centers detected too close to
	 * the left and right image border will be neglected. Reason: detection not
	 * reliable if well is truncated.
	 */
	private static int GL_OFFSET_LATERAL = 20;

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
	 * Maximum offset in x direction (with respect to growth line center) to
	 * take the background intensities from that will be subtracted from the
	 * growth line.
	 */
	private static int BGREM_TEMPLATE_XMAX = 35;

	/**
	 * Minimum offset in x direction (with respect to growth line center) to
	 * take the background intensities from that will be subtracted from the
	 * growth line.
	 */
	private static int BGREM_TEMPLATE_XMIN = 20;

	/**
	 * Offsets in +- x direction (with respect to growth line center) where the
	 * measured background values will be subtracted from.
	 */
	private static int BGREM_X_OFFSET = 35;

	/**
	 * Prior knowledge: minimal length of detected cells
	 */
	public static int MIN_CELL_LENGTH = 18;

	/**
	 * Prior knowledge: minimal contrast of a gap (also used for MSERs)
	 */
	private static float MIN_GAP_CONTRAST = 0.02f; // This is set to a very low
													// value that will basically
													// not filter anything...
	/**
	 * When using the learned classification boosted paramaxflow segmentation,
	 * how much of the midline data obtained by the 'simple' linescan +
	 * component tree segmentation should mix in? Rational: if the
	 * classification is flat, the original (simple) mehod might still offer
	 * some modulation!
	 */
	private static float SEGMENTATION_MIX_CT_INTO_PMFRF = 0.25f;

	/**
	 * String pointing at the weka-segmenter model file that should be used for
	 * classification during segmentation.
	 */
	private static String SEGMENTATION_CLASSIFIER_MODEL_FILE = "CellGapClassifier.model";

	/**
	 * String pointing at the weka-segmenter model file that should be used for
	 * classification during cell-stats export for cell-size estimation.
	 */
	private static String CELLSIZE_CLASSIFIER_MODEL_FILE = "CellSizeClassifier.model";

	/**
	 * Global switch that turns the use of the weka classifier for paramaxflow
	 * on or off.
	 * Default: ON (true)
	 */
	public static boolean USE_CLASSIFIER_FOR_PMF = true;

	/**
	 * Global switches for export options
	 */
	public static boolean EXPORT_DO_TRACK_EXPORT = false;
	public static boolean EXPORT_USER_INPUTS = true;
	public static boolean EXPORT_INCLUDE_HISTOGRAMS = false;
	public static boolean EXPORT_INCLUDE_QUANTILES = false;
	public static boolean EXPORT_INCLUDE_COL_INTENSITY_SUMS = true;
	public static boolean EXPORT_INCLUDE_INTENSITY_FIT = true;
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
	 * Set whether to export an image stack with cell masks.
	 */
	public static boolean EXPORT_CELL_MASKS = true;

	/**
	 * One of the test for paper:
	 * What happens if exit constraints are NOT part of the model?
	 */
	public static final boolean DISABLE_EXIT_CONSTRAINTS = false;

	public static final int MAX_CELL_DROP = 50; // [px]; not in Props; if vertical distance between two Hyps is larger than this, the corresponding assignment never exists!!! (see e.g. addMappingAssignments)

	// - - - - - - - - - - - - - -
	// Info about loaded data
	// - - - - - - - - - - - - - -
	private static int minTime = -1;
	private static int maxTime = -1;
	private static int initOptRange = -1;
	private static int minChannelIdx = 1;
	private static int numChannels = 1;


	// - - - - - - - - - - - - - -
	// GUI-WINDOW RELATED STATICS
	// - - - - - - - - - - - - - -
	/**
	 * The <code>JFrame</code> containing the main GUI.
	 */
	private static JFrame guiFrame;

	/**
	 * Properties to configure app (loaded and saved to properties file!).
	 */
	public static Properties props = null;


	/**
	 * X-position of the main GUI-window. This value will be loaded from and
	 * stored in the properties file!
	 */
	private static int GUI_POS_X;

	/**
	 * Y-position of the main GUI-window. This value will be loaded from and
	 * stored in the properties file!
	 */
	private static int GUI_POS_Y;

	/**
	 * Width (in pixels) of the main GUI-window. This value will be loaded from
	 * and stored in the properties file!
	 */
	private static int GUI_WIDTH = 620;

	/**
	 * Width (in pixels) of the main GUI-window. This value will be loaded from
	 * and stored in the properties file!
	 */
	private static int GUI_HEIGHT = 740;

	/**
	 * Width (in pixels) of the console window. This value will be loaded from
	 * and stored in the properties file!
	 */
	private static int GUI_CONSOLE_WIDTH = 600;

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
	 * The maximum time in seconds GUROBI is allowed to search for a good
	 * tracking solution. (After that period of time GUROBI will stop and best
	 * solution found so far will be used.)
	 */
	public static double GUROBI_TIME_LIMIT = 15.0;
	public static double GUROBI_MAX_OPTIMALITY_GAP = 0.99;

	private static MoMAGui gui;

	/**
	 * Path to Moma setting directory
	 */
	private static File momaUserDirectory = new File(System.getProperty("user.home") + "/.moma");

	/**
	 * Property file provided by user through as command-line option.
	 */
	private static File optionalPropertyFile = null;

	/**
	 * Property file in the moma directory the user.
	 */
	final File userMomaHomePropertyFile = new File(momaUserDirectory.getPath() + "/mm.properties");

	/**
	 * Property file that is being used by this instance of Moma.
	 */
	private static File currentPropertyFile = null;

	/**
	 * Stores a string used to decorate filenames e.g. before export.
	 */
	private static String defaultFilenameDecoration;

	/**
	 * Path to the dataset that we are working on.
	 */
	private static String path;


	// ====================================================================================================================

	/**
	 * PROJECT MAIN
	 *
	 * @param args
	 */
	public static void main( final String[] args ) {
		/*
		  Control if ImageJ and loaded data will be shown...
		 */
		boolean showIJ = false;
		if (showIJ) new ImageJ();

//		// ===== set look and feel ========================================================================
//		try {
//			// Set cross-platform Java L&F (also called "Metal")
//			UIManager.setLookAndFeel(
//					UIManager.getCrossPlatformLookAndFeelClassName() );
//		} catch ( final UnsupportedLookAndFeelException e ) {
//			// handle exception
//		} catch ( final ClassNotFoundException e ) {
//			// handle exception
//		} catch ( final InstantiationException e ) {
//			// handle exception
//		} catch ( final IllegalAccessException e ) {
//			// handle exception
//		}

		// ===== command line parsing ======================================================================

		// create Options object & the parser
		final Options options = new Options();
		final CommandLineParser parser = new DefaultParser();
		// defining command line options
		final Option help = new Option( "help", "print this message" );

		final Option headless = new Option( "h", "headless", false, "start without user interface (note: input-folder must be given!)" );
		headless.setRequired( false );

		final Option timeFirst = new Option( "tmin", "min_time", true, "first time-point to be processed" );
		timeFirst.setRequired( false );

		final Option timeLast = new Option( "tmax", "max_time", true, "last time-point to be processed" );
		timeLast.setRequired( false );

		final Option optRange = new Option( "orange", "opt_range", true, "initial optimization range" );
		optRange.setRequired( false );


		final Option infolder = new Option( "i", "infolder", true, "folder to read data from" );
		infolder.setRequired( false );

		final Option outfolder = new Option( "o", "outfolder", true, "folder to write preprocessed data to (equals infolder if not given)" );
		outfolder.setRequired( false );

		final Option userProps = new Option( "p", "props", true, "properties file to be loaded (mm.properties)" );
		userProps.setRequired( false );

		options.addOption( help );
		options.addOption( headless );
		options.addOption( timeFirst );
		options.addOption( timeLast );
		options.addOption( optRange );
		options.addOption( infolder );
		options.addOption( outfolder );
		options.addOption( userProps );
		// get the commands parsed
		CommandLine cmd = null;
		try {
			cmd = parser.parse( options, args );
		} catch ( final ParseException e1 ) {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(
					"... [-p props-file] -i in-folder [-o out-folder] -c <num-channels> [-cmin start-channel-ids] [-tmin idx] [-tmax idx] [-orange num-frames] [-headless]",
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

		File inputFolder = null;
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


		if (inputFolder.isDirectory() && inputFolder.listFiles(FloatTypeImgLoader.tifFilter).length > 1) {
			System.out.println("reading a folder of images");
			int min_t = Integer.MAX_VALUE;
			int max_t = Integer.MIN_VALUE;
			int min_c = Integer.MAX_VALUE;
			int max_c = Integer.MIN_VALUE;
			for (final File image : inputFolder.listFiles(FloatTypeImgLoader.tifFilter)) {

				final int c = FloatTypeImgLoader.getChannelFromFilename(image.getName());
				final int t = FloatTypeImgLoader.getTimeFromFilename(image.getName());

				if (c < min_c) {
					min_c = c;
				}
				if (c > max_c) {
					max_c = c;
				}

				if (t < min_t) {
					min_t = t;
				}
				if (t > max_t) {
					max_t = t;
				}
			}
			minTime = min_t;
			maxTime = max_t + 1;
			minChannelIdx = min_c;
			numChannels = max_c - min_c + 1;
		} else {

			ImagePlus imp;
			if (inputFolder.isDirectory() && inputFolder.listFiles(FloatTypeImgLoader.tifFilter).length == 1) {
				System.out.println("reading a folder with a single image");
				imp = IJ.openImage(inputFolder.listFiles(FloatTypeImgLoader.tifFilter)[0].getAbsolutePath());
			} else {
				System.out.println("reading a file");
				imp = IJ.openImage(inputFolder.getAbsolutePath());
			}

			minTime = 1;
			maxTime = imp.getNFrames();
			minChannelIdx = 1;
			numChannels = imp.getNChannels();
		}
		System.out.println("Determined minTime" + minTime);
		System.out.println("Determined maxTime" + maxTime);

		System.out.println("Determined minChannelIdx" + minChannelIdx);
		System.out.println("Determined numChannels" + numChannels);


		if ( cmd.hasOption( "tmin" ) ) {
			minTime = Integer.parseInt( cmd.getOptionValue( "tmin" ) );
		}
		if ( cmd.hasOption( "tmax" ) ) {
			maxTime = Integer.parseInt( cmd.getOptionValue( "tmax" ) );
		}

		if ( cmd.hasOption( "orange" ) ) {
			initOptRange = Integer.parseInt( cmd.getOptionValue( "orange" ) );
		}

		// ******** CHECK GUROBI ********* CHECK GUROBI ********* CHECK GUROBI *********
		final String jlp = System.getProperty( "java.library.path" );
		try {
			new GRBEnv( "MoMA_gurobi.log" );
		} catch ( final GRBException e ) {
			final String msgs = "Initial Gurobi test threw exception... check your Gruobi setup!\n\nJava library path: " + jlp;
			if ( HEADLESS ) {
				System.out.println( msgs );
			} else {
				JOptionPane.showMessageDialog(
						MoMA.guiFrame,
						msgs,
						"Gurobi Error?",
						JOptionPane.ERROR_MESSAGE );
			}
			e.printStackTrace();
			if (!running_as_Fiji_plugin) {
				System.exit( 98 );
			} else {
				return;
			}
		} catch ( final UnsatisfiedLinkError ulr ) {
			final String msgs = "Could not initialize Gurobi.\n" + "You might not have installed Gurobi properly or you miss a valid license.\n" + "Please visit 'www.gurobi.com' for further information.\n\n" + ulr.getMessage() + "\nJava library path: " + jlp;
			if ( HEADLESS ) {
				System.out.println( msgs );
			} else {
				JOptionPane.showMessageDialog(
						MoMA.guiFrame,
						msgs,
						"Gurobi Error?",
						JOptionPane.ERROR_MESSAGE );
				ulr.printStackTrace();
			}
			System.out.println( "\n>>>>> Java library path: " + jlp + "\n" );
			if (!running_as_Fiji_plugin) {
				System.exit( 99 );
			} else {
				return;
			}
		}
		// ******* END CHECK GUROBI **** END CHECK GUROBI **** END CHECK GUROBI ********

		final MoMA main = new MoMA();
		if ( !HEADLESS ) {
			guiFrame = new JFrame();
			main.initMainWindow( guiFrame );
		}

		System.out.println( "VERSION: " + VERSION_STRING );

		props = main.loadParams();
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
		SEGMENTATION_CLASSIFIER_MODEL_FILE = props.getProperty( "SEGMENTATION_CLASSIFIER_MODEL_FILE", SEGMENTATION_CLASSIFIER_MODEL_FILE );
		CELLSIZE_CLASSIFIER_MODEL_FILE = props.getProperty( "CELLSIZE_CLASSIFIER_MODEL_FILE", CELLSIZE_CLASSIFIER_MODEL_FILE );
		DEFAULT_PATH = props.getProperty( "DEFAULT_PATH", DEFAULT_PATH );

		GUROBI_TIME_LIMIT = Double.parseDouble( props.getProperty( "GUROBI_TIME_LIMIT", Double.toString( GUROBI_TIME_LIMIT ) ) );
		GUROBI_MAX_OPTIMALITY_GAP = Double.parseDouble( props.getProperty( "GUROBI_MAX_OPTIMALITY_GAP", Double.toString( GUROBI_MAX_OPTIMALITY_GAP ) ) );

		/*
		  Default x-position of the main GUI-window. This value will be used if the
		  values in the properties file are not fitting on any of the currently
		  attached screens.
		 */
		int DEFAULT_GUI_POS_X = 100;
		GUI_POS_X = Integer.parseInt( props.getProperty( "GUI_POS_X", Integer.toString(DEFAULT_GUI_POS_X) ) );
		GUI_POS_Y = Integer.parseInt( props.getProperty( "GUI_POS_Y", Integer.toString(DEFAULT_GUI_POS_X) ) );
		GUI_WIDTH = Integer.parseInt( props.getProperty( "GUI_WIDTH", Integer.toString( GUI_WIDTH ) ) );
		GUI_HEIGHT = Integer.parseInt( props.getProperty( "GUI_HEIGHT", Integer.toString( GUI_HEIGHT ) ) );
		GUI_CONSOLE_WIDTH = Integer.parseInt( props.getProperty( "GUI_CONSOLE_WIDTH", Integer.toString( GUI_CONSOLE_WIDTH ) ) );

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

		EXPORT_CELL_MASKS = props.getProperty( "EXPORT_CELL_MASKS", Integer.toString(EXPORT_CELL_MASKS?1:0) ).equals("1");

		if ( !HEADLESS ) {
			// Iterate over all currently attached monitors and check if sceen
			// position is actually possible,
			// otherwise fall back to the DEFAULT values and ignore the ones
			// coming from the properties-file.
			boolean pos_ok = false;
			final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			final GraphicsDevice[] gs = ge.getScreenDevices();
			for (GraphicsDevice g : gs) {
				if (g.getDefaultConfiguration().getBounds().contains(new java.awt.Point(GUI_POS_X, GUI_POS_Y))) {
					pos_ok = true;
				}
			}
			// None of the screens contained the top-left window coordinates -->
			// fall back onto default values...
			if ( !pos_ok ) {
				GUI_POS_X = DEFAULT_GUI_POS_X;
				/*
				  Default y-position of the main GUI-window. This value will be used if the
				  values in the properties file are not fitting on any of the currently
				  attached screens.
				 */
				int DEFAULT_GUI_POS_Y = 100;
				GUI_POS_Y = DEFAULT_GUI_POS_Y;
			}
		}

		path = props.getProperty( "import_path", System.getProperty( "user.home" ) );
		if ( inputFolder == null || inputFolder.equals( "" ) ) {
			inputFolder = main.showStartupDialog( guiFrame, path );
		}
		System.out.println( "Default filename decoration = " + inputFolder.getName() );
		setDefaultFilenameDecoration(inputFolder.getName());
		path = inputFolder.getAbsolutePath();
		props.setProperty( "import_path", path );


		//GrowthLineSegmentationMagic.setClassifier( tempfilename , "" );



		if ( !HEADLESS ) {
			// Setting up console window...
			main.initConsoleWindow();
			main.showConsoleWindow( true );
		}

		// ------------------------------------------------------------------------------------------------------
		// ------------------------------------------------------------------------------------------------------
		final MoMAModel mmm = new MoMAModel( main );
		instance = main;
		try {
			main.processDataFromFolder( path, minTime, maxTime, minChannelIdx, numChannels );
		} catch ( final Exception e ) {
			e.printStackTrace();
			if (!running_as_Fiji_plugin) {
				System.exit( 11 );
			} else {
				return;
			}
		}
		// ------------------------------------------------------------------------------------------------------
		// ------------------------------------------------------------------------------------------------------

		// show loaded and annotated data
		if (showIJ) {
			new ImageJ();
			ImageJFunctions.show( main.imgRaw, "Rotated & cropped raw data" );
			// ImageJFunctions.show( main.imgTemp, "Temporary" );
			// ImageJFunctions.show( main.imgAnnotated, "Annotated ARGB data" );

			// main.getCellSegmentedChannelImgs()
			// ImageJFunctions.show( main.imgClassified, "Classification" );
			// ImageJFunctions.show( main.getCellSegmentedChannelImgs(), "Segmentation" );
		}

		gui = new MoMAGui( mmm );

		if ( !HEADLESS ) {
			System.out.print( "Build GUI..." );
			main.showConsoleWindow( false );

//			final JFrameSnapper snapper = new JFrameSnapper();
//			snapper.addFrame( main.frameConsoleWindow );
//			snapper.addFrame( guiFrame );

			gui.setVisible( true );
			guiFrame.add( gui );
			guiFrame.setSize( GUI_WIDTH, GUI_HEIGHT );
			guiFrame.setLocation( GUI_POS_X, GUI_POS_Y );
			guiFrame.setVisible( true );

//			SwingUtilities.invokeLater( new Runnable() {
//
//				@Override
//				public void run() {
//					snapper.snapFrames( main.frameConsoleWindow, guiFrame, JFrameSnapper.EAST );
//				}
//			} );
			System.out.println( " done!" );
		} else {
//			final String name = inputFolder.getName();

			gui.exportHtmlOverview();
			gui.exportDataFiles();

			instance.saveParams();

			if (!running_as_Fiji_plugin) {
				System.exit( 11 );
			}
		}
	}

	// -------------------------------------------------------------------------------------
	// fields
	// -------------------------------------------------------------------------------------

	/**
	 * The singleton instance of ImageJ.
	 */

	private List< Img< FloatType >> rawChannelImgs;
	private Img< FloatType > imgRaw;
	private Img< FloatType > imgTemp;
	private Img< FloatType > imgProbs;
	private Img< ARGBType > imgAnnotated;

	/**
	 * Contains all GrowthLines found in the given data.
	 */
	private List< GrowthLine > growthLines;

	/**
	 * Frame hosting the console output.
	 */
	private JFrame frameConsoleWindow;

	/**
	 * TextArea hosting the console output within the JFrame frameConsoleWindow.
	 */
	private JTextArea consoleWindowTextArea;

	/**
	 * String denoting the name of the loaded dataset (e.g. used in GUI)
	 */
	private String datasetName;

	// -------------------------------------------------------------------------------------
	// setters and getters
	// -------------------------------------------------------------------------------------
	/**
	 * @return the imgRaw
	 */
	public Img< FloatType > getImgRaw() {
		return imgRaw;
	}

	/**
	 * @return the rawChannelImgs
	 */
	public List< Img< FloatType >> getRawChannelImgs() {
		return rawChannelImgs;
	}

	/**
	 * @param imgRaw
	 *            the imgRaw to set
	 */
	public void setImgRaw( final Img< FloatType > imgRaw ) {
		this.imgRaw = imgRaw;
	}

	/**
	 * @return the imgTemp
	 */
	public Img< FloatType > getImgTemp() {
		return imgTemp;
	}

	/**
	 * @return the imgProbs
	 */
	public Img< FloatType > getImgProbs() {
		return imgProbs;
	}

	/**
	 * @param imgTemp
	 *            the imgTemp to set
	 */
	private void setImgTemp(final Img<FloatType> imgTemp) {
		this.imgTemp = imgTemp;
	}

	/**
	 * @return the imgRendered
	 */
	private Img< ARGBType > getImgAnnotated() {
		return imgAnnotated;
	}

	/**
	 * @param imgRendered
	 *            the imgRendered to set
	 */
	public void setImgRendered( final Img< ARGBType > imgRendered ) {
		this.imgAnnotated = imgRendered;
	}

	/**
	 * @return the growthLines
	 */
	public List< GrowthLine > getGrowthLines() {
		return growthLines;
	}

	/**
	 * @param growthLines
	 *            the growthLines to set
	 */
	private void setGrowthLines(final List<GrowthLine> growthLines) {
		this.growthLines = growthLines;
	}

	// -------------------------------------------------------------------------------------
	// methods
	// -------------------------------------------------------------------------------------

	/**
	 * Created and shows the console window and redirects System.out and
	 * System.err to it.
	 */
	private void initConsoleWindow() {
		frameConsoleWindow = new JFrame( String.format( "%s Console Window", MoMA.VERSION_STRING ) );
		// frameConsoleWindow.setResizable( false );
		consoleWindowTextArea = new JTextArea();
		consoleWindowTextArea.setLineWrap( true );
		consoleWindowTextArea.setWrapStyleWord( true );

		final int centerX = ( int ) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2;
		final int centerY = ( int ) Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2;
		frameConsoleWindow.setBounds( centerX - GUI_CONSOLE_WIDTH / 2, centerY - GUI_HEIGHT / 2, GUI_CONSOLE_WIDTH, GUI_HEIGHT );
		final JScrollPane scrollPane = new JScrollPane( consoleWindowTextArea );
//		scrollPane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPane.setBorder( BorderFactory.createEmptyBorder( 0, 15, 0, 0 ) );
		frameConsoleWindow.getContentPane().add( scrollPane );

		final OutputStream out = new OutputStream() {

			private final PrintStream original = new PrintStream( System.out );

			@Override
			public void write( final int b ) {
				updateConsoleTextArea( String.valueOf( ( char ) b ) );
				original.print((char) b);
			}

			@Override
			public void write( final byte[] b, final int off, final int len ) {
				updateConsoleTextArea( new String( b, off, len ) );
				original.print( new String( b, off, len ) );
			}

			@Override
			public void write( final byte[] b ) {
				write( b, 0, b.length );
			}
		};

		final OutputStream err = new OutputStream() {

			private final PrintStream original = new PrintStream( System.out );

			@Override
			public void write( final int b ) {
				updateConsoleTextArea( String.valueOf( ( char ) b ) );
				original.print((char) b);
			}

			@Override
			public void write( final byte[] b, final int off, final int len ) {
				updateConsoleTextArea( new String( b, off, len ) );
				original.print( new String( b, off, len ) );
			}

			@Override
			public void write( final byte[] b ) {
				write( b, 0, b.length );
			}
		};

		System.setOut( new PrintStream( out, true ) );
		System.setErr( new PrintStream( err, true ) );
	}

	private void updateConsoleTextArea( final String text ) {
		SwingUtilities.invokeLater(() -> consoleWindowTextArea.append( text ));
	}

	/**
	 * Shows the ConsoleWindow
	 */
	public void showConsoleWindow( final boolean show ) {
		frameConsoleWindow.setVisible( show );
	}

	/**
	 * @return
	 */
	public boolean isConsoleVisible() {
		return this.frameConsoleWindow.isVisible();
	}

	/**
	 * Initializes the MotherMachine main app. This method contains platform
	 * specific code like setting icons, etc.
	 *
	 * @param guiFrame
	 *            the JFrame containing the MotherMachine.
	 */
	private void initMainWindow( final JFrame guiFrame ) {
		setDatasetName( datasetName );

		guiFrame.addWindowListener( new WindowAdapter() {

			@Override
			public void windowClosing( final WindowEvent we ) {
				saveParams();
				if (!running_as_Fiji_plugin) {
					System.exit(0);
				}
			}
		} );

	}

	/**
	 *
	 * @param guiFrame
	 *            parent frame
	 * @param datapath
	 *            path to be suggested to open
	 * @return
	 */
	private File showStartupDialog( final JFrame guiFrame, final String datapath ) {

		File file;
		final String parentFolder = datapath.substring( 0, datapath.lastIndexOf( File.separatorChar ) );

		// DATA TO BE LOADED --- DATA TO BE LOADED --- DATA TO BE LOADED --- DATA TO BE LOADED

		int decision;
		if ( datapath.equals( System.getProperty( "user.home" ) ) ) {
			decision = JOptionPane.NO_OPTION;
		} else {
			final String message = "Should the MotherMachine be opened with the data found in:\n" + datapath + "\n\nIn case you want to choose a folder please select 'No'...";
			final String title = "MotherMachine Data Folder Selection";
			decision = JOptionPane.showConfirmDialog( guiFrame, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
		}
		if ( decision == JOptionPane.YES_OPTION ) {
			file = new File( datapath );
		} else {
			file = showFolderChooser( guiFrame, parentFolder );
		}
		return file;
	}

	/**
	 * Shows a JFileChooser set up to accept the selection of folders. If
	 * 'cancel' is pressed this method terminates the MotherMachine app.
	 *
	 * @param guiFrame
	 *            parent frame
	 * @param path
	 *            path to the folder to open initially
	 * @return an instance of {@link File} pointing at the selected folder.
	 */
	private File showFolderChooser( final JFrame guiFrame, final String path ) {
		File selectedFile;

		if ( SystemUtils.IS_OS_MAC ) {
			// --- ON MAC SYSTEMS --- ON MAC SYSTEMS --- ON MAC SYSTEMS --- ON MAC SYSTEMS --- ON MAC SYSTEMS ---
			System.setProperty( "apple.awt.fileDialogForDirectories", "true" );
			final FileDialog fd = new FileDialog( guiFrame, "Select folder containing image sequence...", FileDialog.LOAD );
			fd.setDirectory( path );
			fd.setVisible( true );
			selectedFile = new File( fd.getDirectory() + "/" + fd.getFile() );
			if ( fd.getFile() == null ) {
				System.exit( 0 );
				return null;
			}
			System.setProperty( "apple.awt.fileDialogForDirectories", "false" );
		} else {
			// --- NOT ON A MAC --- NOT ON A MAC --- NOT ON A MAC --- NOT ON A MAC --- NOT ON A MAC --- NOT ON A MAC ---
			final JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory( new java.io.File( path ) );
			chooser.setDialogTitle( "Select folder containing image sequence..." );
			chooser.setFileFilter( new FileFilter() {

				@Override
				public final boolean accept( final File file ) {
					return file.isDirectory();
				}

				@Override
				public String getDescription() {
					return "We only take directories";
				}
			} );
			chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
			chooser.setAcceptAllFileFilterUsed( false );

			if ( chooser.showOpenDialog( guiFrame ) == JFileChooser.APPROVE_OPTION ) {
				selectedFile = chooser.getSelectedFile();
			} else {
				System.exit( 0 );
				return null;
			}
		}

		return selectedFile;
	}

	/**
	 * Loads the file 'mm.properties' and returns an instance of
	 * {@link Properties} containing the key-value pairs found in that file.
	 *
	 * @return instance of {@link Properties} containing the key-value pairs
	 *         found in that file.
	 */
	private Properties loadParams() {
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
	private Properties loadParameters(File configFile) throws IOException {
		FileInputStream is = new FileInputStream(configFile);
		final Properties props = new Properties();
		props.load( is );
		return props;
	}

	/**
	 * Save parameters to the currently used config file.
	 */
	private void saveParams() {
		saveParams(currentPropertyFile);
	}

	/**
	 * Saves a file 'mm.properties' in the current folder. This file contains
	 * all MotherMachine specific properties as key-value pairs.
	 *
	 */
	public void saveParams(final File f) {
		try {
			final OutputStream out = new FileOutputStream( f );

			props.setProperty( "BGREM_TEMPLATE_XMIN", Integer.toString( BGREM_TEMPLATE_XMIN ) );
			props.setProperty( "BGREM_TEMPLATE_XMAX", Integer.toString( BGREM_TEMPLATE_XMAX ) );
			props.setProperty( "BGREM_X_OFFSET", Integer.toString( BGREM_X_OFFSET ) );
			props.setProperty( "GL_WIDTH_IN_PIXELS", Integer.toString( GL_WIDTH_IN_PIXELS ) );
			props.setProperty( "MOTHER_CELL_BOTTOM_TRICK_MAX_PIXELS", Integer.toString( MOTHER_CELL_BOTTOM_TRICK_MAX_PIXELS ) );
			props.setProperty( "INTENSITY_FIT_RANGE_IN_PIXELS", Integer.toString( INTENSITY_FIT_RANGE_IN_PIXELS ) );
			props.setProperty( "GL_OFFSET_TOP", Integer.toString( GL_OFFSET_TOP ) );
			props.setProperty( "GL_OFFSET_LATERAL", Integer.toString( GL_OFFSET_LATERAL ) );
			props.setProperty( "MIN_CELL_LENGTH", Integer.toString( MIN_CELL_LENGTH ) );
			props.setProperty( "MIN_GAP_CONTRAST", Double.toString( MIN_GAP_CONTRAST ) );
			props.setProperty( "SIGMA_PRE_SEGMENTATION_X", Double.toString( SIGMA_PRE_SEGMENTATION_X ) );
			props.setProperty( "SIGMA_PRE_SEGMENTATION_Y", Double.toString( SIGMA_PRE_SEGMENTATION_Y ) );
			props.setProperty( "SIGMA_GL_DETECTION_X", Double.toString( SIGMA_GL_DETECTION_X ) );
			props.setProperty( "SIGMA_GL_DETECTION_Y", Double.toString( SIGMA_GL_DETECTION_Y ) );
			props.setProperty( "SEGMENTATION_MIX_CT_INTO_PMFRF", Double.toString( SEGMENTATION_MIX_CT_INTO_PMFRF ) );
			props.setProperty( "SEGMENTATION_CLASSIFIER_MODEL_FILE", SEGMENTATION_CLASSIFIER_MODEL_FILE );
			props.setProperty( "CELLSIZE_CLASSIFIER_MODEL_FILE", CELLSIZE_CLASSIFIER_MODEL_FILE );
			props.setProperty( "DEFAULT_PATH", DEFAULT_PATH );

			props.setProperty( "GUROBI_TIME_LIMIT", Double.toString( GUROBI_TIME_LIMIT ) );
			props.setProperty( "GUROBI_MAX_OPTIMALITY_GAP", Double.toString( GUROBI_MAX_OPTIMALITY_GAP ) );

			if ( !MoMA.HEADLESS ) {
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

			props.setProperty("EXPORT_CELL_MASKS", Integer.toString(EXPORT_CELL_MASKS?1:0));

			props.store( out, "MotherMachine properties" );
		} catch ( final Exception e ) {
			e.printStackTrace();
		}
	}

	/**
	 * Opens all tiffs in the given folder, straightens and crops images,
	 * extracts growth lines, subtracts background, builds segmentation
	 * hypothesis and a Markov random field for tracking. Finally it even solves
	 * this model using Gurobi and reads out the MAP.
	 *
	 * Note: multi-channel assumption is that filename encodes channel by
	 * containing a substring of format "_c%02d".
	 *
	 * @param path
	 *            the folder to be processed.
	 * @param minTime
	 * @param maxTime
	 * @param minChannelIdx
	 * @param numChannels
	 * @throws Exception
	 */
	private void processDataFromFolder( final String path, final int minTime, final int maxTime, final int minChannelIdx, final int numChannels ) throws Exception {

		if ( numChannels == 0 ) { throw new Exception( "At least one color channel must be loaded!" ); }

		// extract dataset name and set in GUI
		final File folder = new File( path );
		setDatasetName( String.format( "%s >> %s", folder.getParentFile().getName(), folder.getName() ) );

		// load channels separately into Img objects
		rawChannelImgs = FloatTypeImgLoader.loadTiffsFromFileOrFolder(path, minTime, maxTime, minChannelIdx, numChannels + minChannelIdx - 1);

//		Context context = new Context();
//		ops = context.service(OpService.class);

		imgRaw = rawChannelImgs.get( 0 );
//		Pair<FloatType, FloatType> result1 = ops.stats().minMax(Views.hyperSlice(imgRaw, 2, 1));

		// setup ARGB image (that will eventually contain annotations)
		System.out.print( "Spawning off annotation image (ARGB)..." );
		resetImgAnnotatedLike( getImgRaw() );
		try {
			DataMover.convertAndCopy( getImgRaw(), getImgAnnotated() );
		} catch ( final Exception e ) {
			// conversion might not be supported
			e.printStackTrace();
		}
		System.out.println( " done!" );

		restartFromGLSegmentation();

		if ( HEADLESS ) {
			System.out.println( "Generating Integer Linear Program(s)..." );
			generateILPs();
			System.out.println( " done!" );

			System.out.println( "Running Integer Linear Program(s)..." );
			runILPs();
			System.out.println( " done!" );
		}
	}

	/**
	 * Resets imgTemp to contain the raw data from imgRaw.
	 */
	private void resetImgTempToRaw() {
		setImgTemp( imgRaw.copy() );
	}

	/**
	 * Resets imgTemp to contain the raw data from imgRaw.
	 */
	private void resetImgAnnotatedLike(final Img<FloatType> img) {
		imgAnnotated = DataMover.createEmptyArrayImgLike( img, new ARGBType() );
	}

	/**
	 * Adds all intensity values of row i in view to rowSums[i].
	 *
	 * @param view
	 * @param rowSums
	 */
	private float[] addRowSumsFromInterval( final IntervalView< FloatType > view, final float[] rowSums ) {
		for ( int i = ( int ) view.min( 1 ); i <= view.max( 1 ); i++ ) {
			final IntervalView< FloatType > row = Views.hyperSlice( view, 1, i );
			final Cursor< FloatType > cursor = Views.iterable( row ).cursor();
			while ( cursor.hasNext() ) {
				rowSums[ i - ( int ) view.min( 1 ) ] += cursor.next().get();
			}
		}
		return rowSums;
	}

	/**
	 * Removes the value values[i] from all columns in row i of the given view.
	 *
	 * @param view
	 * @param values
	 */
	private void removeValuesFromRows( final IntervalView< FloatType > view, final float[] values ) {
		for ( int i = ( int ) view.min( 1 ); i <= view.max( 1 ); i++ ) {
			final Cursor< FloatType > cursor = Views.iterable( Views.hyperSlice( view, 1, i ) ).cursor();
			while ( cursor.hasNext() ) {
				cursor.next().set( new FloatType( Math.max( 0, cursor.get().get() - values[ i - ( int ) view.min( 1 ) ] ) ) );
			}
		}
	}

	/**
     * NOTE: This method is kept to be compatible with down-stream code.
	 * Write the centers of the growth line given in 'imgTemp'. Since it is centered
     * in the image, we set the coordinates to the center of the image. Note, that
     * this method is a legacy artifact. Legacy-Moma was able to treat full-frames with
     * multiple GL inside an image by detecting them. This now no longer necessary after
     * doing the preprocessing, so that we can simplify this method, the way we did.
	 */
    private void findGrowthLines() {
        this.setGrowthLines(new ArrayList<>() );
        getGrowthLines().add( new GrowthLine() );

        for ( long frameIdx = 0; frameIdx < imgTemp.dimension( 2 ); frameIdx++ ) {
            GrowthLineFrame currentFrame = new GrowthLineFrame((int) frameIdx);
            final IntervalView< FloatType > ivFrame = Views.hyperSlice( imgTemp, 2, frameIdx );
            currentFrame.setImage(ImgView.wrap(ivFrame, new ArrayImgFactory(new FloatType())));
            getGrowthLines().get(0).add(currentFrame);
        }
    }


	/**
	 * Iterates over all found GrowthLines and evokes
	 * GrowthLine.findGapHypotheses(Img). Note that this function always uses
	 * the image data in 'imgTemp'.
	 */
	private void generateAllSimpleSegmentationHypotheses() {
		imgProbs = processImageOrLoadFromDisk();
		for ( final GrowthLine gl : getGrowthLines() ) {
			gl.getFrames().parallelStream().forEach((glf) -> {
				System.out.print( "." );
				glf.generateSimpleSegmentationHypotheses( imgProbs, glf.getFrameIndex() );
			});
			System.out.println( " ...done!" );
		}
	}

	private Img<FloatType> processImageOrLoadFromDisk() {
		UnetProcessor unetProcessor = new UnetProcessor();

		String checksum = unetProcessor.getModelChecksum();

		/**
		 *  generate probability filename
		 */
		File file = new File(path);
		if(file.isDirectory()){
			File[] list = file.listFiles();
			file = new File(list[0].getAbsolutePath()); /* we were passed a folder, but we want the full file name, for storing the probability map with correct name */
		}
		String outputFolderPath = file.getParent();
		String filename = removeExtension(file.getName());
		String processedImageFileName = outputFolderPath + "/" + filename + "__model_" + checksum + ".tif";

		/**
		 *  create or load probability maps
		 */
		Img<FloatType> probabilityMap;
		if (!new File(processedImageFileName).exists()) {
			probabilityMap = unetProcessor.process(imgTemp);
			ImagePlus tmp_image = ImageJFunctions.wrap(probabilityMap, "tmp_image");
			IJ.saveAsTiff(tmp_image, processedImageFileName);
		} else {
			ImagePlus imp = IJ.openImage(processedImageFileName);
			probabilityMap = ImageJFunctions.convertFloat(imp);
		}
		return probabilityMap;
	}

	/**
	 * Creates and triggers filling of mmILP, containing all
	 * optimization-related structures used to compute the optimal tracking.
	 */
	private void generateILPs() {
		for ( final GrowthLine gl : getGrowthLines() ) {
			gl.generateILP( null );
		}
	}

	/**
	 * Runs all the generated ILPs.
	 */
	private void runILPs() {
		int i = 0;
		for ( final GrowthLine gl : getGrowthLines() ) {
			System.out.println( " > > > > > Starting LP for GL# " + i + " < < < < < " );
			gl.runILP();
			i++;
		}
	}

	/**
	 * @return the guiFrame
	 */
	public static JFrame getGuiFrame() {
		return guiFrame;
	}

	/**
	 * @return the MotherMachineGui instance.
	 */
	public static MoMAGui getGui() {
		return gui;
	}

	/**
	 * @return the defaultFilenameDecoration
	 */
	public static String getDefaultFilenameDecoration() {
		return defaultFilenameDecoration;
	}

	/**
	 * @param defaultFilenameDecoration
	 *            the defaultFilenameDecoration to set
	 */
	public static void setDefaultFilenameDecoration( final String defaultFilenameDecoration ) {
		MoMA.defaultFilenameDecoration = FilenameUtils.removeExtension(defaultFilenameDecoration);
	}

	/**
	 * @return the first time-point loaded
	 */
	public static int getMinTime() {
		return minTime;
	}

	/**
	 * @return the last loaded time-point
	 */
	public static int getMaxTime() {
		return maxTime;
	}

	/**
	 * @return the initial optimization range, -1 if it is infinity.
	 */
	public static int getInitialOptRange() {
		return initOptRange;
	}

	/**
	 * @return the first channel index of the loaded data
	 */
	public static int getMinChannelIdx() {
		return minChannelIdx;
	}

	/**
	 * @return the number of channels loaded
	 */
	public static int getNumChannels() {
		return numChannels;
	}

	/**
	 * Allows one to restart by GL segmentation. This is e.g. needed after top
	 * or bottom offsets are altered, which invalidates all analysis run so far.
	 */
	public void restartFromGLSegmentation() {
		boolean hideConsoleLater = false;
		if ( !HEADLESS && !isConsoleVisible() ) {
			showConsoleWindow( true );
			hideConsoleLater = true;
		}

		System.out.print( "Searching for GrowthLines..." );
		resetImgTempToRaw();
        findGrowthLines();
		System.out.println( " done!" );

		System.out.println( "Generating Segmentation Hypotheses..." );
		resetImgTempToRaw();
		generateAllSimpleSegmentationHypotheses();
		System.out.println( " done!" );

		if ( !HEADLESS && hideConsoleLater ) {
			showConsoleWindow( false );
		}
	}


	/**
	 * @param datasetName the datasetName to set
	 */
	private void setDatasetName(final String datasetName) {
		this.datasetName = datasetName;
		if ( MoMA.getGuiFrame() != null ) {
			MoMA.getGuiFrame().setTitle( String.format( "%s -- %s", MoMA.VERSION_STRING, this.datasetName ) );
		}
	}
}
