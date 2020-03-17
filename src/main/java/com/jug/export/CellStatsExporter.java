package com.jug.export;

import com.jug.GrowthLineFrame;
import com.jug.MoMA;
import com.jug.gui.DialogCellStatsExportSetup;
import com.jug.gui.MoMAGui;
import com.jug.gui.OsDependentFileChooser;
import com.jug.gui.progress.DialogProgress;
import com.jug.lp.GrowthLineTrackingILP;
import com.jug.util.ComponentTreeUtils;
import com.jug.util.Util;
import com.jug.util.componenttree.ComponentProperties;
import com.jug.util.componenttree.SimpleComponent;
import gurobi.GRBException;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
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

		System.out.println( "Exporting collected cell-statistics..." );
		Writer out;
		try {
			out = new OutputStreamWriter( new FileOutputStream( file ) );

			final Vector< String > linesToExport = getCellStatsExportData(out);

//			for ( final String line : linesToExport ) {
//				out.write( line );
//				out.write( "\n" );
//			}
//			out.close();
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


	private Vector< String > getCellStatsExportData(Writer writer) throws GRBException, IOException {
		Locale.setDefault( new Locale( "en", "US" ) ); /* use US-style number formats! (e.g. '.' as decimal point) */

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
		ResultTableColumn<Integer> numberOfCellsInLaneCol = resultTable.addColumn(new ResultTableColumn<>("cells_in_lane"));
		ResultTableColumn<Integer> boundingBoxTopCol = resultTable.addColumn(new ResultTableColumn<>("bbox_top[px]"));
		ResultTableColumn<Integer> boundingBoxBottomCol = resultTable.addColumn(new ResultTableColumn<>("bbox_bottom[px]"));
		ResultTableColumn<Double> cellCenterXCol = resultTable.addColumn(new ResultTableColumn<>("center_x[px]"));
		ResultTableColumn<Double> cellCenterYCol = resultTable.addColumn(new ResultTableColumn<>("center_y[px]"));
		ResultTableColumn<Double> cellWidthCol = resultTable.addColumn(new ResultTableColumn<>("width[px]"));
		ResultTableColumn<Double> cellLengthCol = resultTable.addColumn(new ResultTableColumn<>("length[px]"));
		ResultTableColumn<Double> cellTiltAngleCol = resultTable.addColumn(new ResultTableColumn<>("tilt[rad]"));
		ResultTableColumn<Integer> cellAreaCol = resultTable.addColumn(new ResultTableColumn<>("area[px^2]"));
		ResultTableColumn<Integer> backgroundRoiAreaTotalCol = resultTable.addColumn(new ResultTableColumn<>("bkgr_area_total[px^2]"));

		List<ResultTableColumn> cellMaskTotalIntensityCols = new ArrayList<>();
		List<ResultTableColumn> backgroundMaskTotalIntensityCols = new ArrayList<>();
		List<ResultTableColumn> intensityFitCellIntensityCols = new ArrayList<>();
		List<ResultTableColumn> intensityFitBackgroundIntensityCols = new ArrayList<>();

		/* Add columns for per fluorescence-channel output. */
		for (int c = 1; c < MoMA.instance.getRawChannelImgs().size(); c++) {
			cellMaskTotalIntensityCols.add(resultTable.addColumn(new ResultTableColumn<>(String.format("fluo_cellmask_%d", c))));
			backgroundMaskTotalIntensityCols.add(resultTable.addColumn(new ResultTableColumn<>(String.format("fluo_bgmask_ch_%d", c))));
			intensityFitCellIntensityCols.add(resultTable.addColumn(new ResultTableColumn<>(String.format("fluo_ampl_ch_%d", c))));
			intensityFitBackgroundIntensityCols.add(resultTable.addColumn(new ResultTableColumn<>(String.format("fluo_bg_ch_%d", c))));
		}

		Pattern positionPattern = Pattern.compile("Pos(\\d+)");
		Matcher positionMatcher = positionPattern.matcher(loadedDataFolder);
		positionMatcher.find();
		String positionNumber = positionMatcher.group(1); // group(0) is the whole match; group(1) is just the number, which is what we want

		Pattern growthlanePattern = Pattern.compile("GL(\\d+)");
		Matcher growthlaneMatcher = growthlanePattern.matcher(loadedDataFolder);
		growthlaneMatcher.find();
		String growthlaneNumber = growthlaneMatcher.group(1); // group(0) is the whole match; group(1) is just the number, which is what we want

		String laneID = "pos_" + positionNumber + "_GL_" + growthlaneNumber;

		writer.write(String.format("image_folder=%s\n", loadedDataFolder));

		for (SegmentRecord segmentRecord : startingPoints) {
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
				frameCol.addValue(segmentRecord.frame);

				ValuePair<Double, Double> minorAndMajorAxis = componentProperties.getMinorMajorAxis(currentComponent);
				
				// WARNING -- if you change substring 'frame' you need also to change the last-row-deletion procedure below for the ENDOFTRACKING case... yes, this is not clean... ;)
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

				/* add total cell fluorescence intensity to respective columns */
				int columnIndex = 0;
				for (int c = 1; c < MoMA.instance.getRawChannelImgs().size(); c++) {
					final IntervalView<FloatType> channelFrame = Views.hyperSlice(MoMA.instance.getRawChannelImgs().get(c), 2, segmentRecord.frame);
					cellMaskTotalIntensityCols.get(columnIndex).addValue(componentProperties.getTotalIntensity(currentComponent, channelFrame));
					backgroundMaskTotalIntensityCols.get(columnIndex).addValue(componentProperties.getTotalBackgroundIntensity(currentComponent, channelFrame));

					final IntervalView<FloatType> columnBoxInChannel = Util.getColumnBoxInImg(channelFrame, segmentRecord.hyp, firstGLF.getAvgXpos());
					double[] estimates = mixtureModelFit.performMeasurement(segmentRecord, columnBoxInChannel, channelFrame.max(0));
					intensityFitCellIntensityCols.get(columnIndex).addValue(estimates[0]); /* estimates: {cMax - cMin, cMin, muStart, wStart}; // parameters corresponding to Kaiser paper (step 2): {A, B, i_mid, w} */
					intensityFitBackgroundIntensityCols.get(columnIndex).addValue(estimates[1]); /* estimates: {cMax - cMin, cMin, muStart, wStart}; // parameters corresponding to Kaiser paper (step 2): {A, B, i_mid, w} */

					columnIndex++;
				}

				segmentRecord = segmentRecord.nextSegmentInTime(ilp);

				typeOfEndCol.addValue(segmentRecord.getTerminationIdentifier());
			}
			while (segmentRecord.exists());

			// REPORT PROGRESS if need be
			if (!MoMA.HEADLESS) {
				dialogProgress.hasProgressed();
			}
		}

		// Dispose ProgressBar in need be
		if ( !MoMA.HEADLESS ) {
			dialogProgress.setVisible( false );
			dialogProgress.dispose();
		}

		writer.write("\n");
		resultTable.print(writer);

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
