package com.jug.gui;

import com.jug.Growthlane;
import com.jug.GrowthlaneFrame;
import com.jug.MoMA;
import com.jug.config.ConfigurationManager;
import com.jug.datahandling.IImageProvider;
import com.jug.export.*;
import com.jug.gui.assignmentview.AssignmentsEditorViewer;
import com.jug.gui.progress.DialogProgress;
import com.jug.gui.slider.RangeSlider;
import com.jug.lp.*;
import com.jug.util.ComponentTreeUtils;
import com.jug.util.Util;
import com.jug.util.componenttree.AdvancedComponent;
import ij.ImageJ;
import net.imglib2.Localizable;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import net.miginfocom.swing.MigLayout;
import org.math.plot.Plot2DPanel;
import weka.gui.ExtensionFileFilter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author jug
 */
public class MoMAGui extends JPanel implements ChangeListener, ActionListener {

    private static final long serialVersionUID = -1008974839249784873L;

    // -------------------------------------------------------------------------------------
    // fields
    // -------------------------------------------------------------------------------------
    public final MoMAModel model;
    private final DialogPropertiesEditor propsEditor;
    private final String itemChannel0 = "Channel 0";
    private final String itemChannel1 = "Channel 1";
    private final String itemChannel2 = "Channel 2";
    private final List<IlpVariableEditorPanel> ilpVariableEditorPanels = new ArrayList<>();
    private final List<AssignmentEditorPanel> assignmentEditorPanels = new ArrayList<>();
    private final List<SegmentationEditorPanel> segmentationEditorPanels = new ArrayList<>();
    private final boolean showGroundTruthExportFunctionality;
    private final IImageProvider imageProvider;
    private final MoMA momaInstance;
    public JSlider sliderGL;
    public JSlider sliderTime;
    // -------------------------------------------------------------------------------------
    // gui-fields
    // -------------------------------------------------------------------------------------
    public GrowthlaneViewer growthLaneViewerCenter;
    public AssignmentsEditorViewer assignmentsEditorViewerUsedForHtmlExport;
    private SegmentationEditorPanel segmentationEditorPanelCenter;
    // show helper lines in IntervalViews?
    private boolean showSegmentationAnnotations = true;
    private RangeSlider sliderTrackingRange;
    private JLabel labelCurrentTime;
    private JTabbedPane tabsViews;
    private CountOverviewPanel panelCountingView;
    private JScrollPane panelSegmentationAndAssignmentView;
    private Plot2DPanel plot;
    private JCheckBox checkboxAutosave;

    private JButton buttonRestart;
    private JButton buttonOptimizeMore;
    private JButton buttonExportHtml;
    private JButton buttonExportData;

    private JComboBox comboboxWhichImgToShow;

    private JButton buttonFreezePreviousTimeSteps;
    private JButton buttonSet;
    private JButton buttonReset;

    // Menu-items
    private MenuItem menuViewShowConsole;
    private MenuItem menuShowImgRaw;

    private MenuItem menuProps;
    private MenuItem menuLoad;
    private MenuItem menuSave;

    private MenuItem menuSaveFG;

    // -------------------------------------------------------------------------------------
    // construction & gui creation
    // -------------------------------------------------------------------------------------

    /**
     * Construction
     *
     * @param mmm the MotherMachineModel to show
     */
    public MoMAGui(final MoMAModel mmm, IImageProvider imageProvider, MoMA momaInstance, boolean showGroundTruthExportFunctionality) {
        super(new BorderLayout());

        this.model = mmm;
        this.imageProvider = imageProvider;
        this.momaInstance = momaInstance;
        this.showGroundTruthExportFunctionality = showGroundTruthExportFunctionality;

        propsEditor = new DialogPropertiesEditor(this, MoMA.props, MoMA.dic.getAssignmentPlausibilityTester());

        buildGui();
        dataToDisplayChanged();
        focusOnSliderTime();
    }

    /**
     * Builds the GUI.
     */
    private void buildGui() {

        final MenuBar menuBar = new MenuBar();
        final Menu menuFile = new Menu("File");
        menuProps = new MenuItem("Preferences...");
        menuProps.addActionListener(this);
        menuLoad = new MenuItem("Load tracking...");
        menuLoad.addActionListener(this);
        menuSave = new MenuItem("Save tracking...");
        menuSave.addActionListener(this);
        menuSaveFG = new MenuItem("Save FG...");
        menuSaveFG.addActionListener(this);
        menuFile.add(menuProps);
        menuFile.addSeparator();
        menuFile.add(menuLoad);
        menuFile.add(menuSave);
        menuFile.addSeparator();
        menuFile.add(menuSaveFG);
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
        if (!MoMA.HEADLESS) {
            MoMA.getGuiFrame().setMenuBar(menuBar);
        }

        final JPanel panelContent = new JPanel(new BorderLayout());
        JPanel panelVerticalHelper;
        JPanel panelHorizontalHelper;

        // --- Slider for time and GL -------------

        sliderTime = new JSlider(SwingConstants.HORIZONTAL, 0, model.getCurrentGL().size() - 1, 0);
        model.setCurrentGLF(sliderTime.getValue());
        sliderTime.addChangeListener(this);
        if (sliderTime.getMaximum() < 200) {
            sliderTime.setMajorTickSpacing(10);
            sliderTime.setMinorTickSpacing(2);
        } else {
            sliderTime.setMajorTickSpacing(100);
            sliderTime.setMinorTickSpacing(10);
        }
        sliderTime.setPaintTicks(true);
        sliderTime.setPaintLabels(true);
        sliderTime.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));
        labelCurrentTime = new JLabel(String.format(" t = %4d", sliderTime.getValue()));

        // --- Slider for TrackingRage ----------

        int max = model.getCurrentGL().size() - 2;
        if (MoMA.getInitialOptimizationRange() != -1) {
            max = Math.min(MoMA.getInitialOptimizationRange(), model.getCurrentGL().size() - 2);
        }
        sliderTrackingRange =
                new RangeSlider(0, model.getCurrentGL().size() - 2);
        sliderTrackingRange.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 7));
        sliderTrackingRange.setValue(0);
        if (ConfigurationManager.OPTIMISATION_INTERVAL_LENGTH >= 0) {
            sliderTrackingRange.setUpperValue(ConfigurationManager.OPTIMISATION_INTERVAL_LENGTH);
        } else {
            sliderTrackingRange.setUpperValue(max);
        }
        sliderTrackingRange.addChangeListener(this);
        final JLabel lblIgnoreBeyond =
                new JLabel(String.format("opt. range:", sliderTrackingRange.getValue()));
        lblIgnoreBeyond.setToolTipText("correct up to left slider / ignore data beyond right slider");

        // --- Assemble sliders -----------------
        final JPanel panelSliderArrangement =
                new JPanel(new MigLayout("wrap 2", "[]3[grow,fill]", "[]0[]"));
        panelSliderArrangement.add(lblIgnoreBeyond);
        panelSliderArrangement.add(sliderTrackingRange);
        panelSliderArrangement.add(labelCurrentTime);
        panelSliderArrangement.add(sliderTime);

        panelHorizontalHelper = new JPanel(new BorderLayout());
        panelHorizontalHelper.add(panelSliderArrangement, BorderLayout.CENTER);
        panelContent.add(panelHorizontalHelper, BorderLayout.SOUTH);

        // Does not exist any more...
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
                if (!MoMA.GUI_OPTIMIZE_ON_ILP_CHANGE) {
                    buttonOptimizeMore.setForeground(Color.RED);
                }
            });
        }

        buttonExportHtml = new JButton("Export HTML");
        buttonExportHtml.addActionListener(this);
        buttonExportData = new JButton("Export Data");
        buttonExportData.addActionListener(this);
        panelHorizontalHelper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panelHorizontalHelper.setBorder(BorderFactory.createEmptyBorder(3, 0, 5, 0));
        panelHorizontalHelper.add(checkboxAutosave);
//		panelHorizontalHelper.add( btnRedoAllHypotheses );
        panelHorizontalHelper.add(buttonRestart);
        panelHorizontalHelper.add(buttonOptimizeMore);
        panelHorizontalHelper.add(buttonExportHtml);
        panelHorizontalHelper.add(buttonExportData);
        add(panelHorizontalHelper, BorderLayout.SOUTH);

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
        actionMap.put("ESCAPE", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                sliderTime.requestFocus();
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
                    sliderTime.requestFocus();
                    dataToDisplayChanged();
                }
                if (e.getActionCommand().equals("g")) {
                    sliderTime.setValue(sliderTrackingRange.getUpperValue());
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
        if (hoveredAssignmentEditorPanel != null) {
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
        if (imageProvider.getRawChannelImgs().size() > 1) {
            comboboxWhichImgToShow.addItem(itemChannel1);
        }
        if (imageProvider.getRawChannelImgs().size() > 2) {
            comboboxWhichImgToShow.addItem(itemChannel2);
        }

        comboboxWhichImgToShow.addActionListener(e -> {
            setColorChannelOnSegmentEditorPanels();
            dataToDisplayChanged();
        });

        int viewHeight = (int) imageProvider.getImgRaw().dimension(1);
        int viewWidth = ConfigurationManager.GL_WIDTH_IN_PIXELS + 2 * ConfigurationManager.GL_PIXEL_PADDING_IN_VIEWS;

        LabelEditorDialog labelEditorDialog = new LabelEditorDialog(this, ConfigurationManager.CELL_LABEL_LIST);

        int min_time_offset = -ConfigurationManager.GUI_NUMBER_OF_SHOWN_TIMESTEPS / 2;
        int max_time_offset = ConfigurationManager.GUI_NUMBER_OF_SHOWN_TIMESTEPS / 2;
        for (int time_offset = min_time_offset; time_offset < max_time_offset; time_offset++) {
            SegmentationEditorPanel segmentationEditorPanel = new SegmentationEditorPanel(this, model, imageProvider, labelEditorDialog, viewWidth, viewHeight, time_offset, showGroundTruthExportFunctionality, MoMA.dic.getGroundTruthFramesExporter());
            panel1.add(segmentationEditorPanel, gridBagConstraintPanel1);
            ilpVariableEditorPanels.add(segmentationEditorPanel);
            segmentationEditorPanels.add(segmentationEditorPanel);

            AssignmentEditorPanel assignmentEditorPanel = new AssignmentEditorPanel(this, model, viewHeight, time_offset);
            panel1.add(assignmentEditorPanel, gridBagConstraintPanel1);
            ilpVariableEditorPanels.add(assignmentEditorPanel);
            assignmentEditorPanels.add(assignmentEditorPanel);

            if (time_offset == 0) {
                growthLaneViewerCenter = segmentationEditorPanel.getGrowthlaneViewer();
                segmentationEditorPanelCenter = segmentationEditorPanel;
                assignmentsEditorViewerUsedForHtmlExport = assignmentEditorPanel.getAssignmentViewerPanel();
            }
        }
        IlpVariableEditorPanel segmentationEditorPanel = new SegmentationEditorPanel(this, model, imageProvider, labelEditorDialog, viewWidth, viewHeight, max_time_offset, showGroundTruthExportFunctionality, MoMA.dic.getGroundTruthFramesExporter());
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
        checkboxOptimizeOnIlpChange.setSelected(MoMA.GUI_OPTIMIZE_ON_ILP_CHANGE);
        checkboxOptimizeOnIlpChange.setText("Run optimization on change");
        checkboxOptimizeOnIlpChange.addActionListener(e -> {
            if (checkboxOptimizeOnIlpChange.isSelected()) {
                MoMA.GUI_OPTIMIZE_ON_ILP_CHANGE = true;
                JOptionPane.showMessageDialog(this, "Optimization will now run automatically after each change. It is suggested to run optimization once now before continuing by pressing the button 'Optimize'.");
                return;
            }
            MoMA.GUI_OPTIMIZE_ON_ILP_CHANGE = false;
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
     * @return
     */
    private JPanel buildDetailedDataView() {
        final JPanel panelDataView = new JPanel(new BorderLayout());

        plot = new Plot2DPanel();
        updatePlotPanels();
        plot.setPreferredSize(new Dimension(500, 500));
        panelDataView.add(plot, BorderLayout.CENTER);

        return panelDataView;
    }

    /**
     * Removes all plots from the plot panel and adds new ones showing the data
     * corresponding to the current slider setting.
     */
    private void updatePlotPanels() {

        final GrowthlaneTrackingILP ilp = model.getCurrentGL().getIlp();

        // Intensity plot
        // --------------
        plot.removeAllPlots();

        plot.setFixedBounds(1, 0.0, 1.0);

        // ComponentTreeNodes
        // ------------------
//        dumpCosts(model.getCurrentGLF().getComponentTree(), ilp);
        if (ilp != null) {
            printCosts(model.getCurrentGLF().getComponentTree(), ilp, "Segment");
            printCosts(model.getCurrentGLF().getComponentTree(), ilp, "ExitAssignment");
            printCosts(model.getCurrentGLF().getComponentTree(), ilp, "MappingAssignment");
            printCosts(model.getCurrentGLF().getComponentTree(), ilp, "DivisionAssignment");
        }
    }

    private <C extends Component<FloatType, C>> void printCosts(final ComponentForest<C> ct, final GrowthlaneTrackingILP ilp, String costType) {
        final int t = sliderTime.getValue();
        System.out.print("##################### PRINTING ALL COSTS AT TIME " + t + " FOR: " + costType + " #####################");
        for (final C root : ct.roots()) {
            System.out.println();
            ArrayList<C> ctnLevel = new ArrayList<>();
            ctnLevel.add(root);
            while (ctnLevel.size() > 0) {
                for (final Component<?, ?> ctn : ctnLevel) {
                    if (costType.equals("Segment")) {
                        System.out.print(String.format("%8.4f;\t", ilp.getComponentCost(t, ctn)));
                    } else {
                        List<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> assignments = ilp.getNodes().getAssignmentsAt(t);
                        for (AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> ass : assignments) {
                            if (costType.equals("ExitAssignment")) {
                                if (ass instanceof ExitAssignment)
                                    System.out.print(String.format("%8.4f;\t", ass.getCost()));
                            } else if (costType.equals("MappingAssignment")) {
                                if (ass instanceof MappingAssignment)
                                    System.out.print(String.format("%8.4f;\t", ass.getCost()));
                            } else if (costType.equals("DivisionAssignment")) {
                                if (ass instanceof DivisionAssignment)
                                    System.out.print(String.format("%8.4f;\t", ass.getCost()));
                            }
                        }
                    }
                }
                ctnLevel = ComponentTreeUtils.getAllChildren(ctnLevel);
                System.out.println();
            }
        }
        System.out.print("##################### STOP PRINTING COSTS: " + costType + " #####################");
        System.out.println();
    }

    private <C extends Component<FloatType, C>> void dumpCosts(final ComponentForest<C> ct, final GrowthlaneTrackingILP ilp) {
        final int numCTNs = ComponentTreeUtils.countNodes(ct);
        final float[][] xydxdyCTNBorders = new float[numCTNs][4];
        final int t = sliderTime.getValue();

        int i = 0;
        for (final C root : ct.roots()) {
            System.out.println();
            int level = 0;
            ArrayList<C> ctnLevel = new ArrayList<>();
            ctnLevel.add(root);
            while (ctnLevel.size() > 0) {
                for (final Component<?, ?> ctn : ctnLevel) {
                    addBoxAtIndex(i, ctn, xydxdyCTNBorders, level);
                    if (ilp != null) {
                        System.out.print(String.format(
                                "%8.4f;\t",
                                ilp.getComponentCost(t, ctn)));
                    }
                    i++;
                }
                ctnLevel = ComponentTreeUtils.getAllChildren(ctnLevel);
                level++;
                System.out.println();
            }
        }
        plot.addBoxPlot("Seg. Hypothesis", new Color(127, 127, 127, 255), Util.makeDoubleArray2d(xydxdyCTNBorders));

        // Plot the segments, which are part of the optimal solution
        if (ilp != null) {
            if (ilp.getOptimalSegmentation(t).size() > 0) {
                final float[][] xydxdyCTNBordersActive = new float[ilp.getOptimalSegmentation(t).size()][4];
                i = 0;
                for (final Hypothesis<AdvancedComponent<FloatType>> hyp : ilp.getOptimalSegmentation(t)) {
                    final AdvancedComponent<FloatType> ctn = hyp.getWrappedComponent();
                    addBoxAtIndex(i, ctn, xydxdyCTNBordersActive, ComponentTreeUtils.getLevelInTree(ctn));
                    i++;
                }
                plot.addBoxPlot("Active Seg. Hypothesis", new Color(255, 0, 0, 255), Util.makeDoubleArray2d(xydxdyCTNBordersActive));
            }
        }
    }

    /**
     * @param index
     * @param ctn
     * @param boxDataArray
     * @param level
     */
    @SuppressWarnings("unchecked")
    private void addBoxAtIndex(final int index, final Component<?, ?> ctn, final float[][] boxDataArray, final int level) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        Iterator<Localizable> componentIterator = ctn.iterator();
        while (componentIterator.hasNext()) { // MM-2019-07-01: Here we determine the y-boundaries of the component for drawing
            final int pos = componentIterator.next().getIntPosition(1);
            min = Math.min(min, pos);
            max = Math.max(max, pos);
        }
        final int leftLocation = min;
        final int rightLocation = max;
        boxDataArray[index] = new float[]{0.5f * (leftLocation + rightLocation) + 1, 1.0f - level * 0.05f - 0.02f, rightLocation - leftLocation, 0.02f};
    }

    // -------------------------------------------------------------------------------------
    // getters and setters
    // -------------------------------------------------------------------------------------

    // -------------------------------------------------------------------------------------
    // methods
    // -------------------------------------------------------------------------------------

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
        setFocusToTimeSlider();
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
            model.setCurrentGL(sliderGL.getValue(), sliderTime.getValue());
        }

        if (e.getSource().equals(sliderTime)) {
             updateCenteredTimeStep();
             if(spaceBarIsBeingHeld.isActive()){
                 segmentationEditorPanelCenter.toggleGroundTruthSelectionCheckbox();
             }
        }

        if (e.getSource().equals(sliderTrackingRange)) {
            if (model.getCurrentGL().getIlp() != null) {
                model.getCurrentGL().getIlp().ignoreBeyond(sliderTrackingRange.getUpperValue());
            }
        }

        dataToDisplayChanged();
        this.repaint();

        focusOnSliderTime();
    }

    /**
     *
     */
    private void updateCenteredTimeStep() {
        this.labelCurrentTime.setText(String.format(" t = %4d", sliderTime.getValue()));
        this.model.setCurrentGLF(sliderTime.getValue());
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        if (e.getSource().equals(menuProps)) {
            propsEditor.setVisible(true);
        }
        if (e.getSource().equals(menuLoad)) {

            final MoMAGui self = this;

            final Thread t = new Thread(() -> {
                GrowthlaneTrackingILP ilp = model.getCurrentGL().getIlp();

                final File file = OsDependentFileChooser.showLoadFileChooser(
                        self,
                        MoMA.STATS_OUTPUT_PATH,
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
                        MoMA.STATS_OUTPUT_PATH,
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
            momaInstance.showConsoleWindow(!momaInstance.isConsoleVisible());
            MoMA.getGuiFrame().setVisible(true);
        }
        if (e.getSource().equals(menuShowImgRaw)) {
            new ImageJ();
            ImageJFunctions.show(imageProvider.getRawChannelImgs().get(0), "raw data (ch.0)");
        }
        if (e.getSource().equals(menuSaveFG)) {
            final File file = OsDependentFileChooser.showSaveFileChooser(
                    this,
                    MoMA.DEFAULT_PATH,
                    "Save Factor Graph...",
                    new ExtensionFileFilter(new String[]{"txt", "TXT"}, "TXT-file"));

            if (file != null) {
                MoMA.DEFAULT_PATH = file.getParent();

                if (model.getCurrentGL().getIlp() == null) {
                    System.out.println("Generating ILP...");
                    model.getCurrentGL().generateILP(
                            new DialogProgress(this, "Building tracking model...", (model.getCurrentGL().size() - 1) * 2));
                } else {
                    System.out.println("Using existing ILP (possibly containing user-defined ground-truth bits)...");
                }
                System.out.println("Saving ILP as FactorGraph...");
                new FactorGraphExporter(model.getCurrentGL()).exportFG_PAUL(file);
                System.out.println("...done!");
            }
        }
        if (e.getSource().equals(buttonSet)) {
            final Thread t = new Thread(() -> {
                model.getCurrentGL().getIlp().autosave();

                setAllVariablesFixedWhereChecked();

                System.out.println("Finding optimal result...");
                model.getCurrentGL().getIlp().run();
                System.out.println("...done!");

                sliderTime.requestFocus();
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

                sliderTime.requestFocus();
                dataToDisplayChanged();
            });
            t.start();
        }
        if (e.getSource().equals(buttonFreezePreviousTimeSteps)) {
            final Thread t = new Thread(() -> {
                final int t1 = sliderTime.getValue();
                if (sliderTrackingRange.getUpperValue() < sliderTrackingRange.getMaximum()) {
                    final int extent =
                            sliderTrackingRange.getUpperValue() - sliderTrackingRange.getValue();
                    sliderTrackingRange.setUpperValue(t1 - 1 + extent);
                    buttonOptimizeMore.doClick();
                }
                sliderTrackingRange.setValue(t1 - 1);
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
                    sliderTrackingRange.setValue(0);
                }

                if (sliderTime.getValue() > sliderTrackingRange.getUpperValue()) {
                    sliderTrackingRange.setUpperValue(sliderTime.getValue());
                }
                if (sliderTime.getValue() < sliderTrackingRange.getValue()) {
                    final int len =
                            sliderTrackingRange.getUpperValue() - sliderTrackingRange.getValue();
                    sliderTrackingRange.setValue(sliderTime.getValue() - len / 2);
                    sliderTrackingRange.setUpperValue(sliderTime.getValue() + len / 2 + len % 2);
                }

                model.getCurrentGL().getIlp().freezeBefore(sliderTrackingRange.getValue());
                if (sliderTrackingRange.getUpperValue() < sliderTrackingRange.getMaximum()) {
                    // this is needed because of the duplication of the last time-point
                    model.getCurrentGL().getIlp().ignoreBeyond(sliderTrackingRange.getUpperValue());
                }

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
            final Thread t = new Thread(this::exportDataFiles);
            t.start();
        }
        setFocusToTimeSlider();
    }


    public void restartFromGLSegmentation() {
        model.mm.restartFromGLSegmentation(imageProvider);
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

        if (!(sliderTrackingRange.getUpperValue() == sliderTrackingRange.getMaximum())) {
            final int extent =
                    sliderTrackingRange.getUpperValue() - sliderTrackingRange.getValue();
            sliderTrackingRange.setUpperValue(extent);
        }
        sliderTrackingRange.setValue(0);

        model.getCurrentGL().getIlp().freezeBefore(sliderTrackingRange.getValue());
        if (sliderTrackingRange.getUpperValue() < sliderTrackingRange.getMaximum()) {
            // this is needed because of the duplication of the last time-point
            model.getCurrentGL().getIlp().ignoreBeyond(sliderTrackingRange.getUpperValue());
        }

        System.out.println("Finding optimal result...");
        model.getCurrentGL().getIlp().runImmediately();
        System.out.println("...done!");

        dataToDisplayChanged();
    }

    private void setFocusToTimeSlider() {

        SwingUtilities.invokeLater(() -> sliderTime.requestFocusInWindow());
    }

    /**
     * @return
     */
    private void prepareOptimization() {
        System.out.println("Filling in CT hypotheses where needed...");
        int frameIndex = 0;
        for (final GrowthlaneFrame glf : model.getCurrentGL().getFrames()) {
            if (glf.getComponentTree() == null) {
                glf.generateSimpleSegmentationHypotheses(imageProvider, frameIndex);
                frameIndex++;
            }
        }

        System.out.println("Generating ILP...");
        if (MoMA.HEADLESS) {
            model.getCurrentGL().generateILP(null);
        } else {
            model.getCurrentGL().generateILP(
                    new DialogProgress(this, "Building tracking model...", (model.getCurrentGL().size() - 1) * 2));
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
     *
     */
    public void exportDataFiles() {
        if (model.getCurrentGL().getIlp() == null) {
            JOptionPane.showMessageDialog(this, "The current GL can only be exported after being tracked (optimized)!");
            return;
        }

        File folderToUse;
        if (!MoMA.HEADLESS) {
            if (!showFitRangeWarningDialogIfNeeded()) return;

            folderToUse = OsDependentFileChooser.showSaveFolderChooser(this, MoMA.STATS_OUTPUT_PATH, "Choose export folder...");
            if (folderToUse == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Illegal save location chosen!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else { /* if running headless: use default output path */
            folderToUse = new File(MoMA.STATS_OUTPUT_PATH);
        }

        final CellStatsExporter cellStatsExporter = new CellStatsExporter(this, MoMA.dic.getConfigurationManager(), MoMA.dic.getMixtureModelFit(), MoMA.dic.getComponentProperties(), MoMA.dic.getMomaInstance());
        final CellMaskExporter cellMaskExporter = new CellMaskExporter(MoMA.dic.getImglib2utils(), MoMA.getDefaultFilenameDecoration());

        List<ResultExporterInterface> exporters;
        if (showGroundTruthExportFunctionality) {
            exporters = Arrays.asList(cellStatsExporter, cellMaskExporter, MoMA.dic.getGroundTruthFramesExporter());
        } else {
            exporters = Arrays.asList(cellStatsExporter, cellMaskExporter);
        }

        final ResultExporter resultExporter = new ResultExporter(exporters);
        resultExporter.export(folderToUse, this.sliderTime.getMaximum(), this.model.getCurrentGL().getFrames().get(0));
    }

    private boolean showFitRangeWarningDialogIfNeeded() {
        final IntervalView<FloatType> channelFrame = Views.hyperSlice(imageProvider.getRawChannelImgs().get(0), 2, 0);

        if (channelFrame.dimension(0) >= ConfigurationManager.INTENSITY_FIT_RANGE_IN_PIXELS)
            return true; /* Image wider then fit range. No need to warn. */

        int userSelection = JOptionPane.showConfirmDialog(null,
                String.format("Intensity fit range (%dpx) exceeds image width (%dpx). Image width will be use instead. Do you want to proceed?", ConfigurationManager.INTENSITY_FIT_RANGE_IN_PIXELS, channelFrame.dimension(0)),
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
        int endFrame = sliderTime.getMaximum() + 1;

        File file = new File(MoMA.STATS_OUTPUT_PATH + "/index.html");

        if (!MoMA.HEADLESS) {
            final JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(file);
            fc.addChoosableFileFilter(new ExtensionFileFilter(new String[]{"html"}, "HTML-file"));

            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                file = fc.getSelectedFile();
                if (!file.getAbsolutePath().endsWith(".html") && !file.getAbsolutePath().endsWith(".htm")) {
                    file = new File(file.getAbsolutePath() + ".html");
                }
                MoMA.STATS_OUTPUT_PATH = file.getParent();

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

        if (!MoMA.HEADLESS) {
            dataToDisplayChanged();
        }
    }

    /**
     * Goes over all glfs of the current gl and activates the simple, intensity
     * + comp.tree hypotheses.
     */
    private void activateSimpleHypotheses() {
        activateSimpleHypothesesForGL(model.getCurrentGL());
    }

    private void activateSimpleHypothesesForGL(final Growthlane gl) {
        int frameIndex = 0;
        for (final GrowthlaneFrame glf : gl.getFrames()) {
            System.out.print(".");
            glf.generateSimpleSegmentationHypotheses(imageProvider, frameIndex);
            frameIndex++;
        }
        System.out.println();
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

        final HtmlOverviewExporter exporter = new HtmlOverviewExporter(this, htmlFileToSaveTo, imgpath, startFrame, endFrame);
        exporter.run();

        System.out.println("...done!");

    }

    /**
     * Requests the focus on the slider controlling the time (frame).
     */
    public void focusOnSliderTime() {

        SwingUtilities.invokeLater(() -> sliderTime.requestFocus());
    }

    /**
     * Checkbox getter to enable or disable autosave functionality.
     *
     * @return true if the corresponding CheckBox is checked.
     */
    public boolean isAutosaveRequested() {
        return checkboxAutosave.isSelected();
    }
}
