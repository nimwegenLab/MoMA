package com.jug;

import com.google.common.io.Files;
import com.jug.config.CommandLineArgumentsParser;
import com.jug.config.ConfigurationManager;
import com.jug.datahandling.FilePaths;
import com.jug.datahandling.ImageProvider;
import com.jug.datahandling.InitializationHelpers;
import com.jug.gui.LoggerWindow;
import com.jug.gui.MoMAGui;
import com.jug.gui.WindowFocusListenerImplementation;
import com.jug.intialization.SetupValidator;
import com.jug.util.PseudoDic;
import ij.ImageJ;
import net.imagej.patcher.LegacyInjector;
import net.imglib2.img.display.imagej.ImageJFunctions;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Path;

/**
 * @author jug
 */
public class MoMA {
	private static ConfigurationManager configurationManager;
	private static CommandLineArgumentsParser commandLineArgumentParser;
	private static LoggerWindow loggerWindow;

	static {
		LegacyInjector.preinit();
	}

	/**
	 * This is the pseudo dependency injection container, which we use to clean-up and initialize our instances.
	 */
	public static PseudoDic dic;

	/**
	 * Controls if ImageJ and loaded data will be shown.
	 */
	static boolean showIJ = false;

	public static boolean running_as_Fiji_plugin = false;

	/**
	 * The <code>JFrame</code> containing the main GUI.
	 */
	public static JFrame guiFrame;

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


	/**
	 * PROJECT MAIN
	 *
	 * @param args
	 */
	public static void main( final String[] args ) {
		dic = new PseudoDic();

		/* parse command line arguments */
		commandLineArgumentParser = new CommandLineArgumentsParser(running_as_Fiji_plugin);
		commandLineArgumentParser.parse(args);

		if (SetupValidator.checkGurobiInstallation(commandLineArgumentParser.getIfRunningHeadless(), running_as_Fiji_plugin)) return;

		/* setup configuration manager and read configuration */
		FilePaths filePaths = new FilePaths();

		configurationManager = dic.getConfigurationManager();
		configurationManager.setIfRunningHeadless(commandLineArgumentParser.getIfRunningHeadless());
		configurationManager.GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY = commandLineArgumentParser.getShowGroundTruthFunctionality();

		Path inputFolder = null;
		final InitializationHelpers datasetProperties = new InitializationHelpers();
		if (commandLineArgumentParser.isReloadingData()) {
			filePaths.setLoadingDirectoryPath(commandLineArgumentParser.getReloadFolderPath());
			configurationManager.load(filePaths.getPropertiesFile(), userMomaHomePropertyFile, momaUserDirectory);
//			inputFolder;
			datasetProperties.readDatasetProperties(inputFolder.toFile());
			throw new NotImplementedException("Implementation of the reloading feature is still unfinished!");
		} else {
			configurationManager.load(commandLineArgumentParser.getOptionalPropertyFile(), userMomaHomePropertyFile, momaUserDirectory);

			/* overwrite configuration values with parsed command line values, if needed */
			inputFolder = commandLineArgumentParser.getInputFolder();
			if (commandLineArgumentParser.getUserDefinedMinTime() != -1) {
				configurationManager.setMinTime(commandLineArgumentParser.getUserDefinedMinTime());
			}
			if (commandLineArgumentParser.getUserDefinedMaxTime() != -1) {
				configurationManager.setMaxTime(commandLineArgumentParser.getUserDefinedMaxTime());
			}
			configurationManager.setOutputPath(commandLineArgumentParser.getOutputPath().toString());

			/* test validity of the minimum or maximum times or use dataset values, if not specified */
			datasetProperties.readDatasetProperties(inputFolder.toFile());
			if (configurationManager.getMinTime() == -1) {
				configurationManager.setMinTime(datasetProperties.getMinTime());
			} else {
				if(configurationManager.getMinTime() < datasetProperties.getMinTime() || configurationManager.getMinTime() > datasetProperties.getMaxTime()){
					throw new RuntimeException("minimum value of time range to analyze is invalid.");
				}
			}
			if (configurationManager.getMaxTime() == -1) {
				configurationManager.setMaxTime(datasetProperties.getMaxTime());
			} else {
				if(configurationManager.getMaxTime() < datasetProperties.getMinTime() || configurationManager.getMaxTime() > datasetProperties.getMaxTime()){
					throw new RuntimeException("maximum value of time range to analyze is invalid.");
				}
			}
		}

		loggerWindow = dic.getLoggerWindow();

		System.out.println( "VERSION: " + dic.getGitVersionProvider().getVersionString() );

		if ( !commandLineArgumentParser.getIfRunningHeadless() ) {
			guiFrame = new JFrame();
			dic.getMomaInstance().initMainWindow( guiFrame );

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

			// Setting up console window...
			loggerWindow.initConsoleWindow();
			loggerWindow.showConsoleWindow( true );
		}

		if ( inputFolder == null || inputFolder.equals( "" ) ) {
			inputFolder = dic.getMomaInstance().showStartupDialog( guiFrame, configurationManager.getInputImagePath());
		}
		defaultFilenameDecoration = inputFolder.getFileName().toString();
		System.out.println( "Default filename decoration = " + defaultFilenameDecoration );
		configurationManager.setImagePath(inputFolder.toAbsolutePath().toString());

		final File folder = new File(configurationManager.getInputImagePath());
		dic.getMomaInstance().setDatasetName( String.format( "%s >> %s", folder.getParentFile().getName(), folder.getName() ) );
		ImageProvider imageProvider = new ImageProvider();
		dic.setImageProvider(imageProvider);
		try {
			if ( datasetProperties.getNumChannels() == 0 ) { throw new Exception( "At least one color channel must be loaded!" ); }

			imageProvider.loadTiffsFromFileOrFolder(configurationManager.getInputImagePath(),
					configurationManager.getMinTime(),
					configurationManager.getMaxTime(),
					datasetProperties.getMinChannelIdx(),
					datasetProperties.getNumChannels() + datasetProperties.getMinChannelIdx() - 1);

			dic.getGlDataLoader().restartFromGLSegmentation();

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

		// show loaded and annotated data
		if (showIJ) {
			new ImageJ();
			ImageJFunctions.show( dic.getImageProvider().getImgRaw(), "Rotated & cropped raw data" );
		}

		if ( !commandLineArgumentParser.getIfRunningHeadless() ) {
			SwingUtilities.invokeLater(() -> {
				System.out.print( "Build GUI..." );
				loggerWindow.showConsoleWindow(false);

				guiFrame.add(dic.getMomaGui());
				guiFrame.setSize(configurationManager.GUI_WIDTH, configurationManager.GUI_HEIGHT);
				guiFrame.setLocation(configurationManager.GUI_POS_X, configurationManager.GUI_POS_Y);
				guiFrame.addWindowFocusListener(new WindowFocusListenerImplementation(dic.getMomaGui()));

				guiFrame.setVisible(true);
				System.out.println( " done!" );
			});
		} else {
			dic.getMomaGui().exportHtmlOverview();
			dic.getMomaGui().exportDataFiles(new File(configurationManager.getOutputPath()));

			configurationManager.saveParams(getGuiFrame());

			if (!running_as_Fiji_plugin) {
				System.exit( 11 );
			}
		}
	}

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
	private Path showStartupDialog( final JFrame guiFrame, final String datapath ) {

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
		return file.toPath();
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
				public boolean accept( final File file ) {
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
		return dic.getMomaGui();
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
