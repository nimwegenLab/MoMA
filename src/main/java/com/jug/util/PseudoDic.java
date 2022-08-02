package com.jug.util;

import com.jug.MoMA;
import com.jug.commands.CloseCommand;
import com.jug.commands.ICommand;
import com.jug.config.CommandLineArgumentsParser;
import com.jug.config.ConfigurationManager;
import com.jug.config.ITrackingConfiguration;
import com.jug.config.IUnetProcessingConfiguration;
import com.jug.datahandling.GlFileManager;
import com.jug.datahandling.GlDataLoader;
import com.jug.datahandling.IImageProvider;
import com.jug.datahandling.VersionCompatibilityChecker;
import com.jug.export.*;
import com.jug.export.measurements.*;
import com.jug.gui.*;
import com.jug.logging.LoggerAdapterForSystemOutErr;
import com.jug.lp.AssignmentPlausibilityTester;
import com.jug.lp.costs.CostFactory;
import com.jug.util.componenttree.*;
import com.jug.util.imglib2.Imglib2Utils;
import com.jug.util.imglib2.OverlayUtils;
import com.jug.util.math.GeomUtils;
import com.jug.util.math.Vector2DPolyline;
import net.imagej.ops.OpService;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;
import org.scijava.convert.ConvertService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Objects.isNull;

/**
 * This is pseudo dependency injection container, which I use to work on getting my class dependencies and initialization
 * in order. Ideally at some point, this will be replaced with a true dependency injection frame-work.
 */
public class PseudoDic {
    public Context context;
    public OpService ops;
    private AssignmentPlausibilityTester assignmentPlausibilityTester;
    private ComponentProperties componentProperties;
    private ConfigurationManager configurationManager;
    private MoMA momaInstance;
    private MixtureModelFit mixtureModelFit;
    private ComponentForestGenerator componentForestGenerator;
    private Imglib2Utils imglib2utils;
    private RecursiveComponentWatershedder recursiveComponentWatershedder;
    private UnetProcessor unetProcessor;
    private WatershedMaskGenerator watershedMaskGenerator;
    private GitVersionProvider gitVersionProvider;
    private SpineLengthMeasurement spineLengthMeasurement;
    private ConvertService convertService;
    private IImageProvider imageProvider;

    public PseudoDic() {
    }

    public Context getSciJavaContext() {
        if (context == null) {
            context = new Context();
        }
        return context;
    }

    private LoggerAdapterForSystemOutErr logger;

    public LoggerAdapterForSystemOutErr getLogger() {
        if (isNull(logger)) {
            logger = new LoggerAdapterForSystemOutErr(getLoggerWindow());
        }
        return logger;
    }

    public OpService getImageJOpService() {
        if (isNull(ops)) {
            ops = getSciJavaContext().service(OpService.class);
        }
        return ops;
    }

    public AssignmentPlausibilityTester getAssignmentPlausibilityTester() {
        if (assignmentPlausibilityTester == null) {
            assignmentPlausibilityTester = new AssignmentPlausibilityTester(getConfigurationManager());
        }
        return assignmentPlausibilityTester;
    }

    public ComponentProperties getComponentProperties() {
        if (isNull(componentProperties)) {
            componentProperties = new ComponentProperties(getImageJOpService(), getImglib2utils());
        }
        return componentProperties;
    }

    public ConfigurationManager getConfigurationManager() {
        if (configurationManager == null) {
            configurationManager = new ConfigurationManager();
        }
        return configurationManager;
    }

    public MixtureModelFit getMixtureModelFit() {
        if (mixtureModelFit == null) {
            mixtureModelFit = new MixtureModelFit(getConfigurationManager());
        }
        return mixtureModelFit;
    }

    public void setImageProvider(IImageProvider imageProvider) {
        this.imageProvider = imageProvider;
    }

    public IImageProvider getImageProvider() {
        return this.imageProvider;
    }

    public MoMA getMomaInstance() {
        if (isNull(momaInstance)) {
            momaInstance = new MoMA();
        }
        return momaInstance;
    }

    public ComponentForestGenerator getComponentForestGenerator() {
        if (componentForestGenerator == null) {
            componentForestGenerator = new ComponentForestGenerator(getConfigurationManager(), getRecursiveComponentWatershedder(), getComponentProperties(), getWatershedMaskGenerator(), getImglib2utils());
        }
        return componentForestGenerator;
    }

    private RecursiveComponentWatershedder getRecursiveComponentWatershedder() {
        if (isNull(recursiveComponentWatershedder)) {
            recursiveComponentWatershedder = new RecursiveComponentWatershedder(getImageJOpService());
        }
        return recursiveComponentWatershedder;
    }

    public Imglib2Utils getImglib2utils() {
        if (isNull(imglib2utils)) {
            imglib2utils = new Imglib2Utils(getImageJOpService());
        }
        return imglib2utils;
    }

    public CellStatsExporter getCellStatsExporter() {
        return new CellStatsExporter(getMomaGui(), getConfigurationManager(), getMixtureModelFit(), getComponentProperties(), getImageProvider(), getGitVersionProvider().getVersionString(), getMeasurements());
    }

    private List<SegmentMeasurementInterface> getMeasurements() {
        List<SegmentMeasurementInterface> listOfMeasurements = new ArrayList<>();
        if (configurationManager.EXPORT_ORIENTED_BOUNDING_BOX_MEASUREMENT) {
            listOfMeasurements.add(new OrientedBoundingBoxMeasurement(context));
        }
        listOfMeasurements.add(new ContourMomentsMeasurement(getComponentProperties()));

        if (configurationManager.EXPORT_SPINE_MEASUREMENT) {
            listOfMeasurements.add(getSpineLengthMeasurement());
        }

        if (configurationManager.EXPORT_PROBABILITY_AREA_MEASUREMENT) {
            listOfMeasurements.add(getProbabilityAreaMeasurement());
        }
//        listOfMeasurements.add(getEllipseMeasurement());
        return listOfMeasurements;
    }

    private SegmentMeasurementInterface proabilityAreaMeasurement;

    private SegmentMeasurementInterface getProbabilityAreaMeasurement() {
        if (proabilityAreaMeasurement != null) {
            return proabilityAreaMeasurement;
        }
        proabilityAreaMeasurement = new AreaMeasurementUsingProbability();
        return proabilityAreaMeasurement;
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
        if (isNull(overlayUtils)) {
            overlayUtils = new OverlayUtils(getConvertService());
        }
        return overlayUtils;
    }

    private ConvertService getConvertService() {
        if (isNull(convertService)) {
            convertService = getSciJavaContext().service(ConvertService.class);
        }
        return convertService;
    }

    public CellMaskExporter getCellMaskExporter() {
        return new CellMaskExporter(getImglib2utils(), getOverlayUtils());
    }

    public IlpModelExporter getIlpModelExporter() {
        return new IlpModelExporter();
    }

    private GroundTruthFramesExporter groundTruthFramesExporter;

    public GroundTruthFramesExporter getGroundTruthFramesExporter() {
        if (groundTruthFramesExporter != null) {
            return groundTruthFramesExporter;
        }
        groundTruthFramesExporter = new GroundTruthFramesExporter(); /* we pass a supplier here, because at this point in the instantiation MoMA.getDefaultFilenameDecoration() still Null; once instantiation is clean up, this should not be necessary anymore */
        return groundTruthFramesExporter;
    }

    CostFactory costFactory;

    public CostFactory getCostFactory() {
        if (costFactory == null) {
            costFactory = new CostFactory(getConfigurationManager());
        }
        return costFactory;
    }

    AssignmentCostExporter assignmentCostExporter;

    public AssignmentCostExporter getAssignmentCostExporter() {
        if (assignmentCostExporter == null) {
            assignmentCostExporter = new AssignmentCostExporter(getMomaModel().getCurrentGL(), getComponentProperties(), getCostFactory());
        }
        return assignmentCostExporter;
    }

    public ITrackingConfiguration getTrackingConfiguration() {
        return configurationManager;
    }

    public UnetProcessor getUnetProcessor() {
        if (isNull(unetProcessor)) {
            unetProcessor = new UnetProcessor(getSciJavaContext(), getUnetProcessorConfiguration());
        }
        return unetProcessor;
    }

    public IUnetProcessingConfiguration getUnetProcessorConfiguration() {
        return configurationManager;
    }

    public WatershedMaskGenerator getWatershedMaskGenerator() {
        if (watershedMaskGenerator != null) {
            return watershedMaskGenerator;
        }
        watershedMaskGenerator = new WatershedMaskGenerator(getConfigurationManager().THRESHOLD_FOR_COMPONENT_MERGING, getConfigurationManager().THRESHOLD_FOR_COMPONENT_GENERATION);
        return watershedMaskGenerator;
    }

    public GitVersionProvider getGitVersionProvider() {
        if (isNull(gitVersionProvider)) {
            gitVersionProvider = new GitVersionProvider();
        }
        return gitVersionProvider;
    }

    public VersionCompatibilityChecker getVersionCompatibilityChecker(){
        return new VersionCompatibilityChecker();
    }

    private MoMAModel momaModel;

    public MoMAModel getMomaModel() {
        if (momaModel == null) {
            momaModel = new MoMAModel(getGlDataLoader());
        }
        return momaModel;
    }

    DialogPropertiesEditor propsEditor;

    public DialogPropertiesEditor getPropertiesEditorWindow() {
        if (propsEditor != null) {
            return propsEditor;
        }
        propsEditor = new DialogPropertiesEditor(getMomaGui(), getConfigurationManager().props, getConfigurationManager(), this);
        return propsEditor;
    }

    IDialogManager dialogManager;

    public IDialogManager getDialogManager() {
        if (dialogManager == null) {
            dialogManager = new DialogManager(() -> getMomaGui(), () -> getPropertiesEditorWindow());
        }
        return dialogManager;
    }

    MoMAGui gui;

    PanelWithSliders panelWithSliders;
    public PanelWithSliders getRangeSliderPanel(){
        if(isNull(panelWithSliders)){
            LayoutManager layout = new MigLayout("wrap 2", "[]3[grow,fill]", "[]0[]");
            panelWithSliders = new PanelWithSliders(layout, configurationManager, getMomaModel());
        }
        return panelWithSliders;
    }

    public MoMAGui getMomaGui() {
        if (gui == null) {
            gui = new MoMAGui(getGuiFrame(), getCloseCommand(), getMomaModel(), getImageProvider(), configurationManager.GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY, getConfigurationManager(), getFilePaths(), getLoggerWindow(), getDialogManager(), getRangeSliderPanel());
        }
        return gui;
    }

    JFrame guiFrame;

    public JFrame getGuiFrame() {
        if (getConfigurationManager().getIfRunningHeadless()) {
            return null; /* TODO-MM-20220704: We are running headless, so return null as guiFrame, this is how Florian implemented it; it is hacky and should be refactored */
        }
        if (isNull(guiFrame)) {
            guiFrame = buildGuiFrame();
            guiFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            guiFrame.setVisible(true);
        }
        return guiFrame;
    }

    /**
     * Initializes the MotherMachine main app. This method contains platform
     * specific code like setting icons, etc.
     */
    private JFrame buildGuiFrame() {
        JFrame myGuiFrame = new JFrame();
        return myGuiFrame;
    }

    private ICommand closeCommand;

    public ICommand getCloseCommand() {
        if (isNull(closeCommand)) {
            closeCommand = new CloseCommand(getGuiFrame(), getConfigurationManager(), getCommandLineArgumentParser());
        }
        return closeCommand;
    }

    /**
     * @param datasetName the datasetName to set
     */
    public void setDatasetNameInWindowTitle(final String datasetName) {
        if (getGuiFrame() != null) {
            getGuiFrame().setTitle(String.format("MoMA %s -- %s", getGitVersionProvider().getVersionString(), datasetName));
        }
    }

    private GlDataLoader glDataLoader;

    public GlDataLoader getGlDataLoader() {
        if (glDataLoader != null) {
            return glDataLoader;
        }
        glDataLoader = new GlDataLoader(getUnetProcessor(),
                getConfigurationManager(),
                getImageProvider(),
                getComponentForestGenerator(),
                getDialogManager(),
                getFilePaths());
        return glDataLoader;
    }

    private LoggerWindow loggerWindow;

    public LoggerWindow getLoggerWindow() {
        if (loggerWindow != null) {
            return loggerWindow;
        }
        loggerWindow = new LoggerWindow(getGitVersionProvider().getVersionString(), getConfigurationManager());
        return loggerWindow;
    }

    private CommandLineArgumentsParser commandLineArgumentParser;

    public CommandLineArgumentsParser getCommandLineArgumentParser() {
        if(isNull(commandLineArgumentParser)){
            commandLineArgumentParser = new CommandLineArgumentsParser();
        }
        return commandLineArgumentParser;
    }

    GlFileManager glFileManager;

    public GlFileManager getFilePaths() {
        if (isNull(glFileManager)) {
            glFileManager = new GlFileManager();
        }
        return glFileManager;
    }

    public ResultExporterInterface getMMPropertiesExporter() {
        return new MMPropertiesExporter(getGuiFrame(), getConfigurationManager());
    }

    public ResultExporterInterface getCurationStatsExporter() {
        return new CurationStatsExporter();
    }
}
