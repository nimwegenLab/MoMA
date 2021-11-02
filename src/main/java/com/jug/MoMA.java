package com.jug;

import com.jug.config.ConfigurationManager;
import com.jug.datahandling.IImageProvider;
import com.jug.gui.MoMAGui;
import com.jug.gui.MoMAModel;
import com.jug.gui.WindowFocusListenerImplementation;
import com.jug.util.FloatTypeImgLoader;
import com.jug.util.PseudoDic;
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
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
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
public class MoMA implements IImageProvider {
//	public static final int SCALE_FACTOR = 4;
	private static ConfigurationManager configurationManager;

	static {
		LegacyInjector.preinit();
	}

	/**
	 * This is the pseudo dependency injection container, which we use to clean-up and initialize our instances.
	 */
	public static PseudoDic dic;

	// -------------------------------------------------------------------------------------
	// statics
	// -------------------------------------------------------------------------------------
	/*
	  Control if ImageJ and loaded data will be shown...
	 */
	static boolean showIJ = false;

	public static boolean HEADLESS = false;
	public static boolean running_as_Fiji_plugin = false;


	// - - - - - - - - - - - - - -
	// Info about loaded data
	// - - - - - - - - - - - - - -
	private static int minTime = -1;
	private static int maxTime = -1;
	private static int initialOptimizationRange = -1;
	private static int minChannelIdx = 1;
	private static int numChannels = 1;


	// - - - - - - - - - - - - - -
	// GUI-WINDOW RELATED STATICS
	// - - - - - - - - - - - - - -
	/**
	 * The <code>JFrame</code> containing the main GUI.
	 */
	public static JFrame guiFrame;

	/**
	 * Properties to configure app (loaded and saved to properties file!).
	 */
	public static Properties props = null;


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

	public static boolean GUI_OPTIMIZE_ON_ILP_CHANGE = true;

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
	private static final File userMomaHomePropertyFile = new File(momaUserDirectory.getPath() + "/mm.properties");

	/**
	 * Stores a string used to decorate filenames e.g. before export.
	 */
	private static String defaultFilenameDecoration;

	/**
	 * Path to the dataset that we are working on.
	 */
	public static String IMAGE_PATH;


	// ====================================================================================================================

	/**
	 * PROJECT MAIN
	 *
	 * @param args
	 */
	public static void main( final String[] args ) {
		configurationManager = new ConfigurationManager();
		configurationManager.load(optionalPropertyFile, userMomaHomePropertyFile, momaUserDirectory);

		final MoMA main = new MoMA();

		dic = new PseudoDic(configurationManager, main);

		System.out.println( "VERSION: " + dic.getGitVersionProvider().getVersionString() );

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
			configurationManager.GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY = true;
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
		System.out.println("Determined minTime: " + minTime);
		System.out.println("Determined maxTime: " + maxTime);

		System.out.println("Determined minChannelIdx: " + minChannelIdx);
		System.out.println("Determined numChannels: " + numChannels);


		if ( cmd.hasOption( "tmin" ) ) {
			minTime = Integer.parseInt( cmd.getOptionValue( "tmin" ) );
		}
		if ( cmd.hasOption( "tmax" ) ) {
			maxTime = Integer.parseInt( cmd.getOptionValue( "tmax" ) );
		}

		if ( cmd.hasOption( "optrange" ) ) {
			initialOptimizationRange = Integer.parseInt( cmd.getOptionValue( "optrange" ) );
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

		if ( !HEADLESS ) {
			guiFrame = new JFrame();
			main.initMainWindow( guiFrame );
		}

		if ( !HEADLESS ) {
			// Iterate over all currently attached monitors and check if sceen
			// position is actually possible,
			// otherwise fall back to the DEFAULT values and ignore the ones
			// coming from the properties-file.
			boolean pos_ok = false;
			final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			final GraphicsDevice[] gs = ge.getScreenDevices();
			for (GraphicsDevice g : gs) {
				if (g.getDefaultConfiguration().getBounds().contains(new java.awt.Point(configurationManager.GUI_POS_X, configurationManager.GUI_POS_Y))) {
					pos_ok = true;
				}
			}
			// None of the screens contained the top-left window coordinates -->
			// fall back onto default values...
			if ( !pos_ok ) {
				configurationManager.GUI_POS_X = configurationManager.DEFAULT_GUI_POS_X;
				/*
				  Default y-position of the main GUI-window. This value will be used if the
				  values in the properties file are not fitting on any of the currently
				  attached screens.
				 */
				int DEFAULT_GUI_POS_Y = 100;
				configurationManager.GUI_POS_Y = DEFAULT_GUI_POS_Y;
			}
		}

		IMAGE_PATH = props.getProperty( "import_path", System.getProperty( "user.home" ) );
		if ( inputFolder == null || inputFolder.equals( "" ) ) {
			inputFolder = main.showStartupDialog( guiFrame, IMAGE_PATH);
		}
		System.out.println( "Default filename decoration = " + inputFolder.getName() );
		defaultFilenameDecoration = inputFolder.getName();
		IMAGE_PATH = inputFolder.getAbsolutePath();
		props.setProperty( "import_path", IMAGE_PATH);

		if ( !HEADLESS ) {
			// Setting up console window...
			main.initConsoleWindow();
			main.showConsoleWindow( true );
		}

		// ------------------------------------------------------------------------------------------------------
		// ------------------------------------------------------------------------------------------------------
		final MoMAModel mmm = new MoMAModel( main );
		try {
			main.processDataFromFolder(IMAGE_PATH, minTime, maxTime, minChannelIdx, numChannels );
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

		gui = new MoMAGui( mmm, dic.getMomaInstance(), dic.getMomaInstance(), configurationManager.GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY);


		if ( !HEADLESS ) {
			SwingUtilities.invokeLater(() -> {
				System.out.print( "Build GUI..." );
				main.showConsoleWindow(false);

				guiFrame.add(gui);
				guiFrame.setSize(ConfigurationManager.GUI_WIDTH, ConfigurationManager.GUI_HEIGHT);
				guiFrame.setLocation(ConfigurationManager.GUI_POS_X, ConfigurationManager.GUI_POS_Y);
				guiFrame.addWindowFocusListener(new WindowFocusListenerImplementation(gui));

				gui.setVisible(true);
				guiFrame.setVisible(true);
				System.out.println( " done!" );
			});
		} else {
			gui.exportHtmlOverview();
			gui.exportDataFiles();

			configurationManager.saveParams(getGuiFrame());

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

	/**
	 * Contains all Growthlanes found in the given data.
	 */
	private List<Growthlane> growthlanes;

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
	 * @return the imgProbs
	 */
	public Img< FloatType > getImgProbs() {
		return imgProbs;
	}

	public Img<FloatType> getColorChannelAtTime(int channel, int timestep) {
		return ImgView.wrap(Views.hyperSlice(dic.getImageProvider().getRawChannelImgs().get(channel), 2, timestep));
	}

	/**
	 * @param imgTemp
	 *            the imgTemp to set
	 */
	private void setImgTemp(final Img<FloatType> imgTemp) {
		this.imgTemp = imgTemp;
	}

	/**
	 * @return the growthlanes
	 */
	public List<Growthlane> getGrowthlanes() {
		return growthlanes;
	}

	/**
	 * @param growthlanes
	 *            the growthlanes to set
	 */
	private void setGrowthlanes(final List<Growthlane> growthlanes) {
		this.growthlanes = growthlanes;
	}

	// -------------------------------------------------------------------------------------
	// methods
	// -------------------------------------------------------------------------------------

	/**
	 * Created and shows the console window and redirects System.out and
	 * System.err to it.
	 */
	private void initConsoleWindow() {
		frameConsoleWindow = new JFrame( String.format( "%s Console Window", dic.getGitVersionProvider().getVersionString() ) );
		// frameConsoleWindow.setResizable( false );
		consoleWindowTextArea = new JTextArea();
		consoleWindowTextArea.setLineWrap( true );
		consoleWindowTextArea.setWrapStyleWord( true );

		final int centerX = ( int ) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2;
		final int centerY = ( int ) Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2;
		frameConsoleWindow.setBounds(centerX - configurationManager.GUI_CONSOLE_WIDTH / 2, centerY - configurationManager.GUI_HEIGHT / 2, configurationManager.GUI_CONSOLE_WIDTH, configurationManager.GUI_HEIGHT);
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
				configurationManager.saveParams(getGuiFrame());
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

		imgRaw = rawChannelImgs.get( 0 );

		restartFromGLSegmentation(this);

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
     * NOTE: This method is kept to be compatible with down-stream code.
	 * Write the centers of the growth line given in 'imgTemp'. Since it is centered
     * in the image, we set the coordinates to the center of the image. Note, that
     * this method is a legacy artifact. Legacy-Moma was able to treat full-frames with
     * multiple GL inside an image by detecting them. This now no longer necessary after
     * doing the preprocessing, so that we can simplify this method, the way we did.
	 */
    private void findGrowthlanes(IImageProvider imageProvider) {
        this.setGrowthlanes(new ArrayList<>() );
        getGrowthlanes().add( new Growthlane(imageProvider) );

        for ( long frameIdx = 0; frameIdx < imgTemp.dimension( 2 ); frameIdx++ ) {
            GrowthlaneFrame currentFrame = new GrowthlaneFrame((int) frameIdx, dic.getComponentTreeGenerator());
            final IntervalView< FloatType > ivFrame = Views.hyperSlice( imgTemp, 2, frameIdx );
            currentFrame.setImage(ImgView.wrap(ivFrame, new ArrayImgFactory(new FloatType())));
            getGrowthlanes().get(0).add(currentFrame);
        }
    }


	/**
	 * Iterates over all found Growthlanes and evokes
	 * Growthlane.findGapHypotheses(Img). Note that this function always uses
	 * the image data in 'imgTemp'.
	 */
	private void generateAllSimpleSegmentationHypotheses() {
		imgProbs = processImageOrLoadFromDisk();
		for ( final Growthlane gl : getGrowthlanes() ) {
			gl.getFrames().parallelStream().forEach((glf) -> {
				System.out.print( "." );
				glf.generateSimpleSegmentationHypotheses( this, glf.getFrameIndex() );
			});
			System.out.println( " ...done!" );
		}
	}

	private Img<FloatType> processImageOrLoadFromDisk() {
		UnetProcessor unetProcessor = dic.getUnetProcessor();

		Img<FloatType> probabilityMap = unetProcessor.process(imgTemp);

		String checksum = unetProcessor.getModelChecksum();
		/**
		 *  generate probability filename
		 */
		File file = new File(IMAGE_PATH);
		if(file.isDirectory()){
			File[] list = file.listFiles();
			file = new File(list[0].getAbsolutePath()); /* we were passed a folder, but we want the full file name, for storing the probability map with correct name */
		}
		String outputFolderPath = file.getParent();
		String filename = removeExtension(file.getName());
		String processedImageFileName = outputFolderPath + "/" + filename + "__model_" + checksum + ".tif";

		ImagePlus tmp_image = ImageJFunctions.wrap(probabilityMap, "tmp_image");
		IJ.saveAsTiff(tmp_image, processedImageFileName);

		return probabilityMap;

//		String checksum = unetProcessor.getModelChecksum();
//		/**
//		 *  generate probability filename
//		 */
//		File file = new File(IMAGE_PATH);
//		if(file.isDirectory()){
//			File[] list = file.listFiles();
//			file = new File(list[0].getAbsolutePath()); /* we were passed a folder, but we want the full file name, for storing the probability map with correct name */
//		}
//		String outputFolderPath = file.getParent();
//		String filename = removeExtension(file.getName());
//		String processedImageFileName = outputFolderPath + "/" + filename + "__model_" + checksum + ".tif";
//
//		/**
//		 *  create or load probability maps
//		 */
//		Img<FloatType> probabilityMap;
//		if (!new File(processedImageFileName).exists()) {
//			probabilityMap = unetProcessor.process(imgTemp);
//			ImagePlus tmp_image = ImageJFunctions.wrap(probabilityMap, "tmp_image");
//			IJ.saveAsTiff(tmp_image, processedImageFileName);
//		} else {
//			ImagePlus imp = IJ.openImage(processedImageFileName);
//			probabilityMap = ImageJFunctions.convertFloat(imp);
//		}
//		return probabilityMap;
	}

	/**
	 * Creates and triggers filling of mmILP, containing all
	 * optimization-related structures used to compute the optimal tracking.
	 */
	private void generateILPs() {
		for ( final Growthlane gl : getGrowthlanes() ) {
			gl.generateILP( null );
		}
	}

	/**
	 * Runs all the generated ILPs.
	 */
	private void runILPs() {
		int i = 0;
		for ( final Growthlane gl : getGrowthlanes() ) {
			System.out.println( " > > > > > Starting LP for GL# " + i + " < < < < < " );
			gl.getIlp().run();
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
		MoMA.defaultFilenameDecoration = defaultFilenameDecoration;
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
	public static int getInitialOptimizationRange() {
		return initialOptimizationRange;
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
	public void restartFromGLSegmentation(IImageProvider imageProvider) {
		boolean hideConsoleLater = false;
		if ( !HEADLESS && !isConsoleVisible() ) {
			showConsoleWindow( true );
			hideConsoleLater = true;
		}

		System.out.print( "Searching for Growthlanes..." );
		resetImgTempToRaw();
        findGrowthlanes(imageProvider);
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
			MoMA.getGuiFrame().setTitle( String.format( "MoMA %s -- %s", dic.getGitVersionProvider().getVersionString(), this.datasetName ) );
		}
	}
}
