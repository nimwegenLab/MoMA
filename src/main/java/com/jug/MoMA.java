package com.jug;

import com.jug.config.CommandLineArgumentsParser;
import com.jug.config.ConfigurationManager;
import com.jug.datahandling.ImageProvider;
import com.jug.datahandling.InitializationHelpers;
import com.jug.gui.LoggerWindow;
import com.jug.gui.MoMAGui;
import com.jug.gui.WindowFocusListenerImplementation;
import com.jug.util.PseudoDic;
import gurobi.GRBEnv;
import gurobi.GRBException;
import ij.ImageJ;
import net.imagej.patcher.LegacyInjector;
import net.imglib2.img.display.imagej.ImageJFunctions;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Properties;

/*
  Main class for the MotherMachine project.
 */

/**
 * @author jug
 */
public class MoMA {
	private static ConfigurationManager configurationManager;
	private static ImageProvider imageProvider;
	private static CommandLineArgumentsParser commandLineArgumentParser;
	private static LoggerWindow loggerWindow;

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

	public static boolean running_as_Fiji_plugin = false;


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
	 * Property file in the moma directory the user.
	 */
	private static final File userMomaHomePropertyFile = new File(momaUserDirectory.getPath() + "/mm.properties");

	/**
	 * Stores a string used to decorate filenames e.g. before export.
	 */
	private static String defaultFilenameDecoration;


	// ====================================================================================================================

	/**
	 * PROJECT MAIN
	 *
	 * @param args
	 */
	public static void main( final String[] args ) {
		if (checkGurobiInstallation()) return;

		commandLineArgumentParser = new CommandLineArgumentsParser(running_as_Fiji_plugin);
		commandLineArgumentParser.parse(args);
		File inputFolder = commandLineArgumentParser.getInputFolder();

		final InitializationHelpers datasetProperties = new InitializationHelpers();
		datasetProperties.readDatasetProperties(inputFolder);

		configurationManager = new ConfigurationManager();
		configurationManager.load(commandLineArgumentParser.getOptionalPropertyFile(), userMomaHomePropertyFile, momaUserDirectory);
		configurationManager.GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY = commandLineArgumentParser.getShowGroundTruthFunctionality();

		configurationManager.setMinTime(datasetProperties.getMinTime());
		configurationManager.setMaxTime(datasetProperties.getMaxTime());

		if (commandLineArgumentParser.getUserDefinedMinTime() > 0 && commandLineArgumentParser.getUserDefinedMinTime() > datasetProperties.getMinTime()) {
			configurationManager.setMinTime(commandLineArgumentParser.getUserDefinedMinTime());
		}
		if (commandLineArgumentParser.getUserDefinedMaxTime() > 0 && commandLineArgumentParser.getUserDefinedMaxTime() < datasetProperties.getMaxTime()) {
			configurationManager.setMaxTime(commandLineArgumentParser.getUserDefinedMaxTime());
		}

		configurationManager.setOutputPath(commandLineArgumentParser.getOutputPath());

		final MoMA main = new MoMA();

		dic = new PseudoDic(configurationManager, main);
		loggerWindow = dic.getLoggerWindow();

		System.out.println( "VERSION: " + dic.getGitVersionProvider().getVersionString() );

		if ( !commandLineArgumentParser.getIfRunningHeadless() ) {
			guiFrame = new JFrame();
			main.initMainWindow( guiFrame );
		}

		if ( !commandLineArgumentParser.getIfRunningHeadless() ) {
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

		if ( inputFolder == null || inputFolder.equals( "" ) ) {
			inputFolder = main.showStartupDialog( guiFrame, configurationManager.getImagePath());
		}
		System.out.println( "Default filename decoration = " + inputFolder.getName() );
		defaultFilenameDecoration = inputFolder.getName();
		configurationManager.setImagePath(inputFolder.getAbsolutePath());

		if ( !commandLineArgumentParser.getIfRunningHeadless() ) {
			// Setting up console window...
			loggerWindow.initConsoleWindow();
			loggerWindow.showConsoleWindow( true );
		}

		final File folder = new File(configurationManager.getImagePath());
		main.setDatasetName( String.format( "%s >> %s", folder.getParentFile().getName(), folder.getName() ) );
		try {
			if ( datasetProperties.getNumChannels() == 0 ) { throw new Exception( "At least one color channel must be loaded!" ); }

			imageProvider = new ImageProvider();
			imageProvider.loadTiffsFromFileOrFolder(configurationManager.getImagePath(),
					configurationManager.getMinTime(),
					configurationManager.getMaxTime(),
					datasetProperties.getMinChannelIdx(),
					datasetProperties.getNumChannels() + datasetProperties.getMinChannelIdx() - 1);
			dic.setImageProvider(imageProvider);

			boolean hideConsoleLater = false;
			if ( !commandLineArgumentParser.getIfRunningHeadless() && !loggerWindow.isConsoleVisible() ) {
				loggerWindow.showConsoleWindow( true );
				hideConsoleLater = true;
			}

			dic.getGlDataLoader().restartFromGLSegmentation(imageProvider);
			if ( !commandLineArgumentParser.getIfRunningHeadless() && hideConsoleLater ) {
				loggerWindow.showConsoleWindow( false );
			}

			if ( commandLineArgumentParser.getIfRunningHeadless() ) {
				System.out.println( "Generating Integer Linear Program(s)..." );
				dic.getGlDataLoader().generateILPs();
				System.out.println( " done!" );

				System.out.println( "Running Integer Linear Program(s)..." );
				dic.getGlDataLoader().runILPs();
				System.out.println( " done!" );
			}
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
			ImageJFunctions.show( imageProvider.getImgRaw(), "Rotated & cropped raw data" );
		}

		gui = dic.getMomaGui();

		if ( !commandLineArgumentParser.getIfRunningHeadless() ) {
			SwingUtilities.invokeLater(() -> {
				System.out.print( "Build GUI..." );
				loggerWindow.showConsoleWindow(false);

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
			gui.exportDataFiles(new File(configurationManager.getOutputPath()));

			configurationManager.saveParams(getGuiFrame());

			if (!running_as_Fiji_plugin) {
				System.exit( 11 );
			}
		}
	}

	private static boolean checkGurobiInstallation() {
		final String jlp = System.getProperty( "java.library.path" );
		try {
			new GRBEnv( "MoMA_gurobi.log" );
		} catch ( final GRBException e ) {
			final String msgs = "Initial Gurobi test threw exception... check your Gruobi setup!\n\nJava library path: " + jlp;
			if ( commandLineArgumentParser.getIfRunningHeadless() ) {
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
				return true;
			}
		} catch ( final UnsatisfiedLinkError ulr ) {
			final String msgs = "Could not initialize Gurobi.\n" + "You might not have installed Gurobi properly or you miss a valid license.\n" + "Please visit 'www.gurobi.com' for further information.\n\n" + ulr.getMessage() + "\nJava library path: " + jlp;
			if ( commandLineArgumentParser.getIfRunningHeadless() ) {
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
				return true;
			}
		}
		return false;
	}

	// -------------------------------------------------------------------------------------
	// fields
	// -------------------------------------------------------------------------------------

	/**
	 * String denoting the name of the loaded dataset (e.g. used in GUI)
	 */
	private String datasetName;

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
	 * @return the initial optimization range, -1 if it is infinity.
	 */
	public static int getInitialOptimizationRange() {
		return commandLineArgumentParser.getInitialOptimizationRange();
	}

	public static boolean getIfRunningHeadless() {
		return commandLineArgumentParser.getIfRunningHeadless();
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
