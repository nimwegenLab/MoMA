package com.jug;

import com.jug.config.CommandLineArgumentsParser;
import com.jug.config.ConfigurationManager;
import com.jug.datahandling.DatasetProperties;
import com.jug.datahandling.ImageProvider;
import com.jug.gui.LoggerWindow;
import com.jug.gui.MoMAGui;
import com.jug.gui.WindowFocusListenerImplementation;
import com.jug.intialization.SetupValidator;
import com.jug.util.PseudoDic;
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
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Objects.isNull;

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

	public static boolean runningAsFijiPlugin = false;

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
		commandLineArgumentParser = dic.getCommandLineArgumentParser();
		commandLineArgumentParser.setRunningAsFijiPlugin(runningAsFijiPlugin);
		commandLineArgumentParser.parse(args);

		if (SetupValidator.checkGurobiInstallation(commandLineArgumentParser.getIfRunningHeadless(), runningAsFijiPlugin)) {
			System.exit(-1);
		}

		/* setup configuration manager and read configuration */
		configurationManager = dic.getConfigurationManager();
		configurationManager.setIfRunningHeadless(commandLineArgumentParser.getIfRunningHeadless());
		configurationManager.GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY = commandLineArgumentParser.getShowGroundTruthFunctionality();

		dic.getFilePaths().setAnalysisName(commandLineArgumentParser.getAnalysisName());
		final DatasetProperties datasetProperties = new DatasetProperties();
		configurationManager.setIsReloading(commandLineArgumentParser.isReloadingData());
		if (commandLineArgumentParser.isReloadingData()) {
			dic.getFilePaths().setLoadingDirectoryPath(commandLineArgumentParser.getReloadFolderPath());
			configurationManager.load(dic.getFilePaths().getPropertiesFile(), userMomaHomePropertyFile, momaUserDirectory);
			if (!dic.getVersionCompatibilityChecker().versionAreCompatible(configurationManager.getDatasetMomaVersion(), dic.getGitVersionProvider().getVersionString())) {
				System.out.println(dic.getVersionCompatibilityChecker().getErrorMessage(configurationManager.getDatasetMomaVersion(), dic.getGitVersionProvider().getVersionString()));
				System.exit(-1);;
			}
			dic.getFilePaths().setModelFilePath(dic.getConfigurationManager().SEGMENTATION_MODEL_PATH);
			dic.getFilePaths().setInputImagePath(Paths.get(configurationManager.getInputImagePath()));
			datasetProperties.readDatasetProperties(dic.getFilePaths().getInputImagePath().toFile());
		} else {
			Path optionalPropertiesFilePath = commandLineArgumentParser.getOptionalPropertyFile();
			if (!isNull(optionalPropertiesFilePath) && !optionalPropertiesFilePath.toFile().exists()) {
				System.out.println("ERROR: Properties file does not exist (check argument -p): " + optionalPropertiesFilePath); /* TODO-MM-20220729: create a class that validates user-inputs/command-line argument combinations and values */
				System.exit(-1);
			}
			dic.getFilePaths().setPropertiesFile(optionalPropertiesFilePath);
			configurationManager.load(dic.getFilePaths().getPropertiesFile(), userMomaHomePropertyFile, momaUserDirectory);
			dic.getFilePaths().setModelFilePath(dic.getConfigurationManager().SEGMENTATION_MODEL_PATH);
			dic.getFilePaths().setInputImagePath(commandLineArgumentParser.getInputImagePath());
			dic.getFilePaths().setOutputPath(commandLineArgumentParser.getInputImagePath().getParent());
			dic.getFilePaths().setAnalysisName(commandLineArgumentParser.getAnalysisName());
			datasetProperties.readDatasetProperties(dic.getFilePaths().getInputImagePath().toFile());

			configurationManager.setMinTime(datasetProperties.getMinTime());
			configurationManager.setMaxTime(datasetProperties.getMaxTime());

			/* overwrite configuration values with parsed command line values, if needed */
			if (commandLineArgumentParser.getUserDefinedMinTime() == 0) {
				throw new RuntimeException("minimum value of time range to analyze is invalid; must be at least 1; we use a 1-based time-index like in ImageJ");
			}
			if (commandLineArgumentParser.getUserDefinedMinTime() != -1) {
				if (datasetProperties.timestepInsideRange(commandLineArgumentParser.getUserDefinedMinTime())) {
					configurationManager.setMinTime(commandLineArgumentParser.getUserDefinedMinTime());
				} else {
					throw new RuntimeException("minimum value of user-specified time range is invalid.");
				}
			}
			if (commandLineArgumentParser.getUserDefinedMaxTime() != -1) {
				if (datasetProperties.timestepInsideRange(commandLineArgumentParser.getUserDefinedMaxTime())) {
					configurationManager.setMaxTime(commandLineArgumentParser.getUserDefinedMaxTime());
				} else {
					throw new RuntimeException("maximum value of user-specified time range is invalid.");
				}
			}
		}
		configurationManager.setSatasetMomaVersion(dic.getGitVersionProvider().getVersionString()); /* update the dataset Moma version that will be written to future exported dataset */

		loggerWindow = dic.getLoggerWindow();

		System.out.println( "VERSION: " + dic.getGitVersionProvider().getVersionString() );

		if ( !commandLineArgumentParser.getIfRunningHeadless() ) {
			guiFrame = dic.getGuiFrame();

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

		if (dic.getFilePaths().getInputImagePath() == null) {
			dic.getFilePaths().setInputImagePath(dic.getMomaInstance().showStartupDialog(guiFrame, configurationManager.getInputImagePath()));
		}
		defaultFilenameDecoration = dic.getFilePaths().getInputImagePath().getFileName().toString();
		System.out.println( "Default filename decoration = " + defaultFilenameDecoration );
		configurationManager.setImagePath(dic.getFilePaths().getInputImagePath().toAbsolutePath().toString());

		final File folder = dic.getFilePaths().getInputImagePath().toFile();
		dic.setDatasetNameInWindowTitle(String.format("%s >> %s", folder.getParentFile().getName(), folder.getName()));
		ImageProvider imageProvider = new ImageProvider();
		dic.setImageProvider(imageProvider);
		try {
			if (datasetProperties.getNumChannels() == 0) {
				throw new Exception("At least one color channel must be loaded!");
			}

			imageProvider.loadTiffsFromFileOrFolder(dic.getFilePaths().getInputImagePath().toString(),
					configurationManager.getMinTime(),
					configurationManager.getMaxTime(),
					datasetProperties.getMinChannelIdx(),
					datasetProperties.getNumChannels() + datasetProperties.getMinChannelIdx() - 1);

			dic.getGlDataLoader().restartFromGLSegmentation();

			if (commandLineArgumentParser.getIfRunningHeadless()) {
				System.out.println("Generating Integer Linear Program...");
				dic.getGlDataLoader().generateILPs();
				System.out.println(" done!");

				System.out.println("Running Integer Linear Program...");
				dic.getGlDataLoader().runILPs();
				System.out.println(" done!");
			}
		} catch ( final Exception e ) {
			e.printStackTrace();
			if (!runningAsFijiPlugin) {
				System.exit( 11 );
			} else {
				return;
			}
		}

		// show loaded and annotated data
		if (showIJ) {
			new ImageJ();
			ImageJFunctions.show(dic.getImageProvider().getImgRaw(), "Rotated & cropped raw data");
		}

		if (!commandLineArgumentParser.getIfRunningHeadless()) {
			SwingUtilities.invokeLater(() -> {
				loggerWindow.showConsoleWindow(false);
				guiFrame.add(dic.getMomaGui());
				guiFrame.setSize(configurationManager.GUI_WIDTH, configurationManager.GUI_HEIGHT);
				guiFrame.setLocation(configurationManager.GUI_POS_X, configurationManager.GUI_POS_Y);
				guiFrame.addWindowFocusListener(new WindowFocusListenerImplementation(dic.getMomaGui()));
				guiFrame.setVisible(true);
			});

			if (commandLineArgumentParser.isReloadingData()) {
				SwingUtilities.invokeLater(() -> { /* run optimization on UI thread to ensure the GUI has finished displaying before; this is not good code */
					dic.getMomaGui().startOptimizationWhenReloadingPreviousCuration(); /* if we are reloading data, we want to directly optimize to see the previous results in the GUI */
				});
			}

			Thread t = new Thread(() -> {
				synchronized(lock) {
					while (guiFrame.isVisible())
						try {
							lock.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
				}
			});
			t.start();

			guiFrame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent arg0) {
					synchronized (lock) {
						guiFrame.setVisible(false);
						lock.notify();
					}
				}
			});

			try {
				t.join();
				System.exit(0);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		} else {
			configurationManager.saveParams(dic.getGuiFrame());
			if (commandLineArgumentParser.isTrackOnly()) {
				dic.getMomaGui().exportTrackingData();
			} else {
				dic.getMomaGui().exportHtmlOverview();
				dic.getMomaGui().exportDataFiles();
			}

			if (!runningAsFijiPlugin) {
				System.exit(0);
			}
		}
	}

	private static Object lock = new Object();

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
			final String message = "Should MoMA be opened with the data found in:\n" + datapath + "\n\nIn case you want to choose a folder please select 'No'...";
			final String title = "MoMA Data Folder Selection";
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
}
