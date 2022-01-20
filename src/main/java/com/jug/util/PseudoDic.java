package com.jug.util;

import com.jug.MoMA;
import com.jug.config.ConfigurationManager;
import com.jug.config.ITrackingConfiguration;
import com.jug.config.IUnetProcessingConfiguration;
import com.jug.datahandling.IImageProvider;
import com.jug.export.*;
import com.jug.export.measurements.OrientedBoundingBoxMeasurement;
import com.jug.export.measurements.SegmentMeasurementInterface;
import com.jug.export.measurements.SpineLengthMeasurement;
import com.jug.gui.DialogManager;
import com.jug.gui.IDialogManager;
import com.jug.gui.MoMAGui;
import com.jug.gui.MoMAModel;
import com.jug.lp.AssignmentPlausibilityTester;
import com.jug.util.componenttree.*;
import com.jug.util.imglib2.Imglib2Utils;
import com.jug.util.imglib2.OverlayUtils;
import com.jug.util.math.GeomUtils;
import com.jug.util.math.Vector2DPolyline;
import net.imagej.ops.OpService;
import org.scijava.Context;
import org.scijava.convert.ConvertService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * This is pseudo dependency injection container, which I use to work on getting my class dependencies and initialization
 * in order. Ideally at some point, this will be replaced with a true dependency injection frame-work.
 */
public class PseudoDic {
    public static Context context;
    public static OpService ops;
    private final AssignmentPlausibilityTester assignmentPlausibilityTester;
    private final ComponentProperties componentProperties;
    private final ConfigurationManager configurationManager;
    private final MoMA momaInstance;
    private final MixtureModelFit mixtureModelFit;
    private final ComponentTreeGenerator componentTreeGenerator;
    private final Imglib2Utils imglib2utils;
    private final GroundTruthFramesExporter groundTruthFramesExporter;
    private final RecursiveComponentWatershedder recursiveComponentWatershedder;
    private final UnetProcessor unetProcessor;
    private final WatershedMaskGenerator watershedMaskGenerator;
    private final GitVersionProvider gitVersionProvider;
    private SpineLengthMeasurement spineLengthMeasurement;
    private final ConvertService convertService;

    public PseudoDic(ConfigurationManager configurationManager, MoMA main) {
        this.configurationManager = configurationManager;
        this.momaInstance = main;
        context = new Context();
        convertService = context.service(ConvertService.class);
        ops = context.service(OpService.class);
        imglib2utils = new Imglib2Utils(getImageJOpService());
        recursiveComponentWatershedder = new RecursiveComponentWatershedder(getImageJOpService());
        componentProperties = new ComponentProperties(getImageJOpService(), imglib2utils);
        watershedMaskGenerator = new WatershedMaskGenerator(configurationManager.THRESHOLD_FOR_COMPONENT_MERGING, configurationManager.THRESHOLD_FOR_COMPONENT_GENERATION);
        componentTreeGenerator = new ComponentTreeGenerator(recursiveComponentWatershedder, componentProperties, watershedMaskGenerator, imglib2utils);
        assignmentPlausibilityTester = new AssignmentPlausibilityTester(configurationManager);
        mixtureModelFit = new MixtureModelFit(getConfigurationManager());
        groundTruthFramesExporter = new GroundTruthFramesExporter(() -> MoMA.getDefaultFilenameDecoration()); /* we pass a supplier here, because at this point in the instantiation MoMA.getDefaultFilenameDecoration() still Null; once instantiation is clean up, this should not be necessary anymore */
        unetProcessor = new UnetProcessor(getSciJavaContext(), getUnetProcessorConfiguration());
        unetProcessor.setModelFilePath(ConfigurationManager.SEGMENTATION_MODEL_PATH);
        gitVersionProvider = new GitVersionProvider();
    }

    public Context getSciJavaContext() { return context; }

    public OpService getImageJOpService() { return ops; }

    public AssignmentPlausibilityTester getAssignmentPlausibilityTester() {
        return assignmentPlausibilityTester;
    }

    public ComponentProperties getComponentProperties() {
        return componentProperties;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public MixtureModelFit getMixtureModelFit() {
        return mixtureModelFit;
    }

    public IImageProvider getImageProvider() {
        return momaInstance;
    }

    public MoMA getMomaInstance() {
        return momaInstance;
    }

    public ComponentTreeGenerator getComponentTreeGenerator() { return componentTreeGenerator; }

    public Imglib2Utils getImglib2utils() { return imglib2utils; }

    public CellStatsExporter getCellStatsExporter() {
        return new CellStatsExporter(getMomaGui(), getConfigurationManager(), getMixtureModelFit(), getComponentProperties(), getMomaInstance(), getGitVersionProvider().getVersionString(), getMeasurements());
    }

    private List<SegmentMeasurementInterface> getMeasurements() {
        List<SegmentMeasurementInterface> listOfMeasurements = new ArrayList<>();
        listOfMeasurements.add(new OrientedBoundingBoxMeasurement(context));
        if (ConfigurationManager.EXPORT_SPINE_MEASUREMENT) {
            listOfMeasurements.add(getSpineLengthMeasurement());
        }
        return listOfMeasurements;
    }

    private SpineLengthMeasurement getSpineLengthMeasurement() {
        if (spineLengthMeasurement != null) {
            return spineLengthMeasurement;
        }
        MedialLineCalculator medialLineCalculator = new MedialLineCalculator(getImageJOpService(), getImglib2utils());
        Function<Vector2DPolyline, Vector2DPolyline> medialLineProcessor =
                (input) -> GeomUtils.smoothWithAdaptiveWindowSize(input,
                        configurationManager.SPINE_MEASUREMENT_POSITION_AVERAGING_MINIMUM_WINDOWSIZE,
                        configurationManager.SPINE_MEASUREMENT_POSITION_AVERAGING_MAXIMUM_WINDOWSIZE);
        SpineCalculator spineCalculator = new SpineCalculator(
                configurationManager.SPINE_MEASUREMENT_ENDPOINT_ORIENTATION_AVERAGING_WINDOWSIZE,
                configurationManager.SPINE_MEASUREMENT_MEDIALLINE_OFFSET_FROM_CONTOUR_ENDS,
                medialLineProcessor);
        ContourCalculator contourCalculator = new ContourCalculator(getImageJOpService());
        spineLengthMeasurement = new SpineLengthMeasurement(medialLineCalculator, spineCalculator, contourCalculator);
        return spineLengthMeasurement;
    }

    OverlayUtils overlayUtils;

    public OverlayUtils getOverlayUtils() {
        if (overlayUtils == null) {
            overlayUtils = new OverlayUtils(convertService);
        }
        return overlayUtils;
    }

    public CellMaskExporter getCellMaskExporter(){
        return new CellMaskExporter(getImglib2utils(), getOverlayUtils(), () -> MoMA.getDefaultFilenameDecoration());
    }

    public GroundTruthFramesExporter getGroundTruthFramesExporter() { return groundTruthFramesExporter; }

    AssignmentCostExporter assignmentCostExporter;
    public AssignmentCostExporter getAssignmentCostExporter() {
        if (assignmentCostExporter == null) {
            assignmentCostExporter = new AssignmentCostExporter(getMomaModel().getCurrentGL(), () -> MoMA.getDefaultFilenameDecoration(), getComponentProperties());
        }
        return assignmentCostExporter;
    }

    public ITrackingConfiguration getTrackingConfiguration() {
        return configurationManager;
    }

    public UnetProcessor getUnetProcessor() {
        return unetProcessor;
    }

    public IUnetProcessingConfiguration getUnetProcessorConfiguration(){
        return configurationManager;
    }

    public WatershedMaskGenerator getWatershedMaskGenerator() { return watershedMaskGenerator; }

    public GitVersionProvider getGitVersionProvider() { return gitVersionProvider; }

    MoMAGui gui;
    public MoMAGui getMomaGui() {
        if (gui == null) {
            gui = new MoMAGui(getMomaModel(), getMomaInstance(), getMomaInstance(), ConfigurationManager.GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY);
        }
        return gui;
    }

    private MoMAModel momaModel;
    public MoMAModel getMomaModel() {
        if(momaModel == null){
            momaModel = new MoMAModel(this.momaInstance);
        }
        return momaModel;
    }

    IDialogManager dialogManager;
    public IDialogManager getDialogManager(){
        if(dialogManager == null){
            dialogManager = new DialogManager(() -> getMomaGui());
        }
        return dialogManager;
    }
}
