package com.jug.gui;

import com.jug.GrowthlaneFrame;
import com.jug.MoMA;
import com.jug.commands.ICommand;
import com.jug.config.ConfigurationManager;
import com.jug.datahandling.GlFileManager;
import com.jug.datahandling.IImageProvider;
import com.jug.export.HtmlOverviewExporterWriter;
import com.jug.export.ResultExporter;
import com.jug.export.ResultExporterInterface;
import com.jug.gui.assignmentview.AssignmentsEditorViewer;
import com.jug.gui.progress.DialogProgress;
import com.jug.lp.GrowthlaneTrackingILP;
import ij.ImageJ;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.jetbrains.annotations.Nullable;
import weka.gui.ExtensionFileFilter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.isNull;

/**
 * @author jug
 */
public class MoMAGui extends JPanel implements ChangeListener, ActionListener {

    private static final long serialVersionUID = -1008974839249784873L;

    private JFrame guiFrame;
    private ICommand closeCommand;
    public final MoMAModel model;
    private IDialogManager dialogManager;
    private PanelWithSliders panelWithSliders;
    private HypothesisRangeSelector hypothesisRangeSelector;
    private final String itemChannel0 = "Channel 0";
    private final String itemChannel1 = "Channel 1";
    private final String itemChannel2 = "Channel 2";
    private final List<IlpVariableEditorPanel> ilpVariableEditorPanels = new ArrayList<>();
    private final List<AssignmentEditorPanel> assignmentEditorPanels = new ArrayList<>();
    private final List<SegmentationEditorPanel> segmentationEditorPanels = new ArrayList<>();
    private final boolean showGroundTruthExportFunctionality;
    private ConfigurationManager configurationManager;
    private GlFileManager glFileManager;
    private LoggerWindow loggerWindow;
    private final IImageProvider imageProvider;
    public JSlider sliderGL;

    private GrowthlaneViewer centeredGrowthLaneViewer;
    public AssignmentsEditorViewer assignmentsEditorViewerUsedForHtmlExport;
    private SegmentationEditorPanel segmentationEditorPanelCenter;
    private boolean showSegmentationAnnotations = true; /* show helper lines in IntervalViews? */
    private JTabbedPane tabsViews;
    private CountOverviewPanel panelCountingView;
    private JScrollPane panelSegmentationAndAssignmentView;
    private JCheckBox checkboxAutosave;
    private JButton buttonRestart;

    private JButton buttonOptimizeMore;
    private JButton buttonExportHtml;
    private JButton buttonExportData;

    private JButton buttonSaveTracking;

    private JButton buttonSaveTrackingAndExit;

    private JComboBox comboboxWhichImgToShow;

    private JButton buttonFreezePreviousTimeSteps;
    private JButton buttonSet;
    private JButton buttonReset;

    public List<JComponent> getComponentsToDeactivateWhenIlpIsInfeasible() {
        return Arrays.asList(buttonExportHtml,
                buttonExportData,
                buttonSaveTracking,
                buttonSaveTrackingAndExit);
    }

    public List<JComponent> getAllComponentsToUpdate() {
        return Arrays.asList(checkboxAutosave,
                buttonRestart,
                buttonOptimizeMore,
                buttonExportHtml,
                buttonExportData,
                buttonSaveTracking,
                buttonSaveTrackingAndExit);
    }

    public List<JComponent> getComponentsToDeactivateWhenOptimizationIsRunning() {
        return Arrays.asList(checkboxAutosave,
                buttonRestart,
                buttonOptimizeMore,
                buttonExportHtml,
                buttonExportData,
                buttonSaveTracking,
                buttonSaveTrackingAndExit);
    }

    public List<JComponent> getComponentsToDeactivateWhenOptimizationWasNeverRun() {
        return Arrays.asList(buttonExportHtml,
                buttonExportData,
                buttonSaveTracking,
                buttonSaveTrackingAndExit);
    }

    private MenuItem menuViewShowConsole;
    private MenuItem menuShowImgRaw;
    private MenuItem menuProps;
    private MenuItem menuLoad;
    private MenuItem menuSave;

    /**
     * Construction
     *
     * @param model the MotherMachineModel to show
     */
    public MoMAGui(JFrame guiFrame,
                   ICommand closeCommand,
                   final MoMAModel model,
                   IImageProvider imageProvider,
                   boolean showGroundTruthExportFunctionality,
                   ConfigurationManager configurationManager,
                   GlFileManager glFileManager,
                   LoggerWindow loggerWindow,
                   IDialogManager dialogManager,
                   PanelWithSliders panelWithSliders,
                   HypothesisRangeSelector hypothesisRangeSelector) {
        super(new BorderLayout());
        this.guiFrame = guiFrame;
        this.closeCommand = closeCommand;

        this.model = model;
        this.imageProvider = imageProvider;
        this.showGroundTruthExportFunctionality = showGroundTruthExportFunctionality;
        this.configurationManager = configurationManager;
        this.glFileManager = glFileManager;
        this.loggerWindow = loggerWindow;

        this.dialogManager = dialogManager;

        this.panelWithSliders = panelWithSliders;
        this.hypothesisRangeSelector = hypothesisRangeSelector;
        registerSliderListeners();

        buildGui();
        dataToDisplayChanged();
        requestFocusOnTimeStepSlider();
    }

    private void registerSliderListeners() {
        this.panelWithSliders.addListenerToTimeSlider((changeEvent) -> {
            if (spaceBarIsBeingHeld.isActive()) {
                segmentationEditorPanelCenter.toggleGroundTruthSelectionCheckbox();
            }
            dataToDisplayChanged();
        });

        this.panelWithSliders.addListenerToRangeSlider((changeEvent) -> {
            JSlider slider = (JSlider) changeEvent.getSource();
            if (!slider.getValueIsAdjusting()) {
                if (model.getCurrentGL().getIlp().isReady()) {
                    if(panelWithSliders.getTrackingRangeStart() != previousTrackingRangeStart){
                        model.getCurrentGL().getIlp().addPreOptimizationRangeLockConstraintsBefore(panelWithSliders.getTrackingRangeStart());
                        previousTrackingRangeStart = panelWithSliders.getTrackingRangeStart();
                    }
                    if(panelWithSliders.getTrackingRangeEnd() != previousTrackingRangeEnd) {
                        model.getCurrentGL().getIlp().addPostOptimizationRangeLockConstraintsAfter(panelWithSliders.getTrackingRangeEnd());
                        previousTrackingRangeEnd = panelWithSliders.getTrackingRangeEnd();
                    }
                }
                dataToDisplayChanged();
            }
        });
    }

    int previousTrackingRangeStart = -1;
    int previousTrackingRangeEnd = -1;

    /**
     * Builds the GUI.
     */
    private void buildGui() {
        final MenuBar menuBar = new MenuBar();
        final Menu menuFile = new Menu("File");
        menuProps = new MenuItem("Preferences");
        menuProps.addActionListener(this);
        menuLoad = new MenuItem("Load tracking");
        menuLoad.addActionListener(this);
        menuSave = new MenuItem("Save tracking");
        menuSave.addActionListener(this);
        menuFile.add(menuProps);
        menuFile.addSeparator();
        menuFile.add(menuLoad);
        menuFile.add(menuSave);
        menuBar.add(menuFile);

        final Menu menuView = new Menu("View");
        menuViewShowConsole = new MenuItem("Show/hide Console");
        menuViewShowConsole.addActionListener(this);
        menuShowImgRaw = new MenuItem("Show raw images...");
        menuShowImgRaw.addActionListener(this);
        MenuItem menuTrain = new MenuItem("Show trainer window...");
        menuTrain.addActionListener(this);
        menuView.add(menuViewShowConsole);
        menuView.add(menuTrain);
        menuView.addSeparator();
        menuView.add(menuShowImgRaw);
        menuBar.add(menuView);
        if (!configurationManager.getIfRunningHeadless()) {
            guiFrame.setMenuBar(menuBar);
        }

        final JPanel panelContent = new JPanel(new BorderLayout());
        JPanel panelVerticalHelper;
        JPanel panelHorizontalHelper;

        panelHorizontalHelper = new JPanel(new BorderLayout());
        panelHorizontalHelper.add(panelWithSliders, BorderLayout.CENTER);
        panelContent.add(panelHorizontalHelper, BorderLayout.SOUTH);

        /* the GL slider is currently not being used; but we keep the code for the moment, because it could become relevant again */
        sliderGL = new JSlider(SwingConstants.VERTICAL, 0, model.mm.getGrowthlanes().size() - 1, 0);
        sliderGL.setValue(0);
        sliderGL.addChangeListener(this);
        sliderGL.setMajorTickSpacing(5);
        sliderGL.setMinorTickSpacing(1);
        sliderGL.setPaintTicks(true);
        sliderGL.setPaintLabels(true);
        sliderGL.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));
        panelVerticalHelper = new JPanel(new BorderLayout());
        panelVerticalHelper.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 5));
        panelVerticalHelper.add(new JLabel("GL#"), BorderLayout.NORTH);
        panelVerticalHelper.add(sliderGL, BorderLayout.CENTER);
        // show the slider only if it actually has a purpose...
        if (sliderGL.getMaximum() > 1) {
            add(panelVerticalHelper, BorderLayout.WEST);
        }

        // --- All the TABs -------------

        tabsViews = new JTabbedPane();
        tabsViews.addChangeListener(this);

        panelCountingView = new CountOverviewPanel();
        panelSegmentationAndAssignmentView = new JScrollPane(buildSegmentationAndAssignmentView());
        panelSegmentationAndAssignmentView.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        tabsViews.add("Segm. & Assignments", panelSegmentationAndAssignmentView);

        tabsViews.setSelectedComponent(panelSegmentationAndAssignmentView);

        // --- Controls ----------------------------------
        checkboxAutosave = new JCheckBox("autosave?");
        checkboxAutosave.addActionListener(this);
        buttonRestart = new JButton("Restart");
        buttonRestart.addActionListener(this);
        buttonOptimizeMore = new JButton("Optimize");
        buttonOptimizeMore.setForeground(Color.RED);
        buttonOptimizeMore.addActionListener(this);

        for (IlpVariableEditorPanel ilpVariableEditorPanel : ilpVariableEditorPanels) {
            ilpVariableEditorPanel.addIlpModelChangedEventListener(evt -> {
                if (!configurationManager.getRunIlpOnChange()) {
                    buttonOptimizeMore.setForeground(Color.RED);
                }
            });
        }
        buttonOptimizeMore.setEnabled(false);

        buttonExportHtml = new JButton("Export HTML");
        buttonExportHtml.addActionListener(this);
        buttonExportData = new JButton("Export data");
        buttonExportData.addActionListener(this);
        buttonSaveTracking = new JButton("Save tracking");
        buttonSaveTracking.addActionListener(this);
        buttonSaveTrackingAndExit = new JButton("Save tracking & exit");
        buttonSaveTrackingAndExit.addActionListener(this);

        panelHorizontalHelper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panelHorizontalHelper.setBorder(BorderFactory.createEmptyBorder(3, 0, 5, 0));
        panelHorizontalHelper.add(checkboxAutosave);
//		panelHorizontalHelper.add( btnRedoAllHypotheses );
        panelHorizontalHelper.add(buttonRestart);
        panelHorizontalHelper.add(buttonOptimizeMore);
        panelHorizontalHelper.add(buttonExportHtml);
        panelHorizontalHelper.add(buttonExportData);
        panelHorizontalHelper.add(buttonSaveTracking);
        panelHorizontalHelper.add(buttonSaveTrackingAndExit);
        add(panelHorizontalHelper, BorderLayout.SOUTH);
        panelHorizontalHelper.setEnabled(false);

        // --- Final adding and layout steps -------------

        panelContent.add(tabsViews, BorderLayout.CENTER);
        add(panelContent, BorderLayout.CENTER);

        setupKeyMap();
    }

    ActiveHighLatch spaceBarIsBeingHeld = new ActiveHighLatch();

    private void setupKeyMap() {
        InputMap inputMap = this.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "ESCAPE");
        inputMap.put(KeyStroke.getKeyStroke("pressed SPACE"), "pressed_SPACE");
        inputMap.put(KeyStroke.getKeyStroke("released SPACE"), "released_SPACE");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK), "ignore_selected_component");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK), "clear_user_constraints_component");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK), "force_mapping_assignments");
        inputMap.put(KeyStroke.getKeyStroke('l'), "MMGUI_bindings");
        inputMap.put(KeyStroke.getKeyStroke('t'), "MMGUI_bindings");
        inputMap.put(KeyStroke.getKeyStroke('g'), "MMGUI_bindings");
        inputMap.put(KeyStroke.getKeyStroke('a'), "MMGUI_bindings");
        inputMap.put(KeyStroke.getKeyStroke('s'), "MMGUI_bindings");
        inputMap.put(KeyStroke.getKeyStroke('d'), "MMGUI_bindings");
        inputMap.put(KeyStroke.getKeyStroke('r'), "MMGUI_bindings");
        inputMap.put(KeyStroke.getKeyStroke('o'), "MMGUI_bindings");
        inputMap.put(KeyStroke.getKeyStroke('e'), "MMGUI_bindings");
        inputMap.put(KeyStroke.getKeyStroke('v'), "MMGUI_bindings");
        inputMap.put(KeyStroke.getKeyStroke('b'), "MMGUI_bindings");
        inputMap.put(KeyStroke.getKeyStroke('p'), "MMGUI_bindings");
        inputMap.put(KeyStroke.getKeyStroke('n'), "MMGUI_bindings");
        inputMap.put(KeyStroke.getKeyStroke('0'), "MMGUI_bindings");
        inputMap.put(KeyStroke.getKeyStroke('1'), "MMGUI_bindings");
        inputMap.put(KeyStroke.getKeyStroke('2'), "MMGUI_bindings");
        inputMap.put(KeyStroke.getKeyStroke('3'), "MMGUI_bindings");
        inputMap.put(KeyStroke.getKeyStroke('4'), "MMGUI_bindings");
        inputMap.put(KeyStroke.getKeyStroke('5'), "MMGUI_bindings");
        inputMap.put(KeyStroke.getKeyStroke('6'), "MMGUI_bindings");
        inputMap.put(KeyStroke.getKeyStroke('7'), "MMGUI_bindings");
        inputMap.put(KeyStroke.getKeyStroke('8'), "MMGUI_bindings");
        inputMap.put(KeyStroke.getKeyStroke('9'), "MMGUI_bindings");

        ActionMap actionMap = this.getActionMap();

        getActionMap().put("ignore_selected_component", new FunctionalAction(a -> {
            hypothesisRangeSelector.forceIgnoreSelectedHypotheses();
            dataToDisplayChanged();
        }));

        getActionMap().put("clear_user_constraints_component", new FunctionalAction(a -> {
            hypothesisRangeSelector.clearUserConstraints();
            dataToDisplayChanged();
        }));

        getActionMap().put("force_mapping_assignments", new FunctionalAction(a -> {
            hypothesisRangeSelector.forceMappingAssigmentBetweenSelectedHypotheses();
            dataToDisplayChanged();
        }));

        actionMap.put("ESCAPE", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                hypothesisRangeSelector.clearSelectedHypotheses();
                dataToDisplayChanged();
                requestFocusOnTimeStepSlider();
            }
        });

        actionMap.put("pressed_SPACE", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (!spaceBarIsBeingHeld.isActive()) { /* use spaceBarIsBeingHeld to call toggleGroundTruthSelectionCheckbox() only once */
                    segmentationEditorPanelCenter.toggleGroundTruthSelectionCheckbox();
                }
                spaceBarIsBeingHeld.set();
            }
        });

        actionMap.put("released_SPACE", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                spaceBarIsBeingHeld.reset();
            }
        });

        actionMap.put("MMGUI_bindings", new AbstractAction() {

            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                if (e.getActionCommand().equals("l")) {
                    dataToDisplayChanged();
                }
                if (e.getActionCommand().equals("t")) {
                    requestFocusOnTimeStepSlider();
                    dataToDisplayChanged();
                }
                if (e.getActionCommand().equals("g")) {
                    panelWithSliders.setTimeStepSliderPosition(panelWithSliders.getTrackingRangeEnd());
                    dataToDisplayChanged();
                }
                if (e.getActionCommand().equals("a")) {
                    buttonFreezePreviousTimeSteps.doClick();
                    dataToDisplayChanged();
                }
                if (e.getActionCommand().equals("s")) {
                    openSegmentView();
                }
                if (e.getActionCommand().equals("e")) {
                    buttonExportData.doClick();
                }
                if (e.getActionCommand().equals("r")) {
                    buttonRestart.doClick();
                }
                if (e.getActionCommand().equals("o")) {
                    buttonOptimizeMore.doClick();
                }
                if (e.getActionCommand().equals("v")) {
                    int selIdx = comboboxWhichImgToShow.getSelectedIndex();
                    selIdx++;
                    if (selIdx == comboboxWhichImgToShow.getItemCount()) {
                        selIdx = 0;
                    }
                    comboboxWhichImgToShow.setSelectedIndex(selIdx);
                }
                if (e.getActionCommand().equals("0")) {
                    switchAssignmentViewerTabs(0);
                    dataToDisplayChanged();
                }
                if (e.getActionCommand().equals("1")) {
                    switchAssignmentViewerTabs(1);
                    dataToDisplayChanged();
                }
                if (e.getActionCommand().equals("2")) {
                    switchAssignmentViewerTabs(2);
                    dataToDisplayChanged();
                }
                if (e.getActionCommand().equals("3")) {
                    switchAssignmentViewerTabs(3);
                    dataToDisplayChanged();
                }
                if (e.getActionCommand().equals("4")) {
                    switchAssignmentViewerTabs(4);
                    dataToDisplayChanged();
                }
                if (e.getActionCommand().equals("b")) {
                    showSegmentationAnnotations = !showSegmentationAnnotations;
                    for (IlpVariableEditorPanel entry : ilpVariableEditorPanels) {
                        entry.showSegmentationAnnotations(showSegmentationAnnotations);
                    }
                    dataToDisplayChanged();
                }
            }
        });
    }

    private void openSegmentView() {
        SegmentationEditorPanel hoveredAssignmentEditorPanel = getHoveredSegmentationEditorPanel();
        if (!isNull(hoveredAssignmentEditorPanel)) {
            hoveredAssignmentEditorPanel.openSegmentView();
            return;
        }
        segmentationEditorPanelCenter.openSegmentView();
    }

    private AssignmentEditorPanel getHoveredAssignmentEditorPanel() {
        for (AssignmentEditorPanel entry : assignmentEditorPanels) {
            if (entry.isMouseOver()) {
                return entry;
            }
        }
        return null;
    }

    private void switchAssignmentViewerTabs(int tabIndex) {
        AssignmentEditorPanel hoveredAssignmentEditorPanel = getHoveredAssignmentEditorPanel();
        if (hoveredAssignmentEditorPanel != null) {
            hoveredAssignmentEditorPanel.switchToTab(tabIndex);
            return;
        }
        switchAllAssignmentViewerTabs(tabIndex);
    }

    private void switchAllAssignmentViewerTabs(int tabIndex) {
        for (AssignmentEditorPanel entry : assignmentEditorPanels) {
            entry.switchToTab(tabIndex);
        }
    }

    /**
     * @return
     */
    private JPanel buildSegmentationAndAssignmentView() {
        final JPanel panelContent = new JPanel(new BorderLayout());

        GridBagConstraints gridBagConstraintPanel1 = new GridBagConstraints();
        gridBagConstraintPanel1.anchor = GridBagConstraints.NORTH;

        GridBagConstraints gridBagConstraintPanel2 = new GridBagConstraints();
        gridBagConstraintPanel2.anchor = GridBagConstraints.NORTH;
        gridBagConstraintPanel2.insets = new Insets(10, 3, 10, 3);

        GridBagConstraints gridBagConstraintPanel3 = new GridBagConstraints();
        gridBagConstraintPanel3.anchor = GridBagConstraints.NORTH;

        GridBagConstraints gridBagConstraintPanel4 = new GridBagConstraints();
        gridBagConstraintPanel4.anchor = GridBagConstraints.NORTH;

        final JPanel panelViewCenterHelper = new JPanel();
        panelViewCenterHelper.setLayout(new BoxLayout(panelViewCenterHelper, BoxLayout.PAGE_AXIS));
        final JPanel panel1 = new JPanel();
        GridBagLayout panel1Layout = new GridBagLayout();
        panel1.setLayout(panel1Layout);

        final JPanel panel2 = new JPanel();
        GridBagLayout panel2Layout = new GridBagLayout();
        panel2.setLayout(panel2Layout);

        final JPanel panel3 = new JPanel();
        GridBagLayout panel3Layout = new GridBagLayout();
        panel3.setLayout(panel3Layout);

        final JPanel panel4 = new JPanel();
        GridBagLayout panel4Layout = new GridBagLayout();
        panel4.setLayout(panel4Layout);

        panelViewCenterHelper.add(panel1);
        panelViewCenterHelper.add(panel2);
        panelViewCenterHelper.add(panel3);
        panelViewCenterHelper.add(panel4);
        panelContent.add(panelViewCenterHelper, BorderLayout.CENTER);

        // =============== panelDropdown-part ===================
        comboboxWhichImgToShow = new JComboBox();
        comboboxWhichImgToShow.addItem(itemChannel0);
        if (imageProvider.getNumberOfChannels() > 1) {
            comboboxWhichImgToShow.addItem(itemChannel1);
        }
        if (imageProvider.getNumberOfChannels() > 2) {
            comboboxWhichImgToShow.addItem(itemChannel2);
        }

        comboboxWhichImgToShow.addActionListener(e -> {
            setColorChannelOnSegmentEditorPanels();
            dataToDisplayChanged();
        });

        int viewHeight = (int) imageProvider.getImgRaw().dimension(1);
        int viewWidth = configurationManager.GL_WIDTH_IN_PIXELS + 2 * configurationManager.GL_PIXEL_PADDING_IN_VIEWS;

        LabelEditorDialog labelEditorDialog = new LabelEditorDialog(this, configurationManager.CELL_LABEL_LIST);

        int min_time_offset = -configurationManager.GUI_NUMBER_OF_SHOWN_TIMESTEPS / 2;
        int max_time_offset = configurationManager.GUI_NUMBER_OF_SHOWN_TIMESTEPS / 2;
        for (int time_offset = min_time_offset; time_offset < max_time_offset; time_offset++) {
            SegmentationEditorPanel segmentationEditorPanel = new SegmentationEditorPanel(this, model, imageProvider, labelEditorDialog, dialogManager, viewWidth, viewHeight, time_offset, showGroundTruthExportFunctionality, MoMA.dic.getGroundTruthFramesExporter(), configurationManager, MoMA.dic.getHypothesisRangeSelector());
            panel1.add(segmentationEditorPanel, gridBagConstraintPanel1);
            ilpVariableEditorPanels.add(segmentationEditorPanel);
            segmentationEditorPanels.add(segmentationEditorPanel);

            AssignmentEditorPanel assignmentEditorPanel = new AssignmentEditorPanel(this, model, viewHeight, time_offset, configurationManager);
            panel1.add(assignmentEditorPanel, gridBagConstraintPanel1);
            ilpVariableEditorPanels.add(assignmentEditorPanel);
            assignmentEditorPanels.add(assignmentEditorPanel);

            if (time_offset == 0) {
                centeredGrowthLaneViewer = segmentationEditorPanel.getGrowthlaneViewer();
                segmentationEditorPanelCenter = segmentationEditorPanel;
                assignmentsEditorViewerUsedForHtmlExport = assignmentEditorPanel.getAssignmentViewerPanel();
            }
        }
        IlpVariableEditorPanel segmentationEditorPanel = new SegmentationEditorPanel(this, model, imageProvider, labelEditorDialog, dialogManager, viewWidth, viewHeight, max_time_offset, showGroundTruthExportFunctionality, MoMA.dic.getGroundTruthFramesExporter(), configurationManager, MoMA.dic.getHypothesisRangeSelector());
        panel1.add(segmentationEditorPanel, gridBagConstraintPanel1);
        ilpVariableEditorPanels.add(segmentationEditorPanel);
        segmentationEditorPanels.add((SegmentationEditorPanel) segmentationEditorPanel);

        buttonFreezePreviousTimeSteps = new JButton("<-all");
        buttonFreezePreviousTimeSteps.addActionListener(this);
        buttonSet = new JButton("set");
        buttonSet.addActionListener(this);
        buttonReset = new JButton("reset");
        buttonReset.addActionListener(this);

        // - - - - - -

        panel2.add(buttonFreezePreviousTimeSteps, gridBagConstraintPanel2);
        panel2.add(buttonSet, gridBagConstraintPanel2);
        panel2.add(buttonReset, gridBagConstraintPanel2);

//        panelDropdown.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        panel3.add(comboboxWhichImgToShow, gridBagConstraintPanel3);

        JCheckBox checkboxOptimizeOnIlpChange = new JCheckBox();
        checkboxOptimizeOnIlpChange.setSelected(configurationManager.getRunIlpOnChange());
        checkboxOptimizeOnIlpChange.setText("Run optimization on change");
        checkboxOptimizeOnIlpChange.addActionListener(e -> {
            if (checkboxOptimizeOnIlpChange.isSelected()) {
                configurationManager.setRunIlpOnChange(true);
                JOptionPane.showMessageDialog(this, "Optimization will now run automatically after each change. It is suggested to run optimization once now before continuing by pressing the button 'Optimize'.");
                return;
            }
            configurationManager.setRunIlpOnChange(false);
            JOptionPane.showMessageDialog(this, "Optimization now needs to be run manually by pressing the button 'Optimize' after making changes.");
        });
        panel4.add(checkboxOptimizeOnIlpChange, gridBagConstraintPanel4);

        return panelContent;
    }

    private void setColorChannelOnSegmentEditorPanels() {
        ColorChannel channelToDisplay = ColorChannel.CHANNEL0; // default channel is ColorChannel.CHANNEL0
        if (comboboxWhichImgToShow.getSelectedItem().equals(itemChannel1)) {
            channelToDisplay = ColorChannel.CHANNEL1;
        } else if (comboboxWhichImgToShow.getSelectedItem().equals(itemChannel2)) {
            channelToDisplay = ColorChannel.CHANNEL2;
        }
        setColorChannelOnHoveredPanelOrAllPanels(channelToDisplay);
    }

    private void setColorChannelOnHoveredPanelOrAllPanels(ColorChannel colorChannelToDisplay) {
        SegmentationEditorPanel hoveredAssignmentEditorPanel = getHoveredSegmentationEditorPanel();
        if (hoveredAssignmentEditorPanel != null) {
            hoveredAssignmentEditorPanel.colorChannelToDisplay = colorChannelToDisplay;
            return;
        }
        for (SegmentationEditorPanel segmentationEditorPanel : segmentationEditorPanels) {
            segmentationEditorPanel.colorChannelToDisplay = colorChannelToDisplay;
        }
    }

    private SegmentationEditorPanel getHoveredSegmentationEditorPanel() {
        for (SegmentationEditorPanel entry : segmentationEditorPanels) {
            if (entry.isMouseOver()) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Picks the right hyperslice in Z direction in imgRaw and sets an
     * View.offset according to the current offset settings. Note: this method
     * does not and should not invoke a repaint!
     */
    @SuppressWarnings({"unchecked"})
    public void dataToDisplayChanged() {

        final GrowthlaneTrackingILP ilp = model.getCurrentGL().getIlp();

        // IF 'COUNTING VIEW' VIEW IS ACTIVE
        // =================================
        if (tabsViews.getComponent(tabsViews.getSelectedIndex()).equals(panelCountingView)) {
            if (ilp != null) {
                panelCountingView.showData(model.getCurrentGL());
            } else {
                panelCountingView.showData(null);
            }
        }

        // IF SEGMENTATION AND ASSIGNMENT VIEW IS ACTIVE
        // =============================================
        if (tabsViews.getComponent(tabsViews.getSelectedIndex()).equals(panelSegmentationAndAssignmentView)) {
            updateIlpVariableEditorPanels();
        }
        requestFocusOnTimeStepSlider();

        this.repaint();
    }

    private void updateIlpVariableEditorPanels() {
        for (IlpVariableEditorPanel entry : ilpVariableEditorPanels) {
            entry.display();
        }
    }

    /**
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    @Override
    public void stateChanged(final ChangeEvent e) {
        if (e.getSource().equals(sliderGL)) {
            model.setCurrentGL(sliderGL.getValue(), panelWithSliders.getTimeStepSliderPosition());
        }
        dataToDisplayChanged();
        requestFocusOnTimeStepSlider();
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource().equals(menuProps)) {
            dialogManager.showPropertiesEditor();
        }
        if (e.getSource().equals(menuLoad)) {

            final MoMAGui self = this;

            final Thread t = new Thread(() -> {
                GrowthlaneTrackingILP ilp = model.getCurrentGL().getIlp();

                final File file = OsDependentFileChooser.showLoadFileChooser(
                        self,
                        glFileManager.getOutputPath().toString(),
                        "Choose tracking to load...",
                        new ExtensionFileFilter("moma", "Curated MoMA tracking"));
                System.out.println("File to load tracking from: " + file.getAbsolutePath());

                try {
                    if (file != null) {
                        if (ilp == null) {
                            prepareOptimization();
                            ilp = model.getCurrentGL().getIlp();
                        }
                        ilp.loadState(file);
                    }
                } catch (final IOException e1) {
                    e1.printStackTrace();
                }
            });
            t.start();
        }
        if (e.getSource().equals(menuSave)) {

            final GrowthlaneTrackingILP ilp = model.getCurrentGL().getIlp();

            if (ilp != null) { // && ilp.getStatus() != GrowthlaneTrackingILP.OPTIMIZATION_NEVER_PERFORMED
                final File file = OsDependentFileChooser.showSaveFileChooser(
                        this,
                        glFileManager.getOutputPath().toString(),
                        "Save current tracking to...",
                        new ExtensionFileFilter("moma", "Curated MOMA tracking"));
                System.out.println("File to save tracking to: " + file.getAbsolutePath());
                ilp.saveState(file);
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Loaded data must be optimized before tracking can be saved!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        if (e.getSource().equals(menuViewShowConsole)) {
            loggerWindow.showConsoleWindow(!loggerWindow.isConsoleVisible());
            guiFrame.setVisible(true);
        }
        if (e.getSource().equals(menuShowImgRaw)) {
            new ImageJ();
            ImageJFunctions.show(imageProvider.getRawChannelImgs().get(0), "raw data (ch.0)");
        }
        if (e.getSource().equals(buttonSet)) {
            final Thread t = new Thread(() -> {
                model.getCurrentGL().getIlp().autosave();

                setAllVariablesFixedWhereChecked();

                System.out.println("Finding optimal result...");
                model.getCurrentGL().getIlp().run();
                System.out.println("...done!");

                requestFocusOnTimeStepSlider();
                dataToDisplayChanged();
            });
            t.start();
        }
        if (e.getSource().equals(buttonReset)) {
            final Thread t = new Thread(() -> {
                model.getCurrentGL().getIlp().autosave();

                removeAllSegmentConstraintsWhereChecked();

                System.out.println("Finding optimal result...");
                model.getCurrentGL().getIlp().run();
                System.out.println("...done!");

                requestFocusOnTimeStepSlider();
                dataToDisplayChanged();
            });
            t.start();
        }
        if (e.getSource().equals(buttonFreezePreviousTimeSteps)) {
            final Thread t = new Thread(() -> {
                final int t1 = panelWithSliders.getTimeStepSliderPosition();
                if (panelWithSliders.getTrackingRangeEnd() < panelWithSliders.getTrackingRangeSliderMaximum()) {
                    final int extent =
                            panelWithSliders.getTrackingRangeEnd() - panelWithSliders.getTrackingRangeStart();
                    panelWithSliders.setTrackingRangeEnd(t1 - 1 + extent);
                    buttonOptimizeMore.doClick();
                }
                panelWithSliders.setTrackingRangeStart(t1 - 1);
            });
            t.start();
        }
        if (e.getSource().equals(buttonRestart)) {
            final int choice =
                    JOptionPane.showConfirmDialog(
                            this,
                            "Do you really want to restart the optimization?\nYou will lose all manual edits performed so far!",
                            "Are you sure?",
                            JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.OK_OPTION) {
                restartTrackingAsync();
            }
        }
        if (e.getSource().equals(buttonOptimizeMore)) {
            final Thread t = new Thread(() -> {
                if (model.getCurrentGL().getIlp() == null) {
                    prepareOptimization();
//                    panelWithSliders.setTrackingRangeStart(model.getCurrentGL().getIlp().getOptimizationRangeStart());
                }

//                if (panelWithSliders.getTimeStepSliderPosition() > panelWithSliders.getTrackingRangeEnd()) {
//                    panelWithSliders.setTrackingRangeEnd(panelWithSliders.getTimeStepSliderPosition());
//                }
//                if (panelWithSliders.getTimeStepSliderPosition() < panelWithSliders.getTrackingRangeStart()) {
//                    int len = panelWithSliders.getTrackingRangeEnd() - panelWithSliders.getTrackingRangeStart();
//                    panelWithSliders.setTrackingRangeStart(panelWithSliders.getTimeStepSliderPosition() - len / 2);
//                    panelWithSliders.setTrackingRangeEnd(panelWithSliders.getTimeStepSliderPosition() + len / 2 + len % 2);
//                }

                System.out.println("Finding optimal result...");
                model.getCurrentGL().getIlp().runImmediately();
                System.out.println("...done!");
                buttonOptimizeMore.setForeground(Color.BLACK);
                dataToDisplayChanged();
            });
            t.start();
        }
        if (e.getSource().equals(buttonExportHtml)) {
            final Thread t = new Thread(this::exportHtmlOverview);
            t.start();
        }
        if (e.getSource().equals(buttonExportData)) {
            Path outputPath = queryUserForOutputPath();
            if (outputPathIsValid(outputPath)) {
                model.getCurrentGL().setOutputPath(outputPath);
                final Thread t = new Thread(() -> this.exportAllData());
                t.start();
            }
        }
        if (e.getSource().equals(buttonSaveTracking)) {
            Path folderToUse = queryUserForOutputPath();
            if (outputPathIsValid(folderToUse)) {
                model.getCurrentGL().setOutputPath(folderToUse);
                final Thread t = new Thread(() -> this.exportMomaInternalData());
                t.start();
            }
        }
        if (e.getSource().equals(buttonSaveTrackingAndExit)) {
            Path folderToUse = queryUserForOutputPath();
            if (outputPathIsValid(folderToUse)) {
                model.getCurrentGL().setOutputPath(folderToUse);
                final Thread t = new Thread(() -> {
                    this.exportMomaInternalData();
                    closeCommand.run();
                });
                t.start();
            }
        }
        requestFocusOnTimeStepSlider();
    }

    private boolean outputPathIsValid(Path folderToUse) {
        return !isNull(folderToUse) && folderToUse.toFile().exists() && folderToUse.toFile().isDirectory();
    }


    public void restartFromGLSegmentation() {
        model.mm.restartFromGLSegmentation();
    }

    /**
     * Queries the user, if he wants to restart the tracking, e.g. after
     * changing a parameter or hitting the restart button.
     */
    public Thread restartTrackingAsync() {
        final Thread t = new Thread(() -> {
            restartTracking();
        });
        t.start();
        return t;
    }

    public void restartTracking() {
        prepareOptimization();

        if (!(panelWithSliders.getTrackingRangeEnd() == panelWithSliders.getTrackingRangeSliderMaximum())) {
            final int extent = panelWithSliders.getTrackingRangeEnd() - panelWithSliders.getTrackingRangeStart();
            panelWithSliders.setTrackingRangeEnd(extent);
        }
        panelWithSliders.setTrackingRangeStart(0);

        model.getCurrentGL().getIlp().addPreOptimizationRangeLockConstraintsBefore(panelWithSliders.getTrackingRangeStart());
        if (panelWithSliders.getTrackingRangeEnd() < panelWithSliders.getTrackingRangeSliderMaximum()) {
            model.getCurrentGL().getIlp().addPostOptimizationRangeLockConstraintsAfter(panelWithSliders.getTrackingRangeEnd());
        }

        System.out.println("Finding optimal result...");
        model.getCurrentGL().getIlp().runImmediately();
        System.out.println("...done!");

        dataToDisplayChanged();
    }

    /**
     * @return
     */
    private void prepareOptimization() {
        System.out.println("Filling in CT hypotheses where needed...");
        for (final GrowthlaneFrame glf : model.getCurrentGL().getFrames()) {
            if (glf.getComponentForest() == null) {
                glf.generateSimpleSegmentationHypotheses();
            }
        }

        System.out.println("Generating ILP...");
        if (configurationManager.getIfRunningHeadless()) {
            model.getCurrentGL().generateILP(null);
        } else {
            model.getCurrentGL().generateILP(
                    new DialogProgress(this, "Building tracking model...", model.getTimeStepMaximumOfCurrentGl()));
        }
    }

    /**
     * Depending on which checkboxes are checked, fix ALL respective
     * segmentations and assignments to current ILP state.
     */
    private void setAllVariablesFixedWhereChecked() {
        for (IlpVariableEditorPanel entry : ilpVariableEditorPanels) {
            entry.setVariableConstraints();
        }
    }

    /**
     * Depending on which checkboxes are UNchecked, free ALL respective
     * segmentations and assignments if they are clamped to any value in the
     * ILP.
     */
    private void removeAllSegmentConstraintsWhereChecked() {
        for (IlpVariableEditorPanel entry : ilpVariableEditorPanels) {
            entry.unsetVariableConstraints();
        }
    }

    /**
     * Export all data.
     */
    public void exportAllData() {
        exportMomaInternalData();
        exportTrackingResults();
    }

    /**
     * Export result data that is used by users for their down-stream analysis of the experiment.
     */
    private void exportTrackingResults() {
        MoMA.dic.getExportTimer().start();
        List<ResultExporterInterface> exporters = new ArrayList<>();
        exporters.add(MoMA.dic.getCellStatsExporter());
        exporters.add(MoMA.dic.getCellMaskExporter());
        exporters.add(MoMA.dic.getHtmlOverviewExporterWrapper());
        if (showGroundTruthExportFunctionality) {
            exporters.add(MoMA.dic.getGroundTruthFramesExporter());
        }
        final ResultExporter resultExporter = new ResultExporter(exporters);
        resultExporter.export(model.getCurrentGL(), model.getCurrentGL().getExportPaths());
        MoMA.dic.getExportTimer().stop();
        MoMA.dic.getExportTimer().printExecutionTime("Timer result for exporting results");
    }

    /**
     * Export data that is used by MoMA such as e.g. the state of the ILP and data used for debugging.
     */
    public void exportMomaInternalData() {
        MoMA.dic.getTrackingDataTimer().start();
        List<ResultExporterInterface> exporters = new ArrayList<>();
        exporters.add(MoMA.dic.getMetaDataExporter());
        exporters.add(MoMA.dic.getIlpModelExporter());
        exporters.add(MoMA.dic.getMMPropertiesExporter());
        if (configurationManager.getIfRunningHeadless()) { /* export debug data only in headless mode, so that the user does not have to wait for this during interactive curation. */
            if (configurationManager.getFilterAssignmentsUsingFluorescenceFeatureFlag()) {
                exporters.add(MoMA.dic.getComponentIntensitiesExporter());
            }
            exporters.add(MoMA.dic.getAssignmentCostExporter());
            exporters.add(MoMA.dic.getAssignmentActivitiesExporter());
            exporters.add(MoMA.dic.getHypothesisActivitiesExporter());
            exporters.add(MoMA.dic.getCurationStatsExporter());
        }
        exporters.add(MoMA.dic.getComponentForestExporter());

        final ResultExporter resultExporter = new ResultExporter(exporters);
        resultExporter.export(model.getCurrentGL(), model.getCurrentGL().getExportPaths());
        MoMA.dic.getTrackingDataTimer().stop();
        MoMA.dic.getTrackingDataTimer().printExecutionTime("Timer result for saving tracking data");
    }

    /**
     * Show file-selection dialog for user to pick the output directory.
     *
     * @return File folder that the user selected.
     */
    @Nullable
    private Path queryUserForOutputPath() {
        if (model.getCurrentGL().getIlp() == null) {
            JOptionPane.showMessageDialog(this, "The current GL can only be exported after being tracked (optimized)!");
            return null;
        }

        Path folderToUse;
        if (!configurationManager.getIfRunningHeadless()) {
            if (!showFitRangeWarningDialogIfNeeded()) return null;

            folderToUse = OsDependentFileChooser.showSaveFolderChooser(this,
                    glFileManager.getOutputPath().toString(),
                    "Choose export folder...").toPath();
            if (folderToUse == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Illegal save location chosen!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
        } else { /* if running headless: use default output path */
            folderToUse = glFileManager.getOutputPath();
        }
        return folderToUse;
    }

    private boolean showFitRangeWarningDialogIfNeeded() {
        final IntervalView<FloatType> channelFrame = Views.hyperSlice(imageProvider.getRawChannelImgs().get(0), 2, 0);

        if (channelFrame.dimension(0) >= configurationManager.INTENSITY_FIT_RANGE_IN_PIXELS)
            return true; /* Image wider then fit range. No need to warn. */

        int userSelection = JOptionPane.showConfirmDialog(null,
                String.format("Intensity fit range (%dpx) exceeds image width (%dpx). Image width will be use instead. Do you want to proceed?", configurationManager.INTENSITY_FIT_RANGE_IN_PIXELS, channelFrame.dimension(0)),
                "Fit Range Warning",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return userSelection == JOptionPane.YES_OPTION;
    }


    /**
     *
     */
    public void exportHtmlOverview() {
        if (model.getCurrentGL().getIlp() == null) {
            JOptionPane.showMessageDialog(this, "The current GL can only be exported after being tracked (optimized)!");
            return;
        }

        boolean doExport = true;
        int startFrame = 1;
        int endFrame = panelWithSliders.getTimeStepSliderMaximum() + 1;

        File file = new File(glFileManager.getOutputPath().toString() + "/index.html");

        if (!configurationManager.getIfRunningHeadless()) {
            final JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(file);
            fc.addChoosableFileFilter(new ExtensionFileFilter(new String[]{"html"}, "HTML-file"));

            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                file = fc.getSelectedFile();
                if (!file.getAbsolutePath().endsWith(".html") && !file.getAbsolutePath().endsWith(".htm")) {
                    file = new File(file.getAbsolutePath() + ".html");
                }

                boolean done = false;
                while (!done) {
                    try {
                        final String str = (String) JOptionPane.showInputDialog(this, "First frame to be exported:", "Start at...", JOptionPane.QUESTION_MESSAGE, null, null, "" + startFrame);
                        if (str == null) return; // User decided to hit cancel!
                        startFrame = Integer.parseInt(str);
                        done = true;
                    } catch (final NumberFormatException nfe) {
                        done = false;
                    }
                }
                done = false;
                while (!done) {
                    try {
                        final String str = (String) JOptionPane.showInputDialog(this, "Last frame to be exported:", "End with...", JOptionPane.QUESTION_MESSAGE, null, null, "" + endFrame);
                        if (str == null) return; // User decided to hit cancel!
                        endFrame = Integer.parseInt(str);
                        done = true;
                    } catch (final NumberFormatException nfe) {
                        done = false;
                    }
                }
            } else {
                doExport = false;
            }
        }

        // ----------------------------------------------------------------------------------------------------
        if (doExport) {
            exportHtmlTrackingOverview(file, startFrame - 1, endFrame - 1);
        }
        // ----------------------------------------------------------------------------------------------------

        if (!configurationManager.getIfRunningHeadless()) {
            dataToDisplayChanged();
        }
    }

    /**
     * Exports current tracking solution as individual PNG images in the given
     * folder.
     *
     * @param endFrame
     * @param startFrame
     */
    private void exportHtmlTrackingOverview(final File htmlFileToSaveTo, final int startFrame, final int endFrame) {
        System.out.println("Exporting html tracking overview...");

        final String path = htmlFileToSaveTo.getParent();
        final String imgpath = path + "/imgs";

        final HtmlOverviewExporterWriter exporter = new HtmlOverviewExporterWriter(this, htmlFileToSaveTo, imgpath, startFrame, endFrame);
        exporter.run();

        System.out.println("...done!");
    }

    /**
     * Requests the focus on the slider controlling the time (frame).
     */
    public void requestFocusOnTimeStepSlider() {
        SwingUtilities.invokeLater(() -> panelWithSliders.requestFocusOnTimeStepSlider());
//        SwingUtilities.invokeLater(() -> panelWithSliders.getTimestepSlider().requestFocusInWindow());
    }

    /**
     * Checkbox getter to enable or disable autosave functionality.
     *
     * @return true if the corresponding CheckBox is checked.
     */
    public boolean isAutosaveRequested() {
        return checkboxAutosave.isSelected();
    }

    public void setCenterTime(int timestep) {
        panelWithSliders.setTimeStepSliderPosition(timestep);
    }

    /**
     * This method is run, when loading a GL curation, in order to perform the optimization of the ILP and initialize
     * MoMA to the previous curation state.
     */
    public void startOptimizationWhenReloadingPreviousCuration() {
        final Thread t = new Thread(() -> {
            if (model.getCurrentGL().getIlp() == null) {
                prepareOptimization();
            }
            System.out.println("Loading previous tracking state...");
            GrowthlaneTrackingILP ilp = model.getCurrentGL().getIlp();
            ilp.runImmediately();
            if (configurationManager.getIsReloading()) {
                try {
                    ilp.loadPruneRoots(glFileManager.getDotMomaFilePath().toFile());
                } catch (IOException e) {
                    throw new RuntimeException("Error: Could load prune-roots from file: " + glFileManager.getDotMomaFilePath(), e);
                }
            }
            System.out.println("...done!");
            buttonOptimizeMore.setForeground(Color.BLACK);
            dataToDisplayChanged();
            MoMA.dic.getLoadingTimer().stop();
            MoMA.dic.getLoadingTimer().printExecutionTime("Timer result for loading GL");
        });
        t.start();
    }

    public GrowthlaneViewer getCenteredGrowthLaneViewer() {
        return centeredGrowthLaneViewer;
    }
}
