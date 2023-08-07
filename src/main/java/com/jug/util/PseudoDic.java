package com.jug.util;

import com.jug.MoMA;
import com.jug.commands.CloseCommand;
import com.jug.commands.ICommand;
import com.jug.config.CommandLineArgumentsParser;
import com.jug.config.ConfigurationManager;
import com.jug.config.ITrackingConfiguration;
import com.jug.config.IUnetProcessingConfiguration;
import com.jug.datahandling.*;
import com.jug.export.*;
import com.jug.export.measurements.*;
import com.jug.gui.*;
import com.jug.gui.progress.DialogGurobiProgress;
import com.jug.gui.progress.IDialogGurobiProgress;
import com.jug.logging.LoggerAdapterForSystemOutErr;
import com.jug.logging.LoggerToFile;
import com.jug.lp.*;
import com.jug.lp.costs.CostFactory;
import com.jug.util.componenttree.*;
import com.jug.util.imglib2.Imglib2Utils;
import com.jug.util.imglib2.OverlayUtils;
import com.jug.util.math.GeomUtils;
import com.jug.util.math.Vector2DPolyline;
import net.imagej.ops.OpService;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;
import org.scijava.Context;
import org.scijava.convert.ConvertService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.isNull;

/**
 * This is pseudo dependency injection container, which I use to work on getting my class dependencies and initialization
 * in order. Ideally at some point, this will be replaced with a true dependency injection framework.
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
    private IVersionProvider versionProvider;
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

    private LoggerToFile fileLogger;

    private LoggerToFile getFileLogger() {
        if (isNull(fileLogger)) {
            fileLogger = new LoggerToFile(() -> getFilePaths().getMomaLogFile());
        }
        return fileLogger;
    }

    private LoggerAdapterForSystemOutErr loggerAdapter;

    public LoggerAdapterForSystemOutErr getLogger() {
        if (isNull(loggerAdapter)) {
            loggerAdapter = new LoggerAdapterForSystemOutErr(getLoggerWindow(), getFileLogger());
        }
        return loggerAdapter;
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
            componentProperties = new ComponentProperties(getImageJOpService(), getImglib2utils(), getCostFactory(), getConfigurationManager());
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

    IComponentForestGenerator componentForestProvider;

    public IComponentForestGenerator getComponentForestGenerator() {
        if (componentForestProvider == null) {
            componentForestGenerator = new ComponentForestGenerator(getConfigurationManager(), getRecursiveComponentWatershedder(), getComponentProperties(), getWatershedMaskGenerator(), getImglib2utils());
            componentForestProvider = new ComponentForestProvider(getComponentProperties(), componentForestGenerator, getFilePaths(), getConfigurationManager(), getImageProvider());
        }
        return componentForestProvider;
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
        return new CellStatsExporter(getDialogManager(), getMomaGui(), getConfigurationManager(), getMixtureModelFit(), getComponentProperties(), getImageProvider(), getVersionProvider().getVersion().toString(), getMeasurements());
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

    public ComponentForestExporter getComponentForestExporter() {
        return new ComponentForestExporter();
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
            assignmentCostExporter = new AssignmentCostExporter(getMomaModel().getCurrentGL());
        }
        return assignmentCostExporter;
    }

    HypothesisRangeSelector hypothesisRangeSelector;

    public HypothesisRangeSelector getHypothesisRangeSelector() {
        if (!isNull(hypothesisRangeSelector)) {
            return hypothesisRangeSelector;
        }
        hypothesisRangeSelector = new HypothesisRangeSelector(getMomaModel().getCurrentGL());
        return hypothesisRangeSelector;
    }

    HypothesisActivitiesExporter hypothesisActivitiesExporter;

    public HypothesisActivitiesExporter getHypothesisActivitiesExporter() {
        if (!isNull(hypothesisActivitiesExporter)) {
            return hypothesisActivitiesExporter;
        }
        hypothesisActivitiesExporter = new HypothesisActivitiesExporter(getMomaModel().getCurrentGL(), getNewTimer(), getConfigurationManager());
        return hypothesisActivitiesExporter;
    }

    AssignmentActivitiesExporter assignmentActivitiesExporter;

    public AssignmentActivitiesExporter getAssignmentActivitiesExporter() {
        if (!isNull(assignmentActivitiesExporter)) {
            return assignmentActivitiesExporter;
        }
        assignmentActivitiesExporter = new AssignmentActivitiesExporter(getMomaModel().getCurrentGL(), getNewTimer(), getConfigurationManager());
        return assignmentActivitiesExporter;
    }

    public ITrackingConfiguration getTrackingConfiguration() {
        return configurationManager;
    }

    AssignmentFilterUsingFluoresenceOfAllFrames assignmentFilter;
    public AssignmentFilterUsingFluoresenceOfAllFrames getAssignmentFilter() {
        if(isNull(assignmentFilter)){
            assignmentFilter = new AssignmentFilterUsingFluoresenceOfAllFrames();
        }
        return assignmentFilter;
    }

    private AssignmentFilterFactory assignmentFilterFactory;

    public AssignmentFilterFactory getAssignmentFilterFactory() {
        if (isNull(assignmentFilterFactory)) {
            assignmentFilterFactory = new AssignmentFilterFactory(getConfigurationManager(), getImageProperties());
        }
        return assignmentFilterFactory;
    }

    private ImageProperties imageProperties;
    private ImageProperties getImageProperties() {
        if(isNull(imageProperties)){
            imageProperties = new ImageProperties(getImageProvider(), getImglib2utils(), getConfigurationManager());
        }
        return imageProperties;
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

    public IVersionProvider getVersionProvider() {
        if (isNull(versionProvider)) {
            versionProvider = buildVersionProvider();
        }
        return versionProvider;
    }

    /***
     * Returns builds a provider depending on whether we are running from within a JAR or the development environment.
     * @return
     */
    @NotNull
    private IVersionProvider buildVersionProvider() {
        if (runningFromJarFile()) {
            JarGitVersionReader jarGitVersionReader = new JarGitVersionReader();
            if (jarGitVersionReader.canReadJsonGitInformation()) {
                return new JarGitVersionParser(jarGitVersionReader.getJsonGitInformationString());
            } else {
                throw new RuntimeException("ERROR: Running from JAR, but JarGitVersionReader is unable to read the MoMA version.");
            }
        }
        DevelopmentGitVersionProvider developmentGitVersionProvider = new DevelopmentGitVersionProvider();
        if(developmentGitVersionProvider.canReadGitVersionInformation()){
            return developmentGitVersionProvider;
        }
        else{
            throw new RuntimeException("Could not get a version provider");
        }
    }

    private boolean runningFromJarFile() {
        return PseudoDic.class.getResource("PseudoDic.class").toString().contains("jar");
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
    public PanelWithSliders getPanelWithSliders(){
        if(isNull(panelWithSliders)){
            LayoutManager layout = new MigLayout("wrap 2", "[]3[grow,fill]", "[]0[]");
            panelWithSliders = new PanelWithSliders(layout, configurationManager, getMomaModel());
        }
        return panelWithSliders;
    }

    UiStateController uiStateController;

    public UiStateController getUiStateController() {
        if (isNull(uiStateController)) {
            uiStateController = new UiStateController(getMomaModel(), getMomaGui(), getPanelWithSliders());
        }
        return uiStateController;
    }

    public synchronized MoMAGui getMomaGui() {
        if (isNull(gui)) {
            gui = new MoMAGui(getGuiFrame(), getCloseCommand(), getMomaModel(), getImageProvider(), configurationManager.GUI_SHOW_GROUND_TRUTH_EXPORT_FUNCTIONALITY, getConfigurationManager(), getFilePaths(), getLoggerWindow(), getDialogManager(), getPanelWithSliders(), getHypothesisRangeSelector());
            getUiStateController();
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
            getGuiFrame().setTitle(String.format("MoMA %s -- %s", getVersionProvider().getVersion().toString(), datasetName));
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

    public synchronized LoggerWindow getLoggerWindow() {
        if (loggerWindow != null) {
            return loggerWindow;
        }
        loggerWindow = new LoggerWindow(getVersionProvider().getVersion().toString(), getConfigurationManager());
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

    public ResultExporterInterface getMetaDataExporter() {
        return new MetaDataExporter();
    }

    HtmlOverviewExporter htmlOverviewExporter;

    public HtmlOverviewExporter getHtmlOverviewExporterWrapper() {
        if (isNull(htmlOverviewExporter)) {
            htmlOverviewExporter = new HtmlOverviewExporter(getMomaGui());
        }
        return htmlOverviewExporter;
    }

    private ITimer loadingTimeTimer;

    public ITimer getLoadingTimer() {
        if (isNull(loadingTimeTimer)) {
            loadingTimeTimer = getNewTimer();
        }
        return loadingTimeTimer;
    }

    private ITimer exportTimer;

    public ITimer getExportTimer() {
        if (isNull(exportTimer)) {
            exportTimer = getNewTimer();
        }
        return exportTimer;
    }

    private ITimer trackingDataTimer;

    public ITimer getTrackingDataTimer() {
        if (isNull(trackingDataTimer)) {
            trackingDataTimer = getNewTimer();
        }
        return trackingDataTimer;
    }

    private ITimer componentForestTimer;

    public ITimer getComponentForestTimer() {
        if (isNull(componentForestTimer)) {
            componentForestTimer = getNewTimer();
        }
        return componentForestTimer;
    }

    private ITimer assignmentCreationTimer;

    public ITimer getAssignmentCreationTimer() {
        if (isNull(assignmentCreationTimer)) {
            assignmentCreationTimer = getNewTimer();
        }
        return assignmentCreationTimer;
    }

    private ITimer totalRuntimeTimer;

    public ITimer getTotalRuntimeTimer() {
        if (isNull(totalRuntimeTimer)) {
            totalRuntimeTimer = getNewTimer();
        }
        return totalRuntimeTimer;
    }

    private ITimer optimizationTimer;

    public ITimer getOptimizationTimer() {
        if (isNull(optimizationTimer)) {
            optimizationTimer = getNewTimer();
        }
        return optimizationTimer;
    }

    private ITimer getNewTimer(){
        return new Timer(getCommandLineArgumentParser().isTrackOnly(), getCommandLineArgumentParser().getIfRunningHeadless());
    }

    public Supplier<GurobiCallbackAbstract> getGurobiCallbackFactory() {
        return new GurobiCallbackFactory();
    }

    ComponentIntensitiesExporter componentIntensitiesExporter;

    public ResultExporterInterface getComponentIntensitiesExporter() {
        if (isNull(componentIntensitiesExporter)) {
            componentIntensitiesExporter = new ComponentIntensitiesExporter(getConfigurationManager(), getImageProperties());
        }
        return componentIntensitiesExporter;
    }

    class GurobiCallbackFactory implements Supplier<GurobiCallbackAbstract>{
        private boolean isfirstOptimizationRun = true;
        @Override
        public GurobiCallbackAbstract get() {
            if (isfirstOptimizationRun) {
                isfirstOptimizationRun = false;
                return new GurobiCallback(dialog, configurationManager.getGurobiTimeLimit()); /* for first optimization use value of configurationManager.GUROBI_TIME_LIMIT */
            } else {
                return new GurobiCallback(dialog, configurationManager.getGurobiTimeLimitDuringCuration()) /* for subsequent optimizations use value of configurationManager.GUROBI_TIME_LIMIT_DURING_CURATION */;
            }
        }
    }

    DialogGurobiProgress dialog;
    public Supplier<IDialogGurobiProgress> getGurobiProgressDialogFactory() {
        if (isNull(dialog)) {
            dialog = new DialogGurobiProgress(getGuiFrame());
        }
        return () -> dialog;
    }
}
