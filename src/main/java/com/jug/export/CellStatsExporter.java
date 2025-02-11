package com.jug.export;

import com.jug.Growthlane;
import com.jug.GrowthlaneFrame;
import com.jug.MoMA;
import com.jug.config.ConfigurationManager;
import com.jug.datahandling.IGlExportFilePathGetter;
import com.jug.datahandling.IImageProvider;
import com.jug.export.measurements.SegmentMeasurementData;
import com.jug.export.measurements.SegmentMeasurementInterface;
import com.jug.gui.IDialogManager;
import com.jug.gui.MoMAGui;
import com.jug.gui.progress.DialogProgress;
import com.jug.lp.GrowthlaneTrackingILP;
import com.jug.lp.Hypothesis;
import com.jug.util.ComponentTreeUtils;
import com.jug.util.Util;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.ComponentInterface;
import com.jug.util.componenttree.ComponentProperties;
import gurobi.GRBException;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import javax.swing.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author jug
 */
public class CellStatsExporter implements ResultExporterInterface {

    private final IDialogManager dialogManager;
    private final MoMAGui gui;
    private final IImageProvider imageProvider;
    private String versionString;
    private List<SegmentMeasurementInterface> measurements;
    private ConfigurationManager configurationManager;
    private MixtureModelFit mixtureModelFit;
    private ComponentProperties componentProperties;
    private RegexParser positionStringParser;

    private RegexParser glStringParser;

    public CellStatsExporter(final IDialogManager dialogManager,
                             final MoMAGui gui,
                             final ConfigurationManager configurationManager,
                             MixtureModelFit mixtureModelFit,
                             ComponentProperties componentProperties,
                             IImageProvider imageProvider,
                             String versionString,
                             List<SegmentMeasurementInterface> measurements) {
        this.dialogManager = dialogManager;
        this.gui = gui;
        this.configurationManager = configurationManager;
        this.mixtureModelFit = mixtureModelFit;
        this.componentProperties = componentProperties;
        this.imageProvider = imageProvider;
        this.versionString = versionString;
        this.measurements = measurements;
        positionStringParser = new RegexParser(configurationManager.getPositionIdRegex());
        glStringParser = new RegexParser(configurationManager.getGrowthlaneIdRegex());
    }

    @Override
    public void export(Growthlane gl, IGlExportFilePathGetter exportFilePaths) {
        List<SegmentRecord> cellTrackStartingPoints = gl.getCellTrackStartingPoints();
        exportFilePaths.makeExportDataOutputDirectory();
        exportCellTracks(exportFilePaths.getCellTracksFilePath().toFile()); /* Export cell tracks */
        final GrowthlaneFrame firstGLF = gl.getFrames().get(0);
        long avgXpos = firstGLF.getAvgXpos();
        exportCellStats(exportFilePaths.getCellStatsFilePath().toFile(), cellTrackStartingPoints, avgXpos);
    }

    /**
     * @param file
     * @throws GRBException
     */
    private void exportCellStats(final File file, List<SegmentRecord> cellTrackStartingPoints, long avgXpos) {
        System.out.println("Exporting collected cell-statistics...");
        Writer out;
        try {
            out = new OutputStreamWriter(new FileOutputStream(file));
            writeCellStatsExportData(out, cellTrackStartingPoints, avgXpos);
        } catch (final FileNotFoundException e1) {
            JOptionPane.showMessageDialog(gui, "File not found!", "Error!", JOptionPane.ERROR_MESSAGE);
            e1.printStackTrace();
        } catch (final IOException e1) {
            JOptionPane.showMessageDialog(gui, "Selected file could not be written!", "Error!", JOptionPane.ERROR_MESSAGE);
            e1.printStackTrace();
        }
        System.out.println("...done!");
    }

    private void writeCellStatsExportData(Writer writer, List<SegmentRecord> cellTrackStartingPoints, long avgXpos) throws IOException {
        Locale.setDefault(new Locale("en", "US")); /* use US-style number formats! (e.g. '.' as decimal point) */

        // INITIALIZE PROGRESS-BAR if not run headless
        final DialogProgress dialogProgress = dialogManager.getNewProgressDialog(gui, "Exporting selected cell-statistics...", cellTrackStartingPoints.size());
        if (!configurationManager.getIfRunningHeadless()) {
            dialogProgress.setVisible(true);
        }

        ResultTable resultTable = new ResultTable(",");
        ResultTableColumn<String> laneIdCol = resultTable.addColumn(new ResultTableColumn<>("lane_ID"));
        ResultTableColumn<Integer> cellIdCol = resultTable.addColumn(new ResultTableColumn<>("cell_ID"));
        ResultTableColumn<Integer> frameCol = resultTable.addColumn(new ResultTableColumn<>("frame"));
        ResultTableColumn<Integer> cellRankCol = resultTable.addColumn(new ResultTableColumn<>("cell_rank"));
        ResultTableColumn<String> genealogyCol = resultTable.addColumn(new ResultTableColumn<>("genealogy"));
        ResultTableColumn<String> typeOfEndCol = resultTable.addColumn(new ResultTableColumn<>("type_of_end"));
        ResultTableColumn<Integer> parentIdCol = resultTable.addColumn(new ResultTableColumn<>("parent_ID"));
        ResultTableColumn<Integer> numberOfCellsInLaneCol = resultTable.addColumn(new ResultTableColumn<>("cells_in_lane"));
        ResultTableColumn<Integer> boundingBoxTopCol = resultTable.addColumn(new ResultTableColumn<>("bbox_top_px"));
        ResultTableColumn<Integer> boundingBoxBottomCol = resultTable.addColumn(new ResultTableColumn<>("bbox_bottom_px"));
        ResultTableColumn<Integer> touchesCellDetectionRoiTopCol = resultTable.addColumn(new ResultTableColumn<>("touches_detection_roi_top"));
        ResultTableColumn<Double> cellCenterXCol = resultTable.addColumn(new ResultTableColumn<>("center_x_px", "%.5f"));
        ResultTableColumn<Double> cellCenterYCol = resultTable.addColumn(new ResultTableColumn<>("center_y_px", "%.5f"));
        ResultTableColumn<Double> cellWidthCol = resultTable.addColumn(new ResultTableColumn<>("width_px", "%.5f"));
        ResultTableColumn<Double> cellLengthCol = resultTable.addColumn(new ResultTableColumn<>("length_px", "%.5f"));
        ResultTableColumn<Double> cellTiltAngleCol = resultTable.addColumn(new ResultTableColumn<>("tilt_rad", "%.5f"));
        ResultTableColumn<Integer> cellAreaCol = resultTable.addColumn(new ResultTableColumn<>("area_px"));
        ResultTableColumn<Integer> backgroundRoiAreaTotalCol = resultTable.addColumn(new ResultTableColumn<>("bgmask_area_px"));
        ResultTableColumn<Double> phaseContrastTotalIntensity = resultTable.addColumn(new ResultTableColumn<>("phc_total_intensity_au", "%.5f"));
        ResultTableColumn<Double> phaseContrastCoefficientOfVariation = resultTable.addColumn(new ResultTableColumn<>("phc_intensity_coefficient_of_variation", "%.5f"));

        HashMap<String, ResultTableColumn<Integer>> labelColumns = new HashMap<>();
        for (String label : configurationManager.CELL_LABEL_LIST) {
            labelColumns.put(label, resultTable.addColumn(new ResultTableColumn<>("label:" + label)));
        }

        List<ResultTableColumn> cellMaskTotalIntensityCols = new ArrayList<>();
        List<ResultTableColumn> backgroundMaskTotalIntensityCols = new ArrayList<>();
        List<ResultTableColumn> intensityFitCellIntensityCols = new ArrayList<>();
        List<ResultTableColumn> intensityFitBackgroundIntensityCols = new ArrayList<>();

        /* Add columns for per fluorescence-channel output. */
        for (int c = 1; c < imageProvider.getRawChannelImgs().size(); c++) {
            cellMaskTotalIntensityCols.add(resultTable.addColumn(new ResultTableColumn<>(String.format("fluo_cellmask_ch_%d", c), "%.5f")));
            backgroundMaskTotalIntensityCols.add(resultTable.addColumn(new ResultTableColumn<>(String.format("fluo_bgmask_ch_%d", c), "%.5f")));
            intensityFitCellIntensityCols.add(resultTable.addColumn(new ResultTableColumn<>(String.format("fluo_ampl_ch_%d", c), "%.5f")));
            intensityFitBackgroundIntensityCols.add(resultTable.addColumn(new ResultTableColumn<>(String.format("fluo_bg_ch_%d", c), "%.5f")));
        }

        measurements.forEach((measurement) -> measurement.setOutputTable(resultTable));

        String filename = Paths.get(configurationManager.getInputImagePath()).getFileName().toString();
        positionStringParser.parse(filename);
        glStringParser.parse(filename);
        String laneID = positionStringParser.getMatch() + "_" + glStringParser.getMatch();

        writer.write(String.format("# moma_version=\"%s\"\n", versionString));
        writer.write(String.format("# input_image=\"%s\"\n", configurationManager.getInputImagePath()));
        writer.write(String.format("# segmentation_model=\"%s\"\n", configurationManager.SEGMENTATION_MODEL_PATH));
        writer.write("# \n");

        for (SegmentRecord segmentRecord : cellTrackStartingPoints) {
            do {
                int timeStep = segmentRecord.timestep;

                AdvancedComponent<?> currentComponent = segmentRecord.hyp.getWrappedComponent();
                ValuePair<Integer, Integer> limits =
                        ComponentTreeUtils.getTreeNodeInterval(currentComponent);

                final GrowthlaneFrame glf = gui.model.getCurrentGL().getFrames().get(timeStep);

                final int numCells = glf.getSolutionStats_numberOfTrackedCells();
                final int cellRank = glf.getSolutionStats_cellRank(segmentRecord.hyp);

                laneIdCol.addValue(laneID);
                cellIdCol.addValue(segmentRecord.getId());
                parentIdCol.addValue(segmentRecord.getParentId());
                genealogyCol.addValue(segmentRecord.getGenealogyString());
                frameCol.addValue(timeStep);

                ValuePair<Double, Double> minorAndMajorAxis = componentProperties.getMinorMajorAxis(currentComponent);

                // WARNING -- if you change substring 'frame' you need also to change the last-row-deletion procedure below for the ENDOFTRACKING case... yes, this is not clean... ;)
                cellRankCol.addValue(cellRank);
                numberOfCellsInLaneCol.addValue(numCells);
                Integer cellBboxTop = limits.getA();
                boundingBoxTopCol.addValue(cellBboxTop);
                boundingBoxBottomCol.addValue(limits.getB());

                if (cellBboxTop <= configurationManager.CELL_DETECTION_ROI_OFFSET_TOP){
                    touchesCellDetectionRoiTopCol.addValue(1);
                }
                else{
                    touchesCellDetectionRoiTopCol.addValue(0);
                }

                ValuePair<Double, Double> center = componentProperties.getCentroid(currentComponent);
                cellCenterXCol.addValue(center.getA());
                cellCenterYCol.addValue(center.getB());
                cellWidthCol.addValue(minorAndMajorAxis.getA());
                cellLengthCol.addValue(minorAndMajorAxis.getB());
                cellTiltAngleCol.addValue(componentProperties.getTiltAngle(currentComponent));
                cellAreaCol.addValue(componentProperties.getArea(currentComponent));
                backgroundRoiAreaTotalCol.addValue(componentProperties.getBackgroundArea(currentComponent, imageProvider.getRawChannelImgs().get(0)));

                Img<FloatType> phaseContrastImage = imageProvider.getColorChannelAtTime(0, timeStep);
                phaseContrastTotalIntensity.addValue(componentProperties.getIntensityTotal(currentComponent, phaseContrastImage));
                phaseContrastCoefficientOfVariation.addValue(componentProperties.getIntensityCoefficientOfVariation(currentComponent, phaseContrastImage));

                for (String label : configurationManager.CELL_LABEL_LIST) {
                    if (segmentRecord.hyp.labels.contains(label)){
                        labelColumns.get(label).addValue(1);
                    }
                    else{
                        labelColumns.get(label).addValue(0);
                    }
                }

                /* add total cell fluorescence intensity to respective columns */
                int columnIndex = 0;
                for (int c = 1; c < imageProvider.getRawChannelImgs().size(); c++) {
                    final IntervalView<FloatType> channelFrame = Views.hyperSlice(imageProvider.getRawChannelImgs().get(c), 2, timeStep);
                    cellMaskTotalIntensityCols.get(columnIndex).addValue(componentProperties.getIntensityTotal(currentComponent, channelFrame));
                    backgroundMaskTotalIntensityCols.get(columnIndex).addValue(componentProperties.getBackgroundIntensityTotal(currentComponent, channelFrame));

                    final IntervalView<FloatType> columnBoxInChannel = Util.getColumnBoxInImg(channelFrame, segmentRecord.hyp, avgXpos, configurationManager.INTENSITY_FIT_RANGE_IN_PIXELS, configurationManager.GL_WIDTH_IN_PIXELS);
                    double[] estimates = mixtureModelFit.performMeasurement(segmentRecord, columnBoxInChannel, channelFrame.max(0));
                    intensityFitCellIntensityCols.get(columnIndex).addValue(estimates[0]); /* estimates: {cMax - cMin, cMin, muStart, wStart}; // parameters corresponding to Kaiser paper (step 2): {A, B, i_mid, w} */
                    intensityFitBackgroundIntensityCols.get(columnIndex).addValue(estimates[1]); /* estimates: {cMax - cMin, cMin, muStart, wStart}; // parameters corresponding to Kaiser paper (step 2): {A, B, i_mid, w} */

                    columnIndex++;
                }

                SegmentRecord finalSegmentRecord = segmentRecord;
                GrowthlaneTrackingILP ilp = MoMA.getGui().model.getCurrentGL().getIlp();
                List<Hypothesis<AdvancedComponent<FloatType>>> optimalSegments = ilp.getOptimalSegmentation(timeStep);
                List<ComponentInterface> optimalComponents = optimalSegments.stream().map(Hypothesis::getWrappedComponent).collect(Collectors.toList());
                SegmentMeasurementData data = new SegmentMeasurementData(finalSegmentRecord.hyp.getWrappedComponent(), optimalComponents, imageProvider, timeStep);
                measurements.forEach((measurement) -> measurement.measure(data));

                segmentRecord = segmentRecord.nextSegmentInTime();
//                System.out.println("segmentRecord.getId(): " + segmentRecord.getId());
//                System.out.println("segmentRecord.exists(): " + segmentRecord.exists());
                typeOfEndCol.addValue(segmentRecord.getTerminationIdentifier());
            }
            while (segmentRecord.exists());

            // REPORT PROGRESS if need be
            if (!configurationManager.getIfRunningHeadless()) {
                dialogProgress.hasProgressed();
            }
        }

        // Dispose ProgressBar in need be
        if (!configurationManager.getIfRunningHeadless()) {
            dialogProgress.setVisible(false);
            dialogProgress.dispose();
        }

        resultTable.writeTable(writer);
    }

    private void exportCellTracks(final File file) {

        final Vector<Vector<String>> dataToExport = getTracksExportData();

        System.out.println("Exporting data...");
        Writer out;
        try {
            out = new OutputStreamWriter(new FileOutputStream(file));

            out.write(String.format("# moma_version=\"%s\"\n", versionString));
            out.write(String.format("# input_image=\"%s\"\n", configurationManager.getInputImagePath()));
            out.write(String.format("# segmentation_model=\"%s\"\n", configurationManager.SEGMENTATION_MODEL_PATH));
            out.write("# \n");

            for (final Vector<String> rowInData : dataToExport) {
                for (final String datum : rowInData) {
                    out.write(datum + ",\t ");
                }
                out.write("\n");
            }
            out.close();
        } catch (final FileNotFoundException e1) {
            if (!configurationManager.getIfRunningHeadless())
                JOptionPane.showMessageDialog(gui, "File not found!", "Error!", JOptionPane.ERROR_MESSAGE);
            System.err.println("Export Error: File not found!");
            e1.printStackTrace();
        } catch (final IOException e1) {
            if (!configurationManager.getIfRunningHeadless())
                JOptionPane.showMessageDialog(gui, "Selected file could not be written!", "Error!", JOptionPane.ERROR_MESSAGE);
            System.err.println("Export Error: Selected file could not be written!");
            e1.printStackTrace();
        }
        System.out.println("...done!");
    }

    private Vector<Vector<String>> getTracksExportData() {

        // use US-style number formats! (e.g. '.' as decimal point)
        Locale.setDefault(new Locale("en", "US"));

        final int numCurrGL = gui.sliderGL.getValue();
        final int numGLFs = gui.model.getCurrentGL().getFrames().size();
        final Vector<Vector<String>> dataToExport = new Vector<>();

//        final Vector<String> firstLine = new Vector<>();
//        firstLine.add(loadedDataFolder);
//        dataToExport.add(firstLine);
        final Vector<String> secondLine = new Vector<>();
        secondLine.add("" + numCurrGL);
        secondLine.add("" + numGLFs);
        dataToExport.add(secondLine);

        int i = 0;
        for (final GrowthlaneFrame glf : gui.model.getCurrentGL().getFrames()) {
            final Vector<String> newRow = new Vector<>();
            newRow.add("" + i);

            final int numCells = glf.getSolutionStats_numberOfTrackedCells();
            final Vector<ValuePair<ValuePair<Integer, Integer>, ValuePair<Integer, Integer>>> data = glf.getSolutionStats_limitsAndRightAssType();

            newRow.add("" + numCells);
            for (final ValuePair<ValuePair<Integer, Integer>, ValuePair<Integer, Integer>> elem : data) {
                final int min = elem.a.a;
                final int max = elem.a.b;
                final int type = elem.b.a;
                final int user_touched = elem.b.b;
                newRow.add(String.format("%3d, %3d, %3d, %3d", min, max, type, user_touched));
            }

            dataToExport.add(newRow);
            i++;
        }

        return dataToExport;
    }

}
