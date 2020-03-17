package com.jug.export;

import com.jug.GrowthLineFrame;
import com.jug.MoMA;
import com.jug.gui.DialogCellStatsExportSetup;
import com.jug.gui.MoMAGui;
import com.jug.gui.OsDependentFileChooser;
import com.jug.gui.progress.DialogProgress;
import com.jug.lp.*;
import com.jug.util.ComponentTreeUtils;
import com.jug.util.Util;
import com.jug.util.componenttree.ComponentProperties;
import com.jug.util.componenttree.SimpleComponent;
import gurobi.GRBException;
import net.imglib2.IterableInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import scala.Int;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jug.MoMA.INTENSITY_FIT_RANGE_IN_PIXELS;

/**
 * @author jug
 */
public class CellStatsExporter {

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private ComponentProperties componentProperties = new ComponentProperties();

	private final MoMAGui gui;

	public CellStatsExporter( final MoMAGui gui ) {
		this.gui = gui;
	}

	private boolean showConfigDialog() {
		final DialogCellStatsExportSetup dialog =
				new DialogCellStatsExportSetup( gui, MoMA.EXPORT_USER_INPUTS, MoMA.EXPORT_DO_TRACK_EXPORT, MoMA.EXPORT_INCLUDE_HISTOGRAMS, MoMA.EXPORT_INCLUDE_QUANTILES, MoMA.EXPORT_INCLUDE_COL_INTENSITY_SUMS, MoMA.EXPORT_INCLUDE_PIXEL_INTENSITIES);
		dialog.ask();
		if ( !dialog.wasCanceled() ) {
			MoMA.EXPORT_DO_TRACK_EXPORT = dialog.doExportTracks;
			MoMA.EXPORT_USER_INPUTS = dialog.doExportUserInputs;
			MoMA.EXPORT_INCLUDE_HISTOGRAMS = dialog.includeHistograms;
			MoMA.EXPORT_INCLUDE_QUANTILES = dialog.includeQuantiles;
			MoMA.EXPORT_INCLUDE_COL_INTENSITY_SUMS = dialog.includeColIntensitySums;
			MoMA.EXPORT_INCLUDE_PIXEL_INTENSITIES = dialog.includePixelIntensities;
			return true;
		} else {
			return false;
		}
	}

	private boolean showFitRangeWarningDialogIfNeeded(){
		final IntervalView<FloatType> channelFrame = Views.hyperSlice(MoMA.instance.getRawChannelImgs().get(0), 2, 0);

		if (channelFrame.dimension(0) >= INTENSITY_FIT_RANGE_IN_PIXELS) return true; /* Image wider then fit range. No need to warn. */

		int userSelection = JOptionPane.showConfirmDialog(null,
				String.format("Intensity fit range (%dpx) exceeds image width (%dpx). Image width will be use instead. Do you want to proceed?", INTENSITY_FIT_RANGE_IN_PIXELS, channelFrame.dimension(0)),
				"Fit Range Warning",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (userSelection == JOptionPane.YES_OPTION) return true;
		else return false;
	}

	public void export() {
		if ( !MoMA.HEADLESS ) {
			if ( showConfigDialog() ) {
				if (!showFitRangeWarningDialogIfNeeded()) return;

				final File folderToUse = OsDependentFileChooser.showSaveFolderChooser( gui, MoMA.STATS_OUTPUT_PATH, "Choose export folder..." );
				if ( folderToUse == null ) {
					JOptionPane.showMessageDialog(
							gui,
							"Illegal save location choosen!",
							"Error",
							JOptionPane.ERROR_MESSAGE );
					return;
				}
				if ( MoMA.EXPORT_DO_TRACK_EXPORT) {
					exportTracks( new File( folderToUse, "ExportedTracks_" + MoMA.getDefaultFilenameDecoration() + ".csv" ) );
				}
				if ( MoMA.EXPORT_USER_INPUTS) {
					final int tmin = MoMA.getMinTime();
					final int tmax = MoMA.getMaxTime();
					final File file =
							new File( folderToUse, String.format(
									"[%d-%d]_%s.moma",
									tmin,
									tmax,
									MoMA.getDefaultFilenameDecoration() ) );
					MoMA.getGui().model.getCurrentGL().getIlp().saveState( file );
				}
				try {
					exportCellStats( new File( folderToUse, "ExportedCellStats_" + MoMA.getDefaultFilenameDecoration() + ".csv" ) );
				} catch ( final GRBException e ) {
					e.printStackTrace();
				}
				// always export mmproperties
				MoMA.instance.saveParams(new File( folderToUse, "mm.properties" ));
			}
		} else {
			if ( MoMA.EXPORT_DO_TRACK_EXPORT) {
				exportTracks( new File( MoMA.STATS_OUTPUT_PATH, "ExportedTracks_" + MoMA.getDefaultFilenameDecoration() + ".csv" ) );
			}
			if ( MoMA.EXPORT_USER_INPUTS) {
				final int tmin = MoMA.getMinTime();
				final int tmax = MoMA.getMaxTime();
				final File file =
						new File( MoMA.STATS_OUTPUT_PATH, String.format(
								"--[%d-%d]_%s.timm",
								tmin,
								tmax,
								MoMA.getDefaultFilenameDecoration() ) );
				MoMA.getGui().model.getCurrentGL().getIlp().saveState( file );
			}

			try {
				exportCellStats( new File( MoMA.STATS_OUTPUT_PATH, "ExportedCellStats_" + MoMA.getDefaultFilenameDecoration() + ".csv" ) );
			} catch ( final GRBException e ) {
				e.printStackTrace();
			}
			// always export mmproperties
			MoMA.instance.saveParams(new File( MoMA.STATS_OUTPUT_PATH, "mm.properties" ));
		}
	}

	/**
	 * @param file
	 * @throws GRBException
	 */
    private void exportCellStats(final File file) throws GRBException {

		// ------- THE MAGIC *** THE MAGIC *** THE MAGIC *** THE MAGIG -------
		final Vector< String > linesToExport = getCellStatsExportData();
		// -------------------------------------------------------------------

		System.out.println( "Exporting collected cell-statistics..." );
		Writer out;
		try {
			out = new OutputStreamWriter( new FileOutputStream( file ) );

			for ( final String line : linesToExport ) {
				out.write( line );
				out.write( "\n" );
			}
			out.close();
		} catch ( final FileNotFoundException e1 ) {
			JOptionPane.showMessageDialog( gui, "File not found!", "Error!", JOptionPane.ERROR_MESSAGE );
			e1.printStackTrace();
		} catch ( final IOException e1 ) {
			JOptionPane.showMessageDialog( gui, "Selected file could not be written!", "Error!", JOptionPane.ERROR_MESSAGE );
			e1.printStackTrace();
		}
		System.out.println( "...done!" );
	}

	private MixtureModelFit mixtureModelFit = new MixtureModelFit();


	private Vector< String > getCellStatsExportData() throws GRBException {
		// use US-style number formats! (e.g. '.' as decimal point)
		Locale.setDefault( new Locale( "en", "US" ) );

		final String loadedDataFolder = MoMA.props.getProperty( "import_path", "BUG -- could not get property 'import_path' while exporting cell statistics..." );
		final int numCurrGL = gui.sliderGL.getValue();
		final Vector< String > linesToExport = new Vector<>();

		final GrowthLineFrame firstGLF = gui.model.getCurrentGL().getFrames().get( 0 );
		final GrowthLineTrackingILP ilp = firstGLF.getParent().getIlp();

		CellTrackBuilder trackBuilder = new CellTrackBuilder();
		trackBuilder.buildSegmentTracks(firstGLF.getSortedActiveHypsAndPos(),
										firstGLF.getParent().getIlp(),
										gui.sliderTime.getMaximum());
		List<SegmentRecord> startingPoints = trackBuilder.getStartingPoints();


		// INITIALIZE PROGRESS-BAR if not run headless
		final DialogProgress dialogProgress = new DialogProgress( gui, "Exporting selected cell-statistics...", startingPoints.size() );
		if ( !MoMA.HEADLESS ) {
			dialogProgress.setVisible( true );
		}

		ResultTable resultTable = new ResultTable();
		ResultTableColumn<String> laneIdCol = resultTable.addColumn(new ResultTableColumn<>("lane_ID"));
		ResultTableColumn<Integer> cellIdCol = resultTable.addColumn(new ResultTableColumn<>("cell_ID"));
		ResultTableColumn<Integer> parentIdCol = resultTable.addColumn(new ResultTableColumn<>("parent_ID"));
		ResultTableColumn<String> genealogyCol = resultTable.addColumn(new ResultTableColumn<>("genealogy"));
		ResultTableColumn<String> typeOfEndCol = resultTable.addColumn(new ResultTableColumn<>("type_of_end"));
		ResultTableColumn<Integer> frameCol = resultTable.addColumn(new ResultTableColumn<>("frame"));
		ResultTableColumn<Integer> cellRankCol = resultTable.addColumn(new ResultTableColumn<>("cell_rank"));
		ResultTableColumn<Integer> numberOfCellsInLaneCol = resultTable.addColumn(new ResultTableColumn<>("total_cell_in_lane"));
		ResultTableColumn<Integer> boundingBoxTopCol = resultTable.addColumn(new ResultTableColumn<>("bounding_box_top [px]"));
		ResultTableColumn<Integer> boundingBoxBottomCol = resultTable.addColumn(new ResultTableColumn<>("bounding_box_bottom [px]"));
		ResultTableColumn<Double> cellCenterXCol = resultTable.addColumn(new ResultTableColumn<>("cell_center_x [px]"));
		ResultTableColumn<Double> cellCenterYCol = resultTable.addColumn(new ResultTableColumn<>("cell_center_y [px]"));
		ResultTableColumn<Double> cellWidthCol = resultTable.addColumn(new ResultTableColumn<>("cell_width [px]"));
		ResultTableColumn<Double> cellLengthCol = resultTable.addColumn(new ResultTableColumn<>("cell_length [px]"));
		ResultTableColumn<Double> cellTiltAngleCol = resultTable.addColumn(new ResultTableColumn<>("tilt_angle [rad]"));
		ResultTableColumn<Integer> cellAreaCol = resultTable.addColumn(new ResultTableColumn<>("cell_area [px^2]"));
		ResultTableColumn<Integer> backgroundRoiAreaTotalCol = resultTable.addColumn(new ResultTableColumn<>("background_roi_area_total [px^2]"));

		Pattern positionPattern = Pattern.compile("Pos(\\d+)");
		Matcher positionMatcher = positionPattern.matcher(loadedDataFolder);
		positionMatcher.find();
		String positionNumber = positionMatcher.group(1); // group(0) is the whole match; group(1) is just the number, which is what we want

		Pattern growthlanePattern = Pattern.compile("GL(\\d+)");
		Matcher growthlaneMatcher = growthlanePattern.matcher(loadedDataFolder);
		growthlaneMatcher.find();
		String growthlaneNumber = growthlaneMatcher.group(1); // group(0) is the whole match; group(1) is just the number, which is what we want

		String laneID = "pos_" + positionNumber + "_GL_" + growthlaneNumber;

		// Line 1: import folder
		linesToExport.add( loadedDataFolder );



		// Line 2: GL-id
		linesToExport.add( "GLidx = " + numCurrGL );

		// Line 3: #cells
		linesToExport.add( "numCells = " + startingPoints.size() );

		// Line 4: #channels
		linesToExport.add( "numChannels = " + MoMA.instance.getRawChannelImgs().size() );

		// Line 5: imageHeight
		final long h = MoMA.instance.getImgRaw().dimension( 1 );
		linesToExport.add( "imageHeight = " + h + "\n" );

		// Line 7: track region (pixel row interval we perform tracking within -- this is all but top and bottom offset areas)
		linesToExport.add( String.format("trackRegionInterval = [%d]", h - 1 ) );

		// Export all cells (we found all their starting segments above)
		for (SegmentRecord segmentRecord : startingPoints) {
			linesToExport.add(segmentRecord.toString());
			do {
				SimpleComponent<?> currentComponent = (SimpleComponent<?>) segmentRecord.hyp.getWrappedComponent();
				ValuePair<Integer, Integer> limits =
						ComponentTreeUtils.getTreeNodeInterval(currentComponent);

				final GrowthLineFrame glf = gui.model.getCurrentGL().getFrames().get(segmentRecord.frame);

				final int numCells = glf.getSolutionStats_numberOfTrackedCells();
				final int cellRank = glf.getSolutionStats_cellRank(segmentRecord.hyp);

				laneIdCol.addValue(laneID);
				cellIdCol.addValue(segmentRecord.getId());
				parentIdCol.addValue(segmentRecord.getParentId());
				genealogyCol.addValue(segmentRecord.getGenealogyString());
				typeOfEndCol.addValue(segmentRecord.getTerminationIdentifier());
				frameCol.addValue(segmentRecord.frame);

				ValuePair<Double, Double> minorAndMajorAxis = componentProperties.getMinorMajorAxis(currentComponent);
				
				// WARNING -- if you change substring 'frame' you need also to change the last-row-deletion procedure below for the ENDOFTRACKING case... yes, this is not clean... ;)
				String outputString = "\t";
				cellRankCol.addValue(cellRank);
				numberOfCellsInLaneCol.addValue(numCells);
				boundingBoxTopCol.addValue(limits.getA());
				boundingBoxBottomCol.addValue(limits.getB());
				ValuePair<Double, Double> center = componentProperties.getCentroid(currentComponent);
				cellCenterXCol.addValue(center.getA());
				cellCenterYCol.addValue(center.getB());
				cellWidthCol.addValue(minorAndMajorAxis.getA());
				cellLengthCol.addValue(minorAndMajorAxis.getB());
				cellTiltAngleCol.addValue(componentProperties.getTiltAngle(currentComponent));
				cellAreaCol.addValue(componentProperties.getArea(currentComponent));
				backgroundRoiAreaTotalCol.addValue(componentProperties.getBackgroundArea(currentComponent, MoMA.instance.getRawChannelImgs().get(0)));

				/* start outputting total cell intensities */
				outputString += String.format("cell_intensity_total=[");
				for (int c = 0; c < MoMA.instance.getRawChannelImgs().size(); c++) {
					final IntervalView<FloatType> channelFrame = Views.hyperSlice(MoMA.instance.getRawChannelImgs().get(c), 2, segmentRecord.frame);
					outputString += String.format("%f", componentProperties.getTotalIntensity(currentComponent, channelFrame));
					if(c < MoMA.instance.getRawChannelImgs().size() - 1){
						outputString += ", ";
					}
				}
				outputString += String.format("]; ");
				/* start outputting total background intensities */
				outputString += String.format("background_intensity_total=[");
				for (int c = 0; c < MoMA.instance.getRawChannelImgs().size(); c++) {
					final IntervalView<FloatType> channelFrame = Views.hyperSlice(MoMA.instance.getRawChannelImgs().get(c), 2, segmentRecord.frame);
					outputString += String.format("%f", componentProperties.getTotalBackgroundIntensity(currentComponent, channelFrame));
					if(c < MoMA.instance.getRawChannelImgs().size()-1){
						outputString += ", ";
					}
				}
				outputString += String.format("]; ");

				// export info per image channel
				for (int c = 0; c < MoMA.instance.getRawChannelImgs().size(); c++) {
					final IntervalView<FloatType> channelFrame = Views.hyperSlice(MoMA.instance.getRawChannelImgs().get(c), 2, segmentRecord.frame);
					final IterableInterval<FloatType> segmentBoxInChannel = Util.getSegmentBoxInImg(channelFrame, segmentRecord.hyp, firstGLF.getAvgXpos());

					final FloatType min = new FloatType();
					final FloatType max = new FloatType();
					Util.computeMinMax(segmentBoxInChannel, min, max);

					if (MoMA.EXPORT_INCLUDE_HISTOGRAMS) {
						final long[] hist = segmentRecord.computeChannelHistogram(segmentBoxInChannel, min.get(), max.get());
						StringBuilder histStr = new StringBuilder(String.format("\t\tch=%d; output=HISTOGRAM", c));
						histStr.append(String.format("; min=%8.3f; max=%8.3f", min.get(), max.get()));
						for (final long value : hist) {
							histStr.append(String.format("; %5d", value));
						}
						linesToExport.add(histStr.toString());
					}

					if (MoMA.EXPORT_INCLUDE_QUANTILES) {
						final float[] percentile = segmentRecord.computeChannelPercentile(segmentBoxInChannel);
						StringBuilder percentileStr = new StringBuilder(String.format("\t\tch=%d; output=PERCENTILES", c));
						percentileStr.append(String.format("; min=%8.3f; max=%8.3f", min.get(), max.get()));
						for (final float value : percentile) {
							percentileStr.append(String.format("; %8.3f", value));
						}
						linesToExport.add(percentileStr.toString());
					}

					if (MoMA.EXPORT_INCLUDE_COL_INTENSITY_SUMS) {
						final IntervalView<FloatType> columnBoxInChannel = Util.getColumnBoxInImg(channelFrame, segmentRecord.hyp, firstGLF.getAvgXpos());
						final float[] column_intensities = segmentRecord.computeChannelColumnIntensities(columnBoxInChannel);
						StringBuilder colIntensityStr = new StringBuilder(String.format("\t\tch=%d; output=COLUMN_INTENSITIES", c));
						for (final float value : column_intensities) {
							colIntensityStr.append(String.format("; %.3f", value));
						}
						linesToExport.add(colIntensityStr.toString());
					}

					if (MoMA.EXPORT_INCLUDE_INTENSITY_FIT) {
						if (c > 0) { /* Do not fit the phase contrast channel, which is channel 0. */
							final IntervalView<FloatType> columnBoxInChannel = Util.getColumnBoxInImg(channelFrame, segmentRecord.hyp, firstGLF.getAvgXpos());
							double[] estimates = mixtureModelFit.performMeasurement(segmentRecord, columnBoxInChannel, channelFrame.max(0));
							StringBuilder mixtureModelOutputStr = new StringBuilder(String.format("\t\tch=%d; output=INTENSITY_FIT=", c));
							for (final double value : estimates) {
								mixtureModelOutputStr.append(String.format("%.3f; ", value));
							}
							linesToExport.add(mixtureModelOutputStr.toString());
						}
					}

					if (MoMA.EXPORT_INCLUDE_PIXEL_INTENSITIES) {
						final IntervalView<FloatType> intensityBoxInChannel = Util.getIntensityBoxInImg(channelFrame, segmentRecord.hyp, firstGLF.getAvgXpos());
						final float[][] intensities = segmentRecord.getIntensities(intensityBoxInChannel);
						StringBuilder intensityStr = new StringBuilder(String.format("\t\tch=%d; output=PIXEL_INTENSITIES", c));
						for (int y = 0; y < intensities[0].length; y++) {
							for (float[] intensity : intensities) {
								intensityStr.append(String.format(";%.3f", intensity[y]));
							}
							intensityStr.append(" ");
						}
						linesToExport.add(intensityStr.toString());
					}
				}
				segmentRecord = segmentRecord.nextSegmentInTime(ilp);
			}
			while (segmentRecord.exists());

			// REPORT PROGRESS if needbe
			if (!MoMA.HEADLESS) {
				dialogProgress.hasProgressed();
			}
		}

		// Dispose ProgressBar in needbe
		if ( !MoMA.HEADLESS ) {
			dialogProgress.setVisible( false );
			dialogProgress.dispose();
		}

		return linesToExport;
	}

	private void exportTracks(final File file) {

		final Vector< Vector< String >> dataToExport = getTracksExportData();

		System.out.println( "Exporting data..." );
		Writer out;
		try {
			out = new OutputStreamWriter( new FileOutputStream( file ) );

			for ( final Vector< String > rowInData : dataToExport ) {
				for ( final String datum : rowInData ) {
					out.write( datum + ",\t " );
				}
				out.write( "\n" );
			}
			out.close();
		} catch ( final FileNotFoundException e1 ) {
			if ( !MoMA.HEADLESS )
				JOptionPane.showMessageDialog( gui, "File not found!", "Error!", JOptionPane.ERROR_MESSAGE );
			System.err.println( "Export Error: File not found!" );
			e1.printStackTrace();
		} catch ( final IOException e1 ) {
			if ( !MoMA.HEADLESS )
				JOptionPane.showMessageDialog( gui, "Selected file could not be written!", "Error!", JOptionPane.ERROR_MESSAGE );
			System.err.println( "Export Error: Selected file could not be written!" );
			e1.printStackTrace();
		}
		System.out.println( "...done!" );
	}

	private Vector< Vector< String >> getTracksExportData() {

		// use US-style number formats! (e.g. '.' as decimal point)
		Locale.setDefault( new Locale( "en", "US" ) );

		final String loadedDataFolder = MoMA.props.getProperty( "import_path", "BUG -- could not get property 'import_path' while exporting tracks..." );
		final int numCurrGL = gui.sliderGL.getValue();
		final int numGLFs = gui.model.getCurrentGL().getFrames().size();
		final Vector< Vector< String >> dataToExport = new Vector<>();

		final Vector< String > firstLine = new Vector<>();
		firstLine.add( loadedDataFolder );
		dataToExport.add( firstLine );
		final Vector< String > secondLine = new Vector<>();
		secondLine.add( "" + numCurrGL );
		secondLine.add( "" + numGLFs );
		dataToExport.add( secondLine );

		int i = 0;
		for ( final GrowthLineFrame glf : gui.model.getCurrentGL().getFrames() ) {
			final Vector< String > newRow = new Vector<>();
			newRow.add( "" + i );

			final int numCells = glf.getSolutionStats_numberOfTrackedCells();
			final Vector< ValuePair< ValuePair< Integer, Integer >, ValuePair< Integer, Integer > >> data = glf.getSolutionStats_limitsAndRightAssType();

			newRow.add( "" + numCells );
			for ( final ValuePair< ValuePair< Integer, Integer >, ValuePair< Integer, Integer > > elem : data ) {
				final int min = elem.a.a;
				final int max = elem.a.b;
				final int type = elem.b.a;
				final int user_touched = elem.b.b;
				newRow.add( String.format( "%3d, %3d, %3d, %3d", min, max, type, user_touched ) );
			}

			dataToExport.add( newRow );
			i++;
		}

		return dataToExport;
	}

}
