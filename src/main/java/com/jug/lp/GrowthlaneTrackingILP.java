package com.jug.lp;

import com.jug.Growthlane;
import com.jug.GrowthlaneFrame;
import com.jug.MoMA;
import com.jug.config.IConfiguration;
import com.jug.exceptions.IlpSetupException;
import com.jug.gui.IDialogManager;
import com.jug.gui.progress.IDialogGurobiProgress;
import com.jug.gui.progress.ProgressListener;
import com.jug.lp.GRBModel.IGRBModelAdapter;
import com.jug.lp.costs.CostFactory;
import com.jug.util.ComponentTreeUtils;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.AdvancedComponentForest;
import com.jug.util.componenttree.ComponentInterface;
import gurobi.*;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.jug.development.featureflags.FeatureFlags.featureFlagUseAssignmentPlausibilityFilter;
import static com.jug.util.ComponentTreeUtils.*;
import static java.util.Objects.isNull;

/**
 * @author jug
 */
@SuppressWarnings("restriction")
public class GrowthlaneTrackingILP {

    // < H extends Hypothesis< Component< FloatType, ? > >, A extends AbstractAssignment< H > >

    public static final int ASSIGNMENT_EXIT = 0;
    public static final int ASSIGNMENT_MAPPING = 1;
    public static final int ASSIGNMENT_DIVISION = 2;
    public static final int ASSIGNMENT_LYSIS = 3;

    private JFrame guiFrame;
    // -------------------------------------------------------------------------------------
    // fields
    // -------------------------------------------------------------------------------------
    private final Growthlane gl;
    private final AssignmentsAndHypotheses<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>, Hypothesis<AdvancedComponent<FloatType>>> nodes =
            new AssignmentsAndHypotheses<>();  // all variables of FG
    public final HypothesisNeighborhoods<Hypothesis<AdvancedComponent<FloatType>>, AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> edgeSets =
            new HypothesisNeighborhoods<>();  // incoming and outgoing assignments per hypothesis
    private final AssignmentPlausibilityTester assignmentPlausibilityTester;
    private final List<ProgressListener> progressListener;
    public IGRBModelAdapter model;
    private String versionString;
    private IConfiguration configurationManager;
    private CostFactory costFactory;
    private boolean isLoadedFromDisk;
    private Supplier<GurobiCallbackAbstract> gurobiCallbackFactory;
    private Supplier<IDialogGurobiProgress> gurobiProgressDialogFactory;
    private IAssignmentFilter assignmentFilter;
    private IlpStatus status = IlpStatus.OPTIMIZATION_NEVER_PERFORMED;
    private IDialogManager dialogManager;
    private boolean removeStorageLockConstraintAfterFirstOptimization;

    // -------------------------------------------------------------------------------------
    // construction
    // -------------------------------------------------------------------------------------
    public GrowthlaneTrackingILP(JFrame guiFrame,
                                 final Growthlane gl,
                                 IGRBModelAdapter grbModel,
                                 AssignmentPlausibilityTester assignmentPlausibilityTester,
                                 IConfiguration configurationManager,
                                 String versionString,
                                 CostFactory costFactory,
                                 boolean isLoadedFromDisk,
                                 Supplier<GurobiCallbackAbstract> gurobiCallbackFactory,
                                 Supplier<IDialogGurobiProgress> gurobiProgressDialogFactory,
                                 IAssignmentFilter assignmentFilter) {
        this.guiFrame = guiFrame;
        this.gl = gl;
        this.model = grbModel;
        this.versionString = versionString;
        this.configurationManager = configurationManager;
        this.costFactory = costFactory;
        this.isLoadedFromDisk = isLoadedFromDisk;
        this.gurobiCallbackFactory = gurobiCallbackFactory;
        this.gurobiProgressDialogFactory = gurobiProgressDialogFactory;
        this.progressListener = new ArrayList<>();
        this.assignmentPlausibilityTester = assignmentPlausibilityTester;
        this.assignmentFilter = assignmentFilter;
    }

    /**
     * Returns only the active assignments in this the data.
     *
     * @param data data to filter and keep only the active assignments
     * @return
     */
    public static HashMap<Hypothesis<AdvancedComponent<FloatType>>, Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>>> getActiveAssignments(final HashMap<Hypothesis<AdvancedComponent<FloatType>>, Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>>> data) {
        HashMap<Hypothesis<AdvancedComponent<FloatType>>, Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>>> activeData = new HashMap<>();
        if (data != null) {
            for (final Hypothesis<AdvancedComponent<FloatType>> hypo : data.keySet()) {
                final Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> activeSet = new HashSet<>();
                for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> ass : data.get(hypo)) {
                    try {
                        if (ass.isChosen() || ass.isGroundTruth()) {
                            activeSet.add(ass);
                        }
                    } catch (final GRBException e) {
                        e.printStackTrace();
                    }
                    activeData.put(hypo, activeSet);
                }
            }
        }
        return activeData;
    }

    /**
     * Returns the assignments in {@param data}, which fulfill the condition defined in {@param predicate}.
     *
     * @param data      data from which to get the assignments of correct type
     * @param predicate predicate that the assignment must fulfill in order to be returned
     * @return correct assignment types or null
     */
    public static HashMap<Hypothesis<AdvancedComponent<FloatType>>, Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>>> filterAssignmentsWithPredicate(final HashMap<Hypothesis<AdvancedComponent<FloatType>>, Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>>> data, Function<AbstractAssignment, Boolean> predicate) {
        HashMap<Hypothesis<AdvancedComponent<FloatType>>, Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>>> activeData = new HashMap<>();
        if (data != null) {
            for (final Hypothesis<AdvancedComponent<FloatType>> hypo : data.keySet()) {
                final Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> activeSet = new HashSet<>();
                for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> ass : data.get(hypo)) {
                    if (predicate.apply(ass)) {
                        activeSet.add(ass);
                    }
                    activeData.put(hypo, activeSet);
                }
            }
        }
        return activeData;
    }

    /**
     * @return the status. This status returns one of the following values:
     * OPTIMIZATION_NEVER_PERFORMED, OPTIMAL, INFEASABLE, UNBOUNDED,
     * SUBOPTIMAL, NUMERIC, or LIMIT_REACHED. Values 2-6 correspond
     * directly to the ones from gurobi, the last one is set when none
     * of the others was actually returned by gurobi.
     * OPTIMIZATION_NEVER_PERFORMED shows, that the optimizer was never
     * started on this ILP setup.
     */
    public IlpStatus getStatus() {
        return status;
    }

    public boolean isReady() {
        return getStatus() != IlpStatus.OPTIMIZATION_IS_RUNNING && getStatus() != IlpStatus.OPTIMIZATION_NEVER_PERFORMED;
    }

    public boolean isRunning() {
        return getStatus() == IlpStatus.OPTIMIZATION_IS_RUNNING;
    }

    public boolean isOptimal() {
        return getStatus() == IlpStatus.OPTIMAL;
    }

    public boolean isOptimizationNotPerformed() {
        return getStatus() == IlpStatus.OPTIMIZATION_NEVER_PERFORMED;
    }

    public boolean isInfeasible() {
        return getStatus() == IlpStatus.INFEASIBLE;
    }

    public AssignmentsAndHypotheses<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>, Hypothesis<AdvancedComponent<FloatType>>> getNodes() {
        return nodes;
    }

    /**
     * Return all components that are part of hypotheses in the ILP.
     *
     * @return
     */
    public List<ComponentInterface> getAllComponentsInIlp() {
        List<ComponentInterface> ret = new ArrayList<>();
        for (Hypothesis<AdvancedComponent<FloatType>> hyp : nodes.getAllHypotheses()) {
            ret.add(hyp.getWrappedComponent());
        }
        return ret;
    }

    public HypothesisNeighborhoods<Hypothesis<AdvancedComponent<FloatType>>, AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> getEdgeSets() {
        return edgeSets;
    }

    List<AdvancedComponent<FloatType>> allComponents = new ArrayList<>();

    public void getAllComponents() {
        for (int t = 0; t < gl.numberOfFrames(); t++) {
            AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> componentForest = gl.getFrames().get(t).getComponentForest();
            allComponents.addAll(componentForest.getAllComponents());
        }
    }

    HashMap<String, AdvancedComponent<FloatType>> componentHashMap = new HashMap<>();

    public void buildComponentHashMap() {
        for (AdvancedComponent<FloatType> component : allComponents) {
            componentHashMap.put(component.getStringId(), component);
        }
    }

    // -------------------------------------------------------------------------------------
    // methods
    // -------------------------------------------------------------------------------------
    public void buildILP() {
        try {
            getAllComponents();
            buildComponentHashMap();

            // add Hypothesis and Assignments
            if (isLoadedFromDisk) {
                MoMA.dic.getAssignmentCreationTimer().start();
                loadAssignments();
                MoMA.dic.getAssignmentCreationTimer().stop();
                MoMA.dic.getAssignmentCreationTimer().printExecutionTime("Timer result for loading assignments");
            } else {
                MoMA.dic.getAssignmentCreationTimer().start();
                createAssignments();
                model.update();
                System.out.println("START: Filter assignments.");
                filterAssignments();
                System.out.println("FINISH: Filter assignments.");

    //            HypothesesAndAssignmentsSanityChecker sanityChecker = new HypothesesAndAssignmentsSanityChecker(gl, nodes, edgeSets);
    //            sanityChecker.checkIfAllComponentsHaveCorrespondingHypothesis();
    //            sanityChecker.checkIfAllComponentsHaveMappingAssignmentsBetweenThem();

                // UPDATE GUROBI-MODEL
                // - - - - - - - - - -
                model.update();

                // Iterate over all assignments and ask them to add their
                // constraints to the model
                // - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                for(final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assignment : nodes.getAllAssignments()){
                    assignment.addConstraintsToILP();
                }
//                for (final List<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> innerList : nodes.getAllAssignments()) {
//                    for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assignment : innerList) {
//                        assignment.addConstraintsToILP();
//                    }
//                }

                // Add the remaining ILP constraints
                // (those would be (i) and (ii) of 'Default Solution')
                // - - - - - - - - - - - - - - - - - - - - - - - - - -
    //            addPathBlockingConstraints();
                addPathBlockingConstraintsNew();
                addContinuityConstraints();
                if(configurationManager.getCrossingConstraintFeatureFlag()){
                    addCrossingConstraints();
                }

                // UPDATE GUROBI-MODEL
                // - - - - - - - - - -
                model.update();
                MoMA.dic.getAssignmentCreationTimer().stop();
                MoMA.dic.getAssignmentCreationTimer().printExecutionTime("Timer result for creating assignments");
            }

            printIlpStatistics();

            /* Set Gurobi model parameters */
            int aggregateVal = model.get(GRB.IntParam.Aggregate);
            System.out.println(String.format("Aggregate old value: %d", aggregateVal));
            model.set(GRB.IntParam.Aggregate, 1);
            aggregateVal = model.get(GRB.IntParam.Aggregate);
            System.out.println(String.format("Aggregate new value: %d", aggregateVal));

            int scaleFlagVal = model.get(GRB.IntParam.ScaleFlag);
            System.out.println(String.format("scaleFlag old value: %d", scaleFlagVal));
            model.set(GRB.IntParam.ScaleFlag, 2);
            scaleFlagVal = model.get(GRB.IntParam.ScaleFlag);
            System.out.println(String.format("scaleFlag new value: %d", scaleFlagVal));

//			int numericFocusVal = model.get(GRB.IntParam.NumericFocus);
//			System.out.println(String.format("numericFocus old value: %d", numericFocusVal));
//			model.set(GRB.IntParam.NumericFocus, 3);
//			numericFocusVal = model.get(GRB.IntParam.NumericFocus);
//			System.out.println(String.format("numericFocus new value: %d", numericFocusVal));

//			int quadVal = model.get(GRB.IntParam.Quad);
//			System.out.println(String.format("Quad old value: %d", quadVal));
//			model.set(GRB.IntParam.Quad, 1);
//			quadVal = model.get(GRB.IntParam.Quad);
//			System.out.println(String.format("Quad new value: %d", quadVal));
//
//			double markowitzTolVal = model.get(GRB.DoubleParam.MarkowitzTol);
//			System.out.println(String.format("MarkowitzTol old value: %f", markowitzTolVal));
//			model.set(GRB.DoubleParam.MarkowitzTol, 0.5);
//			markowitzTolVal = model.get(GRB.DoubleParam.MarkowitzTol);
//			System.out.println(String.format("MarkowitzTol new value: %f", markowitzTolVal));

//			double objScaleVal = model.get(GRB.DoubleParam.ObjScale);
//			System.out.println(String.format("objScale old value: %f", objScaleVal));
//			model.set(GRB.DoubleParam.ObjScale, 0.5);
//			objScaleVal = model.get(GRB.DoubleParam.ObjScale);
//			System.out.println(String.format("objScale new value: %f", objScaleVal));

            model.update();

        } catch (final GRBException e) {
            System.out.println("Could not fill data into GrowthlaneTrackingILP!");
            e.printStackTrace();
        }

    }

    private void filterAssignments() {
        for (AbstractAssignment assignment : nodes.getAllAssignments()) {
            assignmentFilter.evaluate(assignment);
        }
    }

    private int getNumberOfLeafComponents() {
        int counter = 0;
        List<ComponentInterface> componentsInIlp = getAllComponentsInIlp();
        for (ComponentInterface component : componentsInIlp) {
            if (component.getChildren().isEmpty()) {
                counter++;
            }
        }
        return counter;
    }

    private int getNumberOfRootComponents() {
        int counter = 0;
        List<ComponentInterface> componentsInIlp = getAllComponentsInIlp();
        for (ComponentInterface component : componentsInIlp) {
            if (isNull(component.getParent())) {
                counter++;
            }
        }
        return counter;
    }

    private void printIlpStatistics() {
        boolean isTrackOnly = MoMA.dic.getCommandLineArgumentParser().isTrackOnly();
        boolean isHeadless = MoMA.dic.getCommandLineArgumentParser().getIfRunningHeadless();
        System.out.println("########### ILP STATISTICS START ###########");
        System.out.println("Number of all components in component-trees (IsTrackOnly: " + isTrackOnly + ", IsHeadless: " + isHeadless + "): " + allComponents.size());
        System.out.println("Number of components in ILP (IsTrackOnly: " + isTrackOnly + ", IsHeadless: " + isHeadless + "): " + getAllComponentsInIlp().size());
        System.out.println("Number of root components (IsTrackOnly: " + isTrackOnly + ", IsHeadless: " + isHeadless + "): " + getNumberOfRootComponents());
        System.out.println("Number of leaf components (IsTrackOnly: " + isTrackOnly + ", IsHeadless: " + isHeadless + "): " + getNumberOfLeafComponents());
        System.out.println("Number of assignments (IsTrackOnly: " + isTrackOnly + ", IsHeadless: " + isHeadless + "): " + nodes.getTotalNumberOfAssignments());
        System.out.println("########### ILP STATISTICS END ###########");
    }

    /**
     * @throws GRBException
     */
    private void loadAssignments() throws GRBException {
        loadMappingAssignments();
        loadDivisionAssignments();
        loadExitAssignments();
        loadLysisAssignments();
    }

    public void loadMappingAssignments() throws GRBException {
        List<GRBVar> vars = getGrbVariablesContaining("MapT");
        for (GRBVar var : vars) {
            String varName = var.get(GRB.StringAttr.VarName);
            String[] splits = varName.split("_");
            String mapId = splits[0];

            int sourceTimeStep = Integer.parseInt(mapId.substring(4));
            AdvancedComponent<FloatType> sourceComponent = componentHashMap.get(splits[1]);
            if(isNull(sourceComponent)){new RuntimeException("component not found: " + sourceComponent.getStringId());}
            AdvancedComponent<FloatType> targetComponent = componentHashMap.get(splits[2]);
            if(isNull(targetComponent)){new RuntimeException("component not found: " + targetComponent.getStringId());}

            final Hypothesis<AdvancedComponent<FloatType>> from = nodes.getOrAddHypothesis(sourceTimeStep, new Hypothesis<>(sourceTimeStep, sourceComponent, this));
            final Hypothesis<AdvancedComponent<FloatType>> to = nodes.getOrAddHypothesis(sourceTimeStep + 1, new Hypothesis<>(sourceTimeStep + 1, targetComponent, this));
            final MappingAssignment ma = new MappingAssignment(sourceTimeStep, model.getVarByName(varName), this, nodes, edgeSets, from, to);
            nodes.addAssignment(sourceTimeStep, ma);
                if (!edgeSets.addToRightNeighborhood(from, ma)) {
                    throw new RuntimeException(String.format("ERROR: Mapping-assignment could not be added to right neighborhood at time-step: t=%d", sourceTimeStep));
                }
                if (!edgeSets.addToLeftNeighborhood(to, ma)) {
                    throw new RuntimeException(String.format("ERROR: Mapping-assignment could not be added to left neighborhood at time-step: t=%d", sourceTimeStep));
                }
        }
    }

    public void loadDivisionAssignments() throws GRBException {
        List<GRBVar> vars = getGrbVariablesContaining("DivT");
        for (GRBVar var : vars) {
            String varName = var.get(GRB.StringAttr.VarName);
            String[] splits = varName.split("_");
            String mapId = splits[0];

            int sourceTimeStep = Integer.parseInt(mapId.substring(4));
            AdvancedComponent<FloatType> sourceComponent = componentHashMap.get(splits[1]);
            if(isNull(sourceComponent)){new RuntimeException("component not found: " + sourceComponent.getStringId());}
            AdvancedComponent<FloatType> upperTargetComponent = componentHashMap.get(splits[2]);
            if(isNull(upperTargetComponent)){new RuntimeException("component not found: " + upperTargetComponent.getStringId());}
            AdvancedComponent<FloatType> lowerTargetComponent = componentHashMap.get(splits[3]);
            if(isNull(lowerTargetComponent)){new RuntimeException("component not found: " + lowerTargetComponent.getStringId());}

            final Hypothesis<AdvancedComponent<FloatType>> from =
                    nodes.getOrAddHypothesis(sourceTimeStep, new Hypothesis<>(sourceTimeStep, sourceComponent, this));
            final Hypothesis<AdvancedComponent<FloatType>> to =
                    nodes.getOrAddHypothesis(sourceTimeStep + 1, new Hypothesis<>(sourceTimeStep + 1, upperTargetComponent, this));
            final Hypothesis<AdvancedComponent<FloatType>> lowerNeighbor =
                    nodes.getOrAddHypothesis(sourceTimeStep + 1, new Hypothesis<>(sourceTimeStep + 1, lowerTargetComponent, this));

            final DivisionAssignment da = new DivisionAssignment(model.getVarByName(varName), this, from, to, lowerNeighbor, sourceTimeStep);
            nodes.addAssignment(sourceTimeStep, da);
            edgeSets.addToRightNeighborhood(from, da);
            edgeSets.addToLeftNeighborhood(to, da);
            edgeSets.addToLeftNeighborhood(lowerNeighbor, da);
        }
    }

    private void loadExitAssignments() throws GRBException {
        List<GRBVar> vars = getGrbVariablesContaining("ExitT");
        for (GRBVar var : vars) {
            String varName = var.get(GRB.StringAttr.VarName);
            String[] splits = varName.split("_");
            String mapId = splits[0];

            int sourceTimeStep = Integer.parseInt(mapId.substring(5));
            AdvancedComponent<FloatType> sourceComponent = componentHashMap.get(splits[1]);
            if(isNull(sourceComponent)){new RuntimeException("component not found: " + sourceComponent.getStringId());}

            final Hypothesis<AdvancedComponent<FloatType>> hyp =
                    nodes.getOrAddHypothesis(sourceTimeStep, new Hypothesis<>(sourceTimeStep, sourceComponent, this));
            List<Hypothesis<AdvancedComponent<FloatType>>> hyps = nodes.getHypothesesAt(sourceTimeStep);

            final List<Hypothesis<AdvancedComponent<FloatType>>> Hup = LpUtils.getHup(hyp, hyps); /* TODO-MichaelMell-20220908: This could be moved inside ExitAssignment.java */
            final ExitAssignment ea = new ExitAssignment(sourceTimeStep, model.getVarByName(varName), this, nodes, edgeSets, Hup, hyp);
            nodes.addAssignment(sourceTimeStep, ea);
            edgeSets.addToRightNeighborhood(hyp, ea);
        }
    }

    private void loadLysisAssignments() throws GRBException {
        List<GRBVar> vars = getGrbVariablesContaining("LysT");
        for (GRBVar var : vars) {
            String varName = var.get(GRB.StringAttr.VarName);
            String[] splits = varName.split("_");
            String mapId = splits[0];

            int sourceTimeStep = Integer.parseInt(mapId.substring(4));
            AdvancedComponent<FloatType> sourceComponent = componentHashMap.get(splits[1]);
            if(isNull(sourceComponent)){new RuntimeException("component not found: " + sourceComponent.getStringId());}

            final Hypothesis<AdvancedComponent<FloatType>> hyp =
                    nodes.getOrAddHypothesis(sourceTimeStep, new Hypothesis<>(sourceTimeStep, sourceComponent, this));

            final LysisAssignment ea = new LysisAssignment(sourceTimeStep, model.getVarByName(varName), this, hyp);
            nodes.addAssignment(sourceTimeStep, ea);
            edgeSets.addToRightNeighborhood(hyp, ea);
        }
    }

    private List<GRBVar> getGrbVariablesContaining(String substring) {
        GRBVar[] vars = model.getVars();
        List<GRBVar> ret = new ArrayList<>();
        try {
            for (GRBVar var : vars) {
                String varName = var.get(GRB.StringAttr.VarName);
                if (varName.contains(substring)) {
                    ret.add(var);
                }
            }
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
        return ret;
    }

    private boolean modelContainsVarWithName(String targetVarName) {
        GRBVar[] vars = model.getVars();
        try {
            for (GRBVar var : vars) {
                String varName = var.get(GRB.StringAttr.VarName);
                if (varName.contains(targetVarName)) {
                    return true;
                }
            }
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * @throws GRBException
     */
    private void createAssignments() throws GRBException {
//        for (int t = 0; t < gl.size(); t++) {
//            createSegmentationHypotheses( t );
//        }

        for (int t = 0; t < gl.numberOfFrames() - 1; t++) {
            createAssignmentsForTimeStep(t);
        }
        final List<Hypothesis<AdvancedComponent<FloatType>>> curHyps = nodes.getHypothesesAt(gl.numberOfFrames() - 1);
        addExitAssignments(gl.numberOfFrames() - 1, curHyps); /* add exit assignment to last time-step, so we can assign to hypothesis in this time-step, while fulfilling the continuity constraint */
    }

    /**
     * Adds all component-tree-nodes, wrapped in instances of
     * <code>Hypothesis</code> at time-point t
     * This method calls <code>recursivelyAddCTNsAsHypotheses(...)</code>.
     */
    private void createSegmentationHypotheses(final int t) {
        final GrowthlaneFrame glf = gl.getFrames().get(t);

        for (final AdvancedComponent<FloatType> ctRoot : glf.getComponentForest().roots()) {
            recursivelyAddCTNsAsHypotheses(t, ctRoot); //, glf.isParaMaxFlowComponentTree()
        }

        this.reportProgress();
    }

    /**
     * Adds all hypothesis given by the nodes in the component tree to
     * <code>nodes</code>.
     *
     * @param component a node in a <code>ComponentTree</code>.
     * @param t         the time-index the ctNode comes from.
     */
    private void recursivelyAddCTNsAsHypotheses(final int t, final AdvancedComponent<FloatType> component) { //, final boolean isForParaMaxFlowSumImg
        nodes.addHypothesis(t, new Hypothesis<>(t, component, this));
        for (final AdvancedComponent<FloatType> ctChild : component.getChildren()) {
            recursivelyAddCTNsAsHypotheses(t, ctChild);
        }
    }

    /**
     * For time-points t and t+1, enumerates all potentially
     * interesting assignments using the <code>addXXXAsignment(...)</code>
     * methods.
     *
     * @throws GRBException
     */
    private void createAssignmentsForTimeStep(final int sourceTimeStep) throws GRBException {
        int targetTimeStep = sourceTimeStep + 1;
        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> sourceComponentForest = gl.getFrames().get(sourceTimeStep).getComponentForest();
        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> targetComponentForest = gl.getFrames().get(targetTimeStep).getComponentForest();

        addMappingAssignments(sourceTimeStep, sourceComponentForest, targetComponentForest);
        addDivisionAssignments(sourceTimeStep, sourceComponentForest, targetComponentForest);
        addExitAssignments(sourceTimeStep, nodes.getHypothesesAt(sourceTimeStep));
        addLysisAssignments(sourceTimeStep, nodes.getHypothesesAt(sourceTimeStep));
        this.reportProgress();
    }

    /**
     * Add an lysis-assignment at time t to a bunch of segmentation hypotheses.
     *
     * @param sourceTimeStep the time-point.
     * @param hyps           a list of hypothesis for which an <code>ExitAssignment</code>
     *                       should be added.
     * @throws GRBException
     */
    private void addLysisAssignments(final int sourceTimeStep, final List<Hypothesis<AdvancedComponent<FloatType>>> hyps) throws GRBException {
        for (final Hypothesis<AdvancedComponent<FloatType>> hyp : hyps) {
            float cost = configurationManager.getLysisAssignmentCost();
            final GRBVar newLPVar = model.addVar(0.0, 1.0, cost, GRB.BINARY, LysisAssignment.buildStringId(sourceTimeStep, hyp.getWrappedComponent()));
            final LysisAssignment ea = new LysisAssignment(sourceTimeStep, newLPVar, this, hyp);
            nodes.addAssignment(sourceTimeStep, ea);
            edgeSets.addToRightNeighborhood(hyp, ea);
        }
    }

    /**
     * Add an exit-assignment at time t to a bunch of segmentation hypotheses.
     * Note: exit-assignments cost <code>0</code>, but they come with a
     * non-trivial construction to enforce, that an exit-assignment can only be
     * assigned by the solver iff all active segmentation hypotheses above one
     * that has an active exit-assignment are also assigned with an
     * exit-assignment.
     *
     * @param sourceTimeStep the time-point.
     * @param hyps           a list of hypothesis for which an <code>ExitAssignment</code>
     *                       should be added.
     * @throws GRBException
     */
    private void addExitAssignments(final int sourceTimeStep, final List<Hypothesis<AdvancedComponent<FloatType>>> hyps) throws GRBException {
        for (final Hypothesis<AdvancedComponent<FloatType>> hyp : hyps) {
            float cost = costModulationForSubstitutedILP(hyp.getCost());
            final GRBVar newLPVar = model.addVar(0.0, 1.0, cost, GRB.BINARY, ExitAssignment.buildStringId(sourceTimeStep, hyp.getWrappedComponent()));
            final List<Hypothesis<AdvancedComponent<FloatType>>> Hup = LpUtils.getHup(hyp, hyps);
            final ExitAssignment ea = new ExitAssignment(sourceTimeStep, newLPVar, this, nodes, edgeSets, Hup, hyp);
            nodes.addAssignment(sourceTimeStep, ea);
            edgeSets.addToRightNeighborhood(hyp, ea);
        }
    }

    /**
     * Add mapping-assignments between source components in {@param sourceComponentForest} and target components in
     * {@param targetComponentForest}.
     *
     * @param sourceTimeStep        the time-point from which the <code>curHyps</code> originate.
     * @param sourceComponentForest the component tree containing source components of the mapping-assignments.
     * @param targetComponentForest the component tree containing target components of the mapping-assignments.
     * @throws GRBException
     */
    public void addMappingAssignments(final int sourceTimeStep,
                                      AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> sourceComponentForest,
                                      AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> targetComponentForest) throws GRBException {
        for (final AdvancedComponent<FloatType> sourceComponent : sourceComponentForest.getAllComponents()) {
            if (sourceTimeStep > 0) {
                if (!nodes.containsKey(sourceComponent))
                    continue; /* we only want to continue paths of previously existing hypotheses; this is to fulfill the continuity constraint */
            }

            List<AdvancedComponent<FloatType>> targetComponents;
            if (featureFlagUseAssignmentPlausibilityFilter) {
                targetComponents = getPlausibleTargetComponents(sourceComponent, targetComponentForest.getAllComponents(), sourceTimeStep, configurationManager.getMaximumShrinkagePerFrame(), configurationManager.getMaximumGrowthPerFrame());
            } else {
                targetComponents = targetComponentForest.getAllComponents();
            }

            for (final AdvancedComponent<FloatType> targetComponent : targetComponents) {
                if (!assignmentPlausibilityTester.sizeDifferenceIsPlausible(sourceComponent.getMajorAxisLength(), targetComponent.getMajorAxisLength())) {
                    continue;
                }

                if (ComponentTreeUtils.isBelowByMoreThen(sourceComponent, targetComponent, configurationManager.getMaxCellDrop())) {
                    continue;
                }

                final Float compatibilityCostOfMapping = compatibilityCostOfMapping(sourceComponent, targetComponent);
                float cost = costModulationForSubstitutedILP(sourceComponent.getCost(), targetComponent.getCost(), compatibilityCostOfMapping);
//                cost = scaleAssignmentCost(sourceComponent, targetComponent, cost);

                if (cost > configurationManager.getAssignmentCostCutoff()) {
                    continue;
                }

                final Hypothesis<AdvancedComponent<FloatType>> to =
                        nodes.getOrAddHypothesis(sourceTimeStep + 1, new Hypothesis<>(sourceTimeStep + 1, targetComponent, this));
                final Hypothesis<AdvancedComponent<FloatType>> from =
                        nodes.getOrAddHypothesis(sourceTimeStep, new Hypothesis<>(sourceTimeStep, sourceComponent, this));

                final GRBVar newLPVar = model.addVar(0.0, 1.0, cost, GRB.BINARY, MappingAssignment.buildStringId(sourceTimeStep, from.getWrappedComponent(), to.getWrappedComponent()));

                final MappingAssignment ma = new MappingAssignment(sourceTimeStep, newLPVar, this, nodes, edgeSets, from, to);

//                assignmentFilter.evaluate(ma);

                nodes.addAssignment(sourceTimeStep, ma);
                if (!edgeSets.addToRightNeighborhood(from, ma)) {
                    throw new RuntimeException(String.format("ERROR: Mapping-assignment could not be added to right neighborhood at time-step: t=%d", sourceTimeStep));
                }
                if (!edgeSets.addToLeftNeighborhood(to, ma)) {
                    throw new RuntimeException(String.format("ERROR: Mapping-assignment could not be added to left neighborhood at time-step: t=%d", sourceTimeStep));
                }
            }
        }
    }

    /**
     * Computes the compatibility-mapping-costs between the two given
     * hypothesis.
     *
     * @param sourceComponent the segmentation hypothesis from which the mapping originates.
     * @param targetComponent the segmentation hypothesis towards which the
     *                        mapping-assignment leads.
     * @return the cost we want to set for the given combination of segmentation
     * hypothesis.
     */
    public Float compatibilityCostOfMapping(
            final AdvancedComponent<FloatType> sourceComponent,
            final AdvancedComponent<FloatType> targetComponent) {
        final long sourceComponentSize = getComponentSize(sourceComponent, 1);
        final long targetComponentSize = getComponentSize(targetComponent, 1);

        final ValuePair<Integer, Integer> sourceComponentBoundaries = sourceComponent.getVerticalComponentLimits();
        final ValuePair<Integer, Integer> targetComponentBoundaries = targetComponent.getVerticalComponentLimits();

        final float sourceUpperBoundary = sourceComponentBoundaries.getA();
        final float sourceLowerBoundary = sourceComponentBoundaries.getB();
        final float targetUpperBoundary = targetComponentBoundaries.getA();
        final float targetLowerBoundary = targetComponentBoundaries.getB();

        float averageMigrationCost = 0;
        if(configurationManager.getMigrationCostFeatureFlag()){
            final Pair<Float, float[]> migrationCostOfUpperBoundary = costFactory.getMigrationCost(sourceUpperBoundary, targetUpperBoundary);
            final Pair<Float, float[]> migrationCostOfLowerBoundary = costFactory.getMigrationCost(sourceLowerBoundary, targetLowerBoundary);
            averageMigrationCost = 0.5f * migrationCostOfLowerBoundary.getA() + 0.5f * migrationCostOfUpperBoundary.getA();
        }

        boolean targetTouchesCellDetectionRoiTop = (targetComponentBoundaries.getA() <= configurationManager.getCellDetectionRoiOffsetTop());

        final Pair<Float, float[]> growthCost = costFactory.getGrowthCost(sourceComponentSize, targetComponentSize, targetTouchesCellDetectionRoiTop);

        float mappingCost = growthCost.getA() + averageMigrationCost;
        return mappingCost;
    }

    /**
     * This method defines how the segmentation costs are influencing the costs
     * of mapping assignments during the ILP hypotheses substitution takes
     * place.
     *
     * @param sourceComponentCost
     * @param targetComponentCost
     * @param mappingCosts
     * @return
     */
    public float costModulationForSubstitutedILP(
            final float sourceComponentCost,
            final float targetComponentCost,
            final float mappingCosts) {
        return sourceWeightingFactor * sourceComponentCost + targetWeightingFactor * targetComponentCost + mappingCosts; /* here again we fold the costs from the nodes into the corresponding assignment;
																  we should probably do 50%/50%, but we did different and it's ok */
    }

    /**
     * This function scales the cost of given assignment by the number of leaves
     * that are under the components participating in that assignment. The idea is
     * that we a given component must have lower cost than the average cost of the
     * leaves under it. This was suggested in Funke et. al., 2012, "Efficient Automatic
     * 3D-Reconstruction of Branching Neurons from EM Data".
     *
     * @param sourceComponent source component of the mapping assignment
     * @param targetComponent target component of the mapping assignment
     * @param cost            current cost, that will be rescaled
     * @return rescaled cost
     */
    public float scaleAssignmentCost(AdvancedComponent sourceComponent,
                                     AdvancedComponent targetComponent,
                                     float cost) {
        int numberOfLeavesUnderSource = getLeafNodes(sourceComponent).size();
        int numberOfLeavesUnderTarget = getLeafNodes(targetComponent).size();
        if (numberOfLeavesUnderSource == 0) numberOfLeavesUnderSource = 1;
        if (numberOfLeavesUnderTarget == 0) numberOfLeavesUnderTarget = 1;
        return cost * (0.1f * numberOfLeavesUnderSource + 0.9f * numberOfLeavesUnderTarget);
    }

    private float sourceWeightingFactor = 0.5f;

    private float targetWeightingFactor = (1 - sourceWeightingFactor);

    /**
     * This method defines how the segmentation costs are influencing the costs
     * of division assignments during the ILP hypotheses substitution takes
     * place.
     *
     * @param sourceComponentCost
     * @param compatibilityCostOfDivision
     * @return
     */
    public float costModulationForSubstitutedILP(
            final float sourceComponentCost,
            final float upperTargetComponentCost,
            final float lowerTargetComponentCost,
            final float compatibilityCostOfDivision) {
        return sourceWeightingFactor * sourceComponentCost + targetWeightingFactor * (upperTargetComponentCost + lowerTargetComponentCost) + compatibilityCostOfDivision;
    }

    /**
     * This method defines how the segmentation costs are influencing the costs
     * of exit assignments during the ILP hypotheses substitution takes place.
     *
     * @param fromCosts costs for the segment to exit
     * @return the modulated costs.
     */
    public float costModulationForSubstitutedILP(final float fromCosts) {
        return 0.0f;
//        return Math.min(0.0f, fromCosts / 2f); // NOTE: 0 or negative but only hyp/4 to prefer map or div if exists...
        // fromCosts/2: 1/2 has to do with the folding of the node-cost into the assignments (e.g. mapping: 1/2 to left und 1/2 to right)
        // Math.min: because exit assignment should never cost something
    }

    /**
     * Add a division-assignment for given timestep between component in {@param sourceComponentForest} and
     * {@param targetComponentForest}. This function also looks for suitable pairs of components in
     * {@param targetComponentForest}, since division-assignments need two target component. The hypotheses of the
     * components, which are needed for the assignments, are created on the fly as needed.
     *
     * @param sourceTimeStep        the time-point from which the <code>curHyps</code> originate.
     * @param sourceComponentForest the component tree containing source components of the division assignments.
     * @param targetComponentForest the component tree containing target components at the next time-point of the division assignments.
     * @throws GRBException
     */
    private void addDivisionAssignments(final int sourceTimeStep,
                                        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> sourceComponentForest,
                                        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> targetComponentForest)
            throws GRBException {

        for (final AdvancedComponent<FloatType> sourceComponent : sourceComponentForest.getAllComponents()) {

            if (sourceTimeStep > 0) {
                if (!nodes.containsKey(sourceComponent))
                    continue; /* we only want to continue paths of previously existing hypotheses; this is to fulfill the continuity constraint */
            }

            for (final AdvancedComponent<FloatType> upperTargetComponent : targetComponentForest.getAllComponents()) {
                final List<AdvancedComponent<FloatType>> lowerNeighborComponents = ((AdvancedComponent) upperTargetComponent).getLowerNeighbors();

                for (final AdvancedComponent<FloatType> lowerTargetComponent : lowerNeighborComponents) {
                    if (ComponentTreeUtils.isBelowByMoreThen(sourceComponent, lowerTargetComponent, configurationManager.getMaxCellDrop())) {
                        continue;
                    }

                    if (!assignmentPlausibilityTester.sizeDifferenceIsPlausible(sourceComponent.getMajorAxisLength(), upperTargetComponent.getMajorAxisLength() + lowerTargetComponent.getMajorAxisLength())) {
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    final Float compatibilityCostOfDivision = compatibilityCostOfDivision(sourceComponent,
                            upperTargetComponent, lowerTargetComponent);

                    float cost = costModulationForSubstitutedILP(
                            sourceComponent.getCost(),
                            upperTargetComponent.getCost(),
                            lowerTargetComponent.getCost(),
                            compatibilityCostOfDivision);

                    if (cost > configurationManager.getAssignmentCostCutoff()) {
                        continue;
                    }
                    final Hypothesis<AdvancedComponent<FloatType>> to =
                            nodes.getOrAddHypothesis(sourceTimeStep + 1, new Hypothesis<>(sourceTimeStep + 1, upperTargetComponent, this));
                    final Hypothesis<AdvancedComponent<FloatType>> lowerNeighbor =
                            nodes.getOrAddHypothesis(sourceTimeStep + 1, new Hypothesis<>(sourceTimeStep + 1, lowerTargetComponent, this));
                    final Hypothesis<AdvancedComponent<FloatType>> from =
                            nodes.getOrAddHypothesis(sourceTimeStep, new Hypothesis<>(sourceTimeStep, sourceComponent, this));

//                    final String name = String.format("a_%d^DIVISION--(%d,%d,%d)", sourceTimeStep, from.getStringId(), to.getStringId(), lowerNeighbor.getStringId());
                    final GRBVar newLPVar = model.addVar(0.0, 1.0, cost, GRB.BINARY, DivisionAssignment.buildStringId(sourceTimeStep, from.getWrappedComponent(), to.getWrappedComponent(), lowerNeighbor.getWrappedComponent()));

                    final DivisionAssignment da = new DivisionAssignment(newLPVar, this, from, to, lowerNeighbor, sourceTimeStep);

//                    assignmentFilter.evaluate(da);

                    nodes.addAssignment(sourceTimeStep, da);
                    edgeSets.addToRightNeighborhood(from, da);
                    edgeSets.addToLeftNeighborhood(to, da);
                    edgeSets.addToLeftNeighborhood(lowerNeighbor, da);
                }
            }
        }
    }

    /**
     * Computes the compatibility-mapping-costs between the two given
     * hypothesis.
     *
     * @param sourceComponent the segmentation hypothesis from which the mapping originates.
     * @return the cost we want to set for the given combination of segmentation
     * hypothesis.
     */
    public Float compatibilityCostOfDivision(
            final AdvancedComponent<FloatType> sourceComponent,
            final AdvancedComponent<FloatType> upperTargetComponent,
            final AdvancedComponent<FloatType> lowerTargetComponent) {


        final ValuePair<Integer, Integer> sourceBoundaries = sourceComponent.getVerticalComponentLimits();
        final ValuePair<Integer, Integer> upperTargetBoundaries = upperTargetComponent.getVerticalComponentLimits();
        final ValuePair<Integer, Integer> lowerTargetBoundaries = lowerTargetComponent.getVerticalComponentLimits();

        final long sourceSize = getComponentSize(sourceComponent, 1);
        final long upperTargetSize = getComponentSize(upperTargetComponent, 1);
        final long lowerTargetSize = getComponentSize(lowerTargetComponent, 1);
        final long summedTargetSize = upperTargetSize + lowerTargetSize;

        final float sourceUpperBoundary = sourceBoundaries.getA();
        final float sourceLowerBoundary = sourceBoundaries.getB();
        final float upperTargetUpperBoundary = upperTargetBoundaries.getA();
        final float lowerTargetLowerBoundary = lowerTargetBoundaries.getB();

        float averageMigrationCost = 0;
        if(configurationManager.getMigrationCostFeatureFlag()){
            final Pair<Float, float[]> migrationCostOfUpperBoundary = costFactory.getMigrationCost(sourceUpperBoundary, upperTargetUpperBoundary);
            final Pair<Float, float[]> migrationCostOfLowerBoundary = costFactory.getMigrationCost(sourceLowerBoundary, lowerTargetLowerBoundary);
            averageMigrationCost = .5f * migrationCostOfLowerBoundary.getA() + .5f * migrationCostOfUpperBoundary.getA();
        }

        boolean upperTargetTouchesCellDetectionRoiTop = (upperTargetBoundaries.getA() <= configurationManager.getCellDetectionRoiOffsetTop());

        final Pair<Float, float[]> growthCost = costFactory.getGrowthCost(sourceSize, summedTargetSize, upperTargetTouchesCellDetectionRoiTop);
//        final float divisionLikelihoodCost = costFactory.getDivisionLikelihoodCost(sourceComponent);

//        float divisionCost = growthCost.getA() + averageMigrationCost + divisionLikelihoodCost;
        float divisionCost = growthCost.getA() + averageMigrationCost;
        return divisionCost;
    }

    /**
     * This function traverses all time-points of the growth-line
     * <code>gl</code>, retrieves the full component tree that has to be built
     * beforehand, and calls the private method
     * <code>recursivelyAddPathBlockingConstraints</code> on all those root
     * nodes. This function adds one constraint for each path starting at a leaf
     * node in the tree up to the root node itself.
     * Those path-blocking constraints ensure, that only 0 or 1 of the
     * segmentation hypothesis along such a path can be chosen during the convex
     * optimization.
     *
     * @throws GRBException
     */
    private void addPathBlockingConstraints() throws GRBException {
        // For each time-point
        for (int t = 0; t < gl.numberOfFrames(); t++) {
            // Get the full component tree
            final ComponentForest<?> ct = gl.get(t).getComponentForest();
            // And call the function adding all the path-blocking-constraints...
            recursivelyAddPathBlockingConstraints(ct, t);
        }
    }

    private void addPathBlockingConstraintsNew() {
        for (int t = 0; t < gl.numberOfFrames(); t++) {
            List<Hypothesis<AdvancedComponent<FloatType>>> hypotheses = nodes.getLeafHypothesesAt(t);
            for (Hypothesis<AdvancedComponent<FloatType>> hyp : hypotheses) {
                try {
                    addPathBlockingConstraintForLeafHypothesis(hyp);
                } catch (GRBException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void addPathBlockingConstraintForLeafHypothesis(Hypothesis<AdvancedComponent<FloatType>> hyp) throws GRBException {
        final GRBLinExpr exprR = new GRBLinExpr();
        Hypothesis<AdvancedComponent<FloatType>> runnerHyp = hyp;
        while (runnerHyp != null) {
            if (isNull(edgeSets.getRightNeighborhood(runnerHyp)) || edgeSets.getRightNeighborhood(runnerHyp).isEmpty()) {
                throw new RuntimeException("Error: There are no outgoing assignments for hypothesis: " + runnerHyp.getStringId());
            }
            for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> a : edgeSets.getRightNeighborhood(runnerHyp)) {
                exprR.addTerm(1.0, a.getGRBVar());  // again we build the constraints for the assignments, because we do not optimize for nodes; we therefore need to add *all* right-assignments to the constraint of a given node
            }
            runnerHyp = runnerHyp.getParentHypothesis();
        }
        model.addConstr(exprR, GRB.LESS_EQUAL, 1.0, "PathBlockConstrT" + hyp.getTime() + "_" + hyp.getStringId());
    }

    private <C extends Component<?, C>> void recursivelyAddPathBlockingConstraints(
            final ComponentForest<C> ct,
            final int t)
            throws GRBException {
        for (final C ctRoot : ct.roots()) {
            // And call the function adding all the path-blocking-constraints...
            recursivelyAddPathBlockingConstraints(ctRoot, t);
        }
    }

    /**
     * Generates path-blocking constraints for each path from the given
     * <code>ctNode</code> to a leaf in the tree.
     * Those path-blocking constraints ensure, that only 0 or 1 of the
     * segmentation hypothesis along such a path can be chosen during the convex
     * optimization.
     *
     * @param t
     * @throws GRBException
     */
    private <C extends Component<?, C>> void recursivelyAddPathBlockingConstraints(
            final C component,
            final int t) throws GRBException {

        if (component.getChildren().size() == 0) { /* If ctNode is leaf-node -> add constraint (by going up the list of parents and building up the constraint) */
            String leafNodeHypId = nodes.findHypothesisContaining(component).getStringId();
            C runnerNode = component;

            final GRBLinExpr exprR = new GRBLinExpr();
            while (runnerNode != null) {
                @SuppressWarnings("unchecked") final Hypothesis<AdvancedComponent<FloatType>> hypothesis = nodes.findHypothesisContaining(runnerNode);
                assert (hypothesis != null) : "WARNING: Hypothesis for a CTN was not found in GrowthlaneTrackingILP -- this is an indication for some design problem of the system!";

                if (edgeSets.getRightNeighborhood(hypothesis) != null) {
                    for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> a : edgeSets.getRightNeighborhood(hypothesis)) {
                        exprR.addTerm(1.0, a.getGRBVar());  // again we build the constraints for the assignments, because we do not optimize for nodes; we therefore need to add *all* right-assignments to the constraint of a given node
                    }
                }
                runnerNode = runnerNode.getParent();
            }
            model.addConstr(exprR, GRB.LESS_EQUAL, 1.0, "PathBlockConstrT" + t + "_" + leafNodeHypId);
        } else {  /* If ctNode is not a leaf-node -> recurse */
            for (final C childComponents : component.getChildren()) {
                recursivelyAddPathBlockingConstraints(childComponents, t);
            }
        }
    }

    double bigM = 0;

    private void addCrossingConstraints() throws GRBException {
        for (int t = 0; t < gl.numberOfFrames() - 1; t++) { /* upper limit of FOR-loop is `gl.numberOfFrames() - 1` because we do not need crossing-constraints for the last time-step, which only contains exit-assignments */
            calculateBigM(nodes.getHypothesesAt(t+1)); /* calculate bigM for target components at t+1 */

            for (final Hypothesis<AdvancedComponent<FloatType>> hypothesisOfInterest : nodes.getHypothesesAt(t)) {
                List<AdvancedComponent<FloatType>> componentsBelow = hypothesisOfInterest.getWrappedComponent().getAllComponentsBelow();
                List<Hypothesis<AdvancedComponent<FloatType>>> hypothesesBelow = getExistingHypothesesForComponents(componentsBelow);
                addCrossingConstraint(hypothesisOfInterest, hypothesesBelow);
            }
        }
    }

    /**
     * This method calculate the big-M value for the crossing-constraint.
     *
     * It takes as input all target-components of assignments being considered in the crossing-constraint.
     * It then:
     * 1. Determines the highest rank of the leaf-components in the list: R
     * 2. Increase this rank value by 1: R+1
     * 3. Set big-M as the power-of-two: 2^R
     * big-M is thus guaranteed to be larger than any ordinal-value of the target-components and also larger than the
     * sum of any two ordinal.
     *
     * @param allTargetHypotheses
     */
    private void calculateBigM(List<Hypothesis<AdvancedComponent<FloatType>>> allTargetHypotheses) {
        double maxLeafRank = 0;
//        List<Integer> res = allTargetHypotheses.stream().map(hyp -> hyp.getWrappedComponent().getChildren().size()).collect(Collectors.toList());
        for (Hypothesis<AdvancedComponent<FloatType>> hypothesis : allTargetHypotheses) {
            AdvancedComponent<FloatType> component = hypothesis.getWrappedComponent();
            System.out.println("component id: " + component.getStringId());
            if (component.getChildren().size() == 0) {
                double componentRank = component.getRankRelativeToLeafComponent();
                maxLeafRank = (componentRank > maxLeafRank) ? componentRank : maxLeafRank;
            }
        }
        bigM = Math.pow(2, maxLeafRank + 1);

        if (Double.isNaN(bigM) || Double.isInfinite(bigM)) {
            throw new RuntimeException("Value for bigM was not correctly calculated!");
        }
    }

    private List<Hypothesis<AdvancedComponent<FloatType>>> getExistingHypothesesForComponents(List<AdvancedComponent<FloatType>> components) {
        List<Hypothesis<AdvancedComponent<FloatType>>> hypothesisList = new ArrayList<>();
        for (AdvancedComponent<FloatType> component : components){
            try {
                hypothesisList.add(nodes.findHypothesisContaining(component));
            } catch (IlpSetupException e) {
                /* we catch IlpSetupException because this method returns _all existing_ hypothesis for the components of interest  */
            }
        }
        return hypothesisList;
    }

    private void addConstrainedTerm(AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assignment,
                                    double coeff_sign,
                                    GRBLinExpr expr) {
        Hypothesis<AdvancedComponent<FloatType>> hyp;
        if (assignment instanceof MappingAssignment) {
            hyp = ((MappingAssignment) assignment).getDestinationHypothesis();
            double coefficient = coeff_sign * hyp.getWrappedComponent().getOrdinalValue();
            expr.addTerm(coefficient, assignment.getGRBVar());
        } else if (assignment instanceof DivisionAssignment) {
            Hypothesis<AdvancedComponent<FloatType>> lowerHypothesis = ((DivisionAssignment) assignment).getLowerDestinationHypothesis();
            Hypothesis<AdvancedComponent<FloatType>> upperHypothesis = ((DivisionAssignment) assignment).getUpperDestinationHypothesis();
            double coefficient = coeff_sign * (lowerHypothesis.getWrappedComponent().getOrdinalValue() + upperHypothesis.getWrappedComponent().getOrdinalValue());
            expr.addTerm(coefficient, assignment.getGRBVar());
        }
    }

    private void addConstrainedTerms(Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> assignments,
                                      double coeff_sign,
                                      GRBLinExpr expr) {
        for (AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assignment : assignments) {
            addConstrainedTerm(assignment, coeff_sign, expr);
        }
    }

    /**
     * Add constrained terms for assignments starting from source hypotheses below the hypothesis of interest.
     * @param hypothesis
     * @param coeff_sign
     * @param expr
     */
    private void addConstrainedTermsForHypothesis(Hypothesis<AdvancedComponent<FloatType>> hypothesis,
                                                   double coeff_sign,
                                                   GRBLinExpr expr) {
        if (edgeSets.getRightNeighborhood(hypothesis) != null) {
            addConstrainedTerms(edgeSets.getRightNeighborhood(hypothesis), coeff_sign, expr);
        } else {
            throw new RuntimeException("edgeSets.getRightNeighborhood(hypothesis) == null; hypothesis.getStringId(): " + hypothesis.getStringId());
        }
    }

    private void addCrossingConstraint(Hypothesis<AdvancedComponent<FloatType>> hypothesisOfInterest, List<Hypothesis<AdvancedComponent<FloatType>>> hypothesesBelow) throws GRBException {
        final GRBLinExpr expr = new GRBLinExpr();
        int sourceTime = hypothesisOfInterest.getTime();

        addConstrainingTermsForHypothesis(hypothesisOfInterest, 1.0, expr);

        for (Hypothesis<AdvancedComponent<FloatType>> hypothesis : hypothesesBelow){
            addConstrainedTermsForHypothesis(hypothesis, -1.0, expr);
        }

        model.addConstr(expr, GRB.GREATER_EQUAL, -bigM, "CrossConstrT" + sourceTime + "_" + hypothesisOfInterest.getStringId());
    }

    /**
     * Adding constraining terms for assignments starting the source hypothesis of interest.
     * @param hypothesis
     * @param coeff_sign
     * @param expr
     */
    private void addConstrainingTermsForHypothesis(Hypothesis<AdvancedComponent<FloatType>> hypothesis,
                                                   double coeff_sign,
                                                   GRBLinExpr expr) {
        if (edgeSets.getRightNeighborhood(hypothesis) != null) {
            addConstrainingTerms(edgeSets.getRightNeighborhood(hypothesis), coeff_sign, expr);
        } else {
            throw new RuntimeException("edgeSets.getRightNeighborhood(hypothesis) == null; hypothesis.getStringId(): " + hypothesis.getStringId());
        }
    }

    private void addConstrainingTerms(Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> assignments,
                                      double coeff_sign,
                                      GRBLinExpr expr) {
        for (AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assignment : assignments) {
            addConstrainingTerm(assignment, coeff_sign, expr);
        }
    }

    private void addConstrainingTerm(AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assignment,
                                     double coeff_sign,
                                     GRBLinExpr expr) {
        Hypothesis<AdvancedComponent<FloatType>> hyp;
        if (assignment instanceof MappingAssignment) {
            hyp = ((MappingAssignment) assignment).getDestinationHypothesis();
            double ordinal = hyp.getWrappedComponent().getOrdinalValue();
            if(ordinal >= bigM){
                throw new AssertionError(String.format("The value of bigM (=%f) is smaller than the ordinal value (=%f) of hypothesis %s; this is not allowed, because it will lead to an incorrect crossing-constraint", bigM, ordinal, hyp.getStringId()));
            }
            double coefficient = coeff_sign * (ordinal - bigM);
            expr.addTerm(coefficient, assignment.getGRBVar());
        } else if (assignment instanceof DivisionAssignment) {
            Hypothesis<AdvancedComponent<FloatType>> lowerHypothesis = ((DivisionAssignment) assignment).getLowerDestinationHypothesis();
            Hypothesis<AdvancedComponent<FloatType>> upperHypothesis = ((DivisionAssignment) assignment).getUpperDestinationHypothesis();
            double ordinal = lowerHypothesis.getWrappedComponent().getOrdinalValue() + upperHypothesis.getWrappedComponent().getOrdinalValue();
            if(ordinal >= bigM){
                throw new AssertionError(String.format("The value of bigM (=%f) is smaller than the summed ordinal values (=%f) of hypotheses %s and %s; this is not allowed, because it will lead to an incorrect crossing-constraint", bigM, ordinal, lowerHypothesis.getStringId(), upperHypothesis.getStringId()));
            }
            double coefficient = coeff_sign * (ordinal - bigM);
            expr.addTerm(coefficient, assignment.getGRBVar());
        }
    }

    /**
     * This function generates and adds the explanation-continuity-constraints
     * to the ILP model.
     * Those constraints ensure that for each segmentation hypotheses at all
     * time-points t we have the same number of active incoming and active
     * outgoing edges from/to assignments.
     * Intuitively speaking this means that each hypothesis that is chosen by an
     * assignment coming from t-1 we need to continue its interpretation by
     * finding an active assignment towards t+1.
     */
    private void addContinuityConstraints() throws GRBException {
        // For each time-point
        for (int t = 1; t < gl.numberOfFrames(); t++) {
            for (final Hypothesis<AdvancedComponent<FloatType>> hyp : nodes.getHypothesesAt(t)) {
                final GRBLinExpr expr = new GRBLinExpr();

                /* TODO-MM-2019-11-21: WARNING: The two separate null-checks below might cause problems in setting up ILP-constraint. If one is null and the other is not, we will have an asymmetric constraint.
                 * Additional note: While the above is true, we will have to find a solution for t=0/t=gl.size(), which do not have incoming/outgoing assignments.
                 */
                if (edgeSets.getLeftNeighborhood(hyp) != null) {
                    for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> a_j : edgeSets.getLeftNeighborhood(hyp)) {
                        expr.addTerm(1.0, a_j.getGRBVar());
                    }
                } else {
                    System.out.println(String.format("addContinuityConstraints(): t=%d", t));
                    System.out.println("edgeSets.getLeftNeighborhood( hyp ) == null");
                }
                if (edgeSets.getRightNeighborhood(hyp) != null) {
                    for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> a_j : edgeSets.getRightNeighborhood(hyp)) {
                        expr.addTerm(-1.0, a_j.getGRBVar());
                    }
                } else {
                    System.out.println(String.format("addContinuityConstraints(): t=%d", t));
                    System.out.println("edgeSets.getRightNeighborhood( hyp ) == null");
                }

                // add the constraint for this hypothesis
                model.addConstr(expr, GRB.EQUAL, 0.0, "ContConstrT" + t + "_" + hyp.getStringId());
            }
        }
    }

    /**
     * Performs autosave of current tracking interactions (if the checkbox in
     * the MotherMachineGui is checked).
     */
    public synchronized void autosave() {
        if (!configurationManager.getIfRunningHeadless() && MoMA.getGui().isAutosaveRequested()) {
            final File autosaveFile = new File(configurationManager.getPathForAutosaving());
            saveState(autosaveFile);
            System.out.println("Autosave to: " + autosaveFile.getAbsolutePath());
        }
    }

    public void run() {
        if (configurationManager.getRunIlpOnChange()) {
            runImmediately();
        }
    }

    private void invalidateCaches() {
        getAllAssignments().stream().forEach(assigmnent -> assigmnent.invalidateCache());
        getAllHypotheses().stream().forEach(hypothesis -> hypothesis.invalidateCache());
    }

    private void fillCaches() {
        getAllAssignments().stream().forEach(assigmnent -> assigmnent.cache());
        getAllHypotheses().stream().forEach(hypothesis -> hypothesis.cache());
    }

    /**
     * This function takes the ILP built up in <code>model</code>
     * and starts the convex optimization procedure. This is actually the step
     * that will find the MAP in the given model and hence the solution to our
     * segmentation and tracking problem.
     */
    public void runImmediately() {
        try {
            invalidateCaches();

            // Set maximum time Gurobi may use!
//			model.getEnv().set( GRB.DoubleParam.TimeLimit, MotherMachine.GUROBI_TIME_LIMIT ); // now handled by callback!
            model.getEnv().set(GRB.IntParam.OutputFlag, 0);

            final IDialogGurobiProgress dialog = gurobiProgressDialogFactory.get();
            final GurobiCallbackAbstract gcb = gurobiCallbackFactory.get();
            model.setCallback(gcb);
            if (!configurationManager.getIfRunningHeadless()) {
                dialog.setVisible(true);
            }

            // RUN + return true if solution is feasible
            // - - - - - - - - - - - - - - - - - - - - -
            MoMA.dic.getOptimizationTimer().start();
            status = IlpStatus.OPTIMIZATION_IS_RUNNING;
            fireStateChanged();
            model.optimize();
            MoMA.dic.getOptimizationTimer().stop();
            MoMA.dic.getOptimizationTimer().printExecutionTime("Timer result for optimization time");
            dialog.notifyGurobiTermination();

            // Read solution and extract interpretation
            // - - - - - - - - - - - - - - - - - - - - -
            if (model.get(GRB.IntAttr.Status) == GRB.Status.OPTIMAL) {
                status = IlpStatus.OPTIMAL;
                if (!configurationManager.getIfRunningHeadless()) {
                    dialog.pushStatus("Optimum was found!");
                    if (MoMA.getGui() != null) {
                        MoMA.getGui().requestFocusOnTimeStepSlider();
                    }
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            } else if (model.get(GRB.IntAttr.Status) == GRB.Status.INFEASIBLE) {
                status = IlpStatus.INFEASIBLE;
                if (!configurationManager.getIfRunningHeadless()) {
                    dialog.pushStatus("ILP now infeasible. Please reoptimize!");
                }
            } else if (model.get(GRB.IntAttr.Status) == GRB.Status.UNBOUNDED) {
                status = IlpStatus.UNBOUNDED;
            } else if (model.get(GRB.IntAttr.Status) == GRB.Status.SUBOPTIMAL) {
                status = IlpStatus.SUBOPTIMAL;
            } else if (model.get(GRB.IntAttr.Status) == GRB.Status.NUMERIC) {
                status = IlpStatus.NUMERIC;
            } else {
                status = IlpStatus.LIMIT_REACHED;
                if (!configurationManager.getIfRunningHeadless()) {
                    dialog.pushStatus(String.format("Timelimit reached, rel. optimality gap: %.2f%%", gcb.getLatestGap() * 100.0));
                }
            }
            System.out.println("Status: " + status);

            if (status == IlpStatus.OPTIMIZATION_IS_RUNNING) {
                status = IlpStatus.UNDEFINED; /* something went wrong and `this.status` was not set to something different than OPTIMIZATION_RUNNING; so set it to UNDEFINED */
            }

            IlpSolutionSanityChecker solutionSanityChecker = new IlpSolutionSanityChecker(this, gl);
            solutionSanityChecker.CheckSolutionContinuityConstraintForAllTimesteps();
            System.out.println(solutionSanityChecker.getErrorMessage());
            if (solutionSanityChecker.continuityConstraintViolationFound()) {
                if (!isNull(dialogManager)) {
                    dialogManager.showErrorDialogWithTextArea("ERROR: Missing assignments found", solutionSanityChecker.getErrorMessage());
                }
            }
            if (isReady() && removeStorageLockConstraintAfterFirstOptimization) {
                removeStorageLockConstraintsFromAssignments(); /* remove optimization locks after first successful optimization, when loading previous results */
                removeStorageLockConstraintAfterFirstOptimization = false;
            }
            if (getStatus() != IlpStatus.INFEASIBLE && getStatus() != IlpStatus.SUBOPTIMAL) {
                fillCaches();
            }
            fireStateChanged();

            if (!isNull(MoMA.getGui())) {
                MoMA.getGui().dataToDisplayChanged();
            }
        } catch (final GRBException e) {
            status = IlpStatus.UNDEFINED;
            System.out.println("Could not run the generated ILP!");
            e.printStackTrace();
        }
    }

    /**
     * Returns the optimal segmentation at time t, given by a list of non-conflicting component-tree-nodes.
     * Calling this function makes only sense if the <code>run</code>-method was
     * called and the convex optimizer could find a optimal feasible solution.
     *
     * @param t the time-point at which to look for the optimal segmentation.
     * @return a list of <code>Hypothesis</code> containting
     * <code>ComponentTreeNodes</code> that correspond to the
     * active segmentation hypothesis (chosen by the optimization
     * procedure).
     */
    public List<Hypothesis<AdvancedComponent<FloatType>>> getOptimalSegmentation(int t) {
        return getOptimalHypotheses(t);
    }

    /**
     * Returns all hypotheses of the specified time-step.
     *
     * @param t
     * @return list of hypotheses
     */
    public List<Hypothesis<AdvancedComponent<FloatType>>> getHypothesesAt(int t) {
        return nodes.getHypothesesAt(t);
    }

    /**
     * Returns the components of the optimal segmentation at time t.
     *
     * @param t time
     * @return List<Component < FloatType, ?>> list of components at time t that are part of the optimal solution
     */
    public List<AdvancedComponent<FloatType>> getOptimalComponents(final int t) {
        List<Hypothesis<AdvancedComponent<FloatType>>> ilpSelectedHypotheses = getOptimalSegmentation(t);

        List<AdvancedComponent<FloatType>> selectedComponents = new ArrayList<>();
        for (Hypothesis<AdvancedComponent<FloatType>> hypothesis : ilpSelectedHypotheses) {
            selectedComponents.add(hypothesis.getWrappedComponent());
        }
        return selectedComponents;
    }

    /**
     * Returns the active segmentation at time t and the given y-location along
     * the gap-separation function of the corresponding GLF.
     * Calling this function makes only sense if the <code>run</code>-method was
     * called and the convex optimizer could find a optimal feasible solution.
     *
     * @param t          the time-point at which to look for the optimal segmentation.
     * @param gapSepYPos the position along the gap-separation-function you want to
     *                   receive the active segmentation hypothesis for.
     * @return a <code>Hypothesis< Component< FloatType, ? >></code> that
     * correspond to the active segmentation hypothesis at the
     * requested location.
     * Note: this function might return <code>null</code> since not all
     * y-locations are occupied by active segmentation hypotheses!
     */
    public Hypothesis<AdvancedComponent<FloatType>> getOptimalSegmentationAtLocation(final int t, final int gapSepYPos) {
        final List<Hypothesis<AdvancedComponent<FloatType>>> hyps = getOptimalHypotheses(t);
        for (final Hypothesis<AdvancedComponent<FloatType>> h : hyps) {
            final ValuePair<Integer, Integer> ctnLimits =
                    ComponentTreeUtils.getTreeNodeInterval(h.getWrappedComponent());
            if (ctnLimits.getA() <= gapSepYPos && ctnLimits.getB() >= gapSepYPos) {
                return h;
            }
        }
        return null;
    }

    /**
     * Returns the hypotheses with components/segements, which are children of
     */
    public List<Hypothesis<AdvancedComponent<FloatType>>> getConflictingChildSegments(final int t, final Hypothesis<AdvancedComponent<FloatType>> parentHypothesis) {
        AdvancedComponent<FloatType> parentComponent = parentHypothesis.getWrappedComponent();
        ArrayList<AdvancedComponent<FloatType>> componentList = new ArrayList<>();
        addListOfNodes(parentComponent, componentList);
        final List<Hypothesis<AdvancedComponent<FloatType>>> hypotheses = nodes.getHypothesesAt(t);
        ArrayList<Hypothesis<AdvancedComponent<FloatType>>> result = new ArrayList<>();
        for (Hypothesis<AdvancedComponent<FloatType>> hypothesis : hypotheses) {
            AdvancedComponent<FloatType> wrappedComponent = hypothesis.getWrappedComponent();
            if (componentList.contains(wrappedComponent)) {
                result.add(hypothesis);
            }
        }
        return result;
    }

    /**
     * Returns a list of the hypotheses which conflict with {@param hypothesis} base on its component-tree structure.
     * Note that not all components must have a corresponding hypothesis, so we only return the hypotheses that exist.
     *
     * @param hypothesis for which we want to find conflicting hypotheses in the component-tree.
     * @return list of conflicting hypotheses in the component-tree
     */
    public List<Hypothesis<AdvancedComponent<FloatType>>> getConflictingHypotheses(final Hypothesis<AdvancedComponent<FloatType>> hypothesis) {
        final List<Hypothesis<AdvancedComponent<FloatType>>> ret = new ArrayList<>();

        AdvancedComponent<FloatType> component = hypothesis.getWrappedComponent();
        List<ComponentInterface> conflictingComponents = new ArrayList<>();
        ComponentTreeUtils.recursivelyAddChildrenToList(component, conflictingComponents);
        recursivelyAddParentsToList(component, conflictingComponents);

        for (ComponentInterface comp : conflictingComponents){
            if(nodes.containsKey(comp)){
                ret.add(nodes.findHypothesisContaining(comp));
            }
        }
        return ret;
    }

    /**
     * @param t
     * @param gapSepYPos
     * @return
     */
    public List<Hypothesis<AdvancedComponent<FloatType>>> getSegmentsAtLocation(final int t, final int gapSepYPos) {
        final List<Hypothesis<AdvancedComponent<FloatType>>> ret = new ArrayList<>();

        final List<Hypothesis<AdvancedComponent<FloatType>>> hyps = nodes.getHypothesesAt(t);
        for (final Hypothesis<AdvancedComponent<FloatType>> h : hyps) {
            final ValuePair<Integer, Integer> ctnLimits =
                    ComponentTreeUtils.getTreeNodeInterval(h.getWrappedComponent());
            if (ctnLimits.getA() <= gapSepYPos && ctnLimits.getB() >= gapSepYPos) {  // fully contained inside
                ret.add(h);
            }
        }
        return ret;
    }

    /**
     * Returns the optimal segmentation at time t, given by a list of non-conflicting
     * segmentation hypothesis.
     * Calling this function makes only sense if the <code>run</code>-method was
     * called and the convex optimizer could find a optimal feasible solution.
     *
     * @param t the time-point at which to look for the optimal segmentation.
     * @return a list of <code>Hypothesis< Component< FloatType, ? > ></code>
     * that correspond to the active segmentation hypothesis (chosen by
     * the optimization procedure).
     */
    private List<Hypothesis<AdvancedComponent<FloatType>>> getOptimalHypotheses(final int t) {
        final ArrayList<Hypothesis<AdvancedComponent<FloatType>>> result = new ArrayList<>();

        final List<Hypothesis<AdvancedComponent<FloatType>>> hyps = nodes.getHypothesesAt(t);

        for (final Hypothesis<AdvancedComponent<FloatType>> hyp : hyps) {
            Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> nh;
            if (t > 0) {
                nh = edgeSets.getLeftNeighborhood(hyp);
            } else {
                nh = edgeSets.getRightNeighborhood(hyp);
            }

            try {
                final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> aa = findActiveAssignment(nh);
                if (aa != null) {
                    result.add(hyp);
                }
            } catch (final GRBException e) {
                System.err.println("ERROR: It could not be determined if a certain assignment was chosen during the convex optimization! Hint: Maybe the ILP is infeasible and was therefore not solved?");
                e.printStackTrace();
            }
        }

        return result;
    }

    /***
     * Get all hypotheses at time t that have segment-specific constraint added to them.
     *
     * @param t: time
     * @return List of hypotheses with constraint.
     */
    public List<Hypothesis<AdvancedComponent<FloatType>>> getForcedHypotheses(final int t) {
        final List<Hypothesis<AdvancedComponent<FloatType>>> hypotheses = nodes.getHypothesesAt(t);
        List<Hypothesis<AdvancedComponent<FloatType>>> result = new ArrayList<>();
        for (Hypothesis<AdvancedComponent<FloatType>> hypothesis : hypotheses) {
            if (hypothesis.isForced() || hypothesis.isForceIgnored()) {
                result.add(hypothesis);
            }
        }
        return result;
    }

    public List<Hypothesis<AdvancedComponent<FloatType>>> getSelectedHypothesesAt(int t){
        List<Hypothesis<AdvancedComponent<FloatType>>> hyps = getHypothesesAt(t);
        return getHypothesesAt(t).stream().filter(hypothesis -> hypothesis.isSelected()).collect(Collectors.toList());
    }

    public boolean isSelected(final Hypothesis<AdvancedComponent<FloatType>> hypothesis) {
        Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> connectedHyptheses;
        if (hypothesis.getTime() > 0) {
            connectedHyptheses = edgeSets.getLeftNeighborhood(hypothesis);
        } else {
            connectedHyptheses = edgeSets.getRightNeighborhood(hypothesis);
        }

        try {
            final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> aa =
                    findActiveAssignment(connectedHyptheses);
            if (aa != null) {
                return true;
            }
        } catch (final GRBException e) {
//			System.err.println( "It could not be determined of a certain assignment was choosen during the convex optimization!" );
//			e.printStackTrace();
        }
        return false;
    }

    /**
     * Finds and returns the optimal left (to t-1) assignments at time-point t.
     * For each segmentation hypothesis at t we collect all active assignments
     * coming in from the left (from t-1).
     * Calling this function makes only sense if the <code>run</code>-method was
     * called and the convex optimizer could find a optimal feasible solution.
     *
     * @param t the time at which to look for active left-assignments.
     *          Values for t make only sense if <code>>=1</code> and
     *          <code>< nodes.getNumberOfTimeSteps().</code>
     * @return a hash-map that maps from segmentation hypothesis to sets
     * containing ONE assignment that (i) are active, and (ii) come in
     * from the left (from t-1).
     * Note that segmentation hypothesis that are not active will NOT be
     * included in the hash-map.
     */
    public HashMap<Hypothesis<AdvancedComponent<FloatType>>, Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>>> getOptimalLeftAssignments(final int t) {
        assert (t >= 1);
        assert (t < nodes.getNumberOfTimeSteps());

        final HashMap<Hypothesis<AdvancedComponent<FloatType>>, Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>>> ret = new HashMap<>();

        final List<Hypothesis<AdvancedComponent<FloatType>>> hyps = nodes.getHypothesesAt(t);

        for (final Hypothesis<AdvancedComponent<FloatType>> hyp : hyps) {
            try {
                final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> ola = getOptimalLeftAssignment(hyp);
                if (ola != null) {
                    final HashSet<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> oneElemSet = new HashSet<>();
                    oneElemSet.add(ola);
                    ret.put(hyp, oneElemSet);
                }
            } catch (final GRBException e) {
                System.err.println("An optimal left assignment could not be determined!");
                e.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * Finds and returns the optimal left (to t-1) assignment given a
     * segmentation hypothesis.
     * For each segmentation hypothesis we know a set of outgoing edges
     * (assignments) that describe the interpretation (fate) of this segmented
     * cell. The ILP is set up such that only 1 such assignment can be chosen by
     * the convex optimizer during the computation of the optimal MAP
     * assignment.
     *
     * @return the optimal (choosen by the convex optimizer) assignment
     * describing the most likely data interpretation (MAP) towards the
     * previous time-point.
     * @throws GRBException
     */
    public AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> getOptimalLeftAssignment(final Hypothesis<AdvancedComponent<FloatType>> hypothesis) throws GRBException {
        return findActiveAssignment(edgeSets.getLeftNeighborhood(hypothesis));
    }

    /**
     * Get optimal assignments from timestep t to t+1
     *
     * @param t time step
     * @return set of optimal assignments
     */
    public Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> getOptimalAssignments(final int t) {
        return new HashSet<>(getOptimalAssignments(nodes.getAssignmentsAt(t))); /* return new HashSet to make sure the caller cannot modify underlying data-structures*/
    }

    /**
     * Return all assignments at t.
     *
     * @param t
     * @return
     */
    public Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> getAssignmentsAt(final int t) {
        return new HashSet<>(nodes.getAssignmentsAt(t));
    }

    /**
     * Get all optimal assignments from an Iterable of {@link AbstractAssignment}
     *
     * @param assignments iterable of assignments
     * @return set of optimal assignments
     */
    public Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> getOptimalAssignments(Iterable<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> assignments) {
        HashSet<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> activeAssignments = new HashSet<>();
        try {
            for (AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assignment : assignments) {
                if (assignment.isChosen()) activeAssignments.add(assignment);
            }
        } catch (GRBException e) {
            e.printStackTrace();
        }
        return activeAssignments;
    }

    /**
     * Finds and returns the optimal right (to t+1) assignments at time-point t.
     * For each segmentation hypothesis at t we collect all active assignments
     * going towards the right (to t+1).
     * Calling this function makes only sense if the <code>run</code>-method was
     * called and the convex optimizer could find a optimal feasible solution.
     *
     * @param t the time at which to look for active right-assignments.
     *          Values for t make only sense if <code>>=0</code> and
     *          <code>< nodes.getNumberOfTimeSteps() - 1.</code>
     * @return a hash-map that maps from segmentation hypothesis to a sets
     * containing ONE assignment that (i) are active, and (i) go towards
     * the right (to t+1).
     * Note that segmentation hypothesis that are not active will NOT be
     * included in the hash-map.
     */
    public HashMap<Hypothesis<AdvancedComponent<FloatType>>, Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>>> getOptimalRightAssignments(final int t) {
        assert (t >= 0);
        assert (t < nodes.getNumberOfTimeSteps() - 1) : String.format("Assert failed: t<nodes.getNumberOfTimeSteps()-1, because t=%d and nodes.getNumberOfTimeSteps()-1=%d", t, nodes.getNumberOfTimeSteps() - 1);

        final HashMap<Hypothesis<AdvancedComponent<FloatType>>, Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>>> ret = new HashMap<>();

        final List<Hypothesis<AdvancedComponent<FloatType>>> hyps = nodes.getHypothesesAt(t);

        if (hyps == null) return ret;

        for (final Hypothesis<AdvancedComponent<FloatType>> hyp : hyps) {
            try {
                final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> ora = getOptimalRightAssignment(hyp);
                if (ora != null) {
                    final HashSet<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> oneElemSet = new HashSet<>();
                    oneElemSet.add(ora);
                    ret.put(hyp, oneElemSet);
                }
            } catch (final GRBException e) {
                System.err.println("An optimal right assignment could not be determined!");
                e.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * Finds and returns the optimal right (to t+1) assignment given a
     * segmentation hypothesis.
     * For each segmentation hypothesis we know a set of outgoing edges
     * (assignments) that describe the interpretation (fate) of this segmented
     * cell. The ILP is set up such that only 1 such assignment can be chosen by
     * the convex optimizer during the computation of the optimal MAP
     * assignment.
     *
     * @return the optimal (choosen by the convex optimizer) assignment
     * describing the most likely data interpretation (MAP) towards the
     * next time-point.
     * @throws GRBException
     */
    public AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> getOptimalRightAssignment(final Hypothesis<AdvancedComponent<FloatType>> hypothesis) throws GRBException {
        return findActiveAssignment(edgeSets.getRightNeighborhood(hypothesis));
    }


    /**
     * Return set of all right-assignments that are outgoing from a given hypothesis.
     *
     * @param hypothesis
     * @return
     */
    public Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> getAllRightAssignmentsForHypothesis(final Hypothesis<AdvancedComponent<FloatType>> hypothesis) {
        return new HashSet<>(edgeSets.getRightNeighborhood(hypothesis)); /* return a new set of assignment references, so that caller cannot modify the underlying set of the ILP */
    }

    /**
     * Finds the active assignment in a set of assignments.
     * This method is thought to be called given a set that can only contain at
     * max 1 active assignment. (It will always and exclusively return the first
     * active assignment in the iteration order of the given set!)
     *
     * @return the one (first) active assignment in the given set of
     * assignments. (An assignment is active iff the binary ILP variable
     * associated with the assignment was set to 1 by the convex
     * optimizer!)
     * @throws GRBException
     */
    private AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> findActiveAssignment(final Iterable<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> set) throws GRBException {
        if (set == null) return null;

        for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> a : set) {
            if (a.isChosen()) {
                return a;
            }
        }
        return null;
    }

    /**
     * Collects and returns all inactive left-assignments given the optimal
     * segmentation.
     * An assignment in inactive, when it was NOT chosen by the ILP.
     * Only those assignments are collected that are left-edges from one of the
     * currently chosen (optimal) segmentation-hypotheses.
     *
     * @param t the time at which to look for inactive left-assignments.
     *          Values for t make only sense if <code>>=1</code> and
     *          <code>< nodes.getNumberOfTimeSteps().</code>
     * @return a hash-map that maps from segmentation hypothesis to a set of
     * assignments that (i) are NOT active, and (ii) come in from the
     * left (from t-1).
     */
    public HashMap<Hypothesis<AdvancedComponent<FloatType>>, Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>>> getInactiveLeftAssignments(final int t) {
        assert (t >= 1);
        assert (t < nodes.getNumberOfTimeSteps());

        final HashMap<Hypothesis<AdvancedComponent<FloatType>>, Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>>> ret = new HashMap<>();

        final List<Hypothesis<AdvancedComponent<FloatType>>> hyps = this.getOptimalHypotheses(t);

        for (final Hypothesis<AdvancedComponent<FloatType>> hyp : hyps) {
            try {
                final Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> set = edgeSets.getLeftNeighborhood(hyp);

                if (set == null) continue;

                for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> a : set) {
                    if (!a.isChosen()) {
                        Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> innerSet = ret.get(hyp);
                        if (innerSet == null) {
                            innerSet = new HashSet<>();
                            innerSet.add(a);
                            ret.put(hyp, innerSet);
                        } else {
                            innerSet.add(a);
                        }
                    }
                }
            } catch (final GRBException e) {
                System.err.println("Gurobi problem at getInactiveLeftAssignments(t)!");
                e.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * Collects and returns all inactive right-assignments given the optimal
     * segmentation.
     * An assignment in inactive, when it was NOT chosen by the ILP.
     * Only those assignments are collected that are right-edges from one of the
     * currently chosen (optimal) segmentation-hypotheses.
     *
     * @param t the time at which to look for inactive right-assignments.
     *          Values for t make only sense if <code>>=0</code> and
     *          <code>< nodes.getNumberOfTimeSteps()-1.</code>
     * @return a hash-map that maps from segmentation hypothesis to a set of
     * assignments that (i) are NOT active, and (ii) come in from the
     * right (from t+1).
     */
    public HashMap<Hypothesis<AdvancedComponent<FloatType>>, Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>>> getInactiveRightAssignments(final int t) {
        assert (t >= 0);
        assert (t < nodes.getNumberOfTimeSteps() - 1);

        final HashMap<Hypothesis<AdvancedComponent<FloatType>>, Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>>> ret = new HashMap<>();

        final List<Hypothesis<AdvancedComponent<FloatType>>> hyps = this.getOptimalHypotheses(t);

        for (final Hypothesis<AdvancedComponent<FloatType>> hyp : hyps) {
            try {
                final Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> set = edgeSets.getRightNeighborhood(hyp);

                if (set == null) continue;

                for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> a : set) {
                    if (!a.isChosen()) {
                        Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> innerSet = ret.get(hyp);
                        if (innerSet == null) {
                            innerSet = new HashSet<>();
                            innerSet.add(a);
                            ret.put(hyp, innerSet);
                        } else {
                            innerSet.add(a);
                        }
                    }
                }
            } catch (final GRBException e) {
                System.err.println("Gurobi problem at getInactiveRightAssignments(t)!");
                e.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * Collects and returns all left-assignments given the optimal segmentation.
     * Only those assignments are collected that are left-edges from one of the
     * currently chosen (optimal) segmentation-hypotheses.
     *
     * @param t the time at which to look for inactive left-assignments.
     *          Values for t make only sense if <code>>=1</code> and
     *          <code>< nodes.getNumberOfTimeSteps().</code>
     * @return a hash-map that maps from segmentation hypothesis to a set of
     * assignments that come in from the left (from t-1).
     */
    public HashMap<Hypothesis<AdvancedComponent<FloatType>>, Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>>> getAllCompatibleLeftAssignments(final int t) {
        assert (t >= 1);
        assert (t < nodes.getNumberOfTimeSteps());

        final HashMap<Hypothesis<AdvancedComponent<FloatType>>, Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>>> ret = new HashMap<>();

        final List<Hypothesis<AdvancedComponent<FloatType>>> hyps = this.getOptimalHypotheses(t);

        for (final Hypothesis<AdvancedComponent<FloatType>> hyp : hyps) {
            final Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> set = edgeSets.getLeftNeighborhood(hyp);

            if (set == null) continue;

            for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> a : set) {
                Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> innerSet = ret.get(hyp);
                if (innerSet == null) {
                    innerSet = new HashSet<>();
                    innerSet.add(a);
                    ret.put(hyp, innerSet);
                } else {
                    innerSet.add(a);
                }
            }
        }

        return ret;
    }

    /**
     * Collects and returns all right-assignments given the optimal
     * segmentation.
     * Only those assignments are collected that are right-edges from one of the
     * currently chosen (optimal) segmentation-hypotheses.
     *
     * @param t the time at which to look for inactive right-assignments.
     *          Values for t make only sense if <code>>=0</code> and
     *          <code>< nodes.getNumberOfTimeSteps()-1.</code>
     * @return a hash-map that maps from segmentation hypothesis to a set of
     * assignments that come in from the right (from t+1).
     */
    public HashMap<Hypothesis<AdvancedComponent<FloatType>>, Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>>> getAllRightAssignmentsThatStartFromOptimalHypothesesAt(final int t) {
        assert (t >= 0);
        assert (t < nodes.getNumberOfTimeSteps() - 1);

        final HashMap<Hypothesis<AdvancedComponent<FloatType>>, Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>>> ret = new HashMap<>();

        final List<Hypothesis<AdvancedComponent<FloatType>>> hyps = this.getOptimalHypotheses(t);

        for (final Hypothesis<AdvancedComponent<FloatType>> hyp : hyps) {
            final Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> set = edgeSets.getRightNeighborhood(hyp);

            if (set == null) continue;

            for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> a : set) {
                Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> innerSet = ret.get(hyp);
                if (innerSet == null) {
                    innerSet = new HashSet<>();
                    innerSet.add(a);
                    ret.put(hyp, innerSet);
                } else {
                    innerSet.add(a);
                }
            }
        }

        return ret;
    }

    /**
     * One of the powerful user interaction constraints.
     * This method constraints a frame to contain a given number of segments
     * (cells).
     *
     * @param t        The time-index. Must be in [0,nodes.getNumberOfTimeSteps()-2]
     * @param numCells the right hand side of the constraint.
     * @throws GRBException
     */
    public void addCellCountConstraint(final int t, final int numCells) throws GRBException {
        CellCountConstraint.addCellCountConstraint(t, numCells, model, nodes, edgeSets);
    }

    public CellCountConstraint getCellCountConstraint(int timeStep) {
        return CellCountConstraint.getCellCountConstraint(timeStep, model);
    }

    /**
     * Adds a constraint that forces a solution of this ILP to contain a certain
     * segment hypothesis.
     * To avoid requesting solutions that conflict with the tree constraints,
     * the second parameter can be the hypotheses at the same location for which
     * such a constraint exists so far.
     *
     * @param hyp2add the hypothesis for which the constraint should be installed.
     * @throws GRBException
     */
    public void addSegmentInSolutionConstraintAndRemoveConflictingSegmentConstraints(final Hypothesis<AdvancedComponent<FloatType>> hyp2add, final List<Hypothesis<AdvancedComponent<FloatType>>> hypothesesToRemove) throws GRBException {
        // Remove constraints form all given hypotheses
        if (!isNull(hypothesesToRemove)) {
            removeSegmentConstraints(hypothesesToRemove);
        }
        hyp2add.setIsForced(true);
    }

    /**
     * Remove constraints for the hypotheses in the provided list.
     *
     * @param hypothesesToRemove
     */
    private void removeSegmentConstraints(List<Hypothesis<AdvancedComponent<FloatType>>> hypothesesToRemove) {
        for (final Hypothesis<AdvancedComponent<FloatType>> hyp2remove : hypothesesToRemove) {
            removeSegmentConstraints(hyp2remove);
        }
    }

    /**
     * Remove constraints for the provided hypothesis.
     *
     * @param hypothesisToRemove
     */
    public void removeSegmentConstraints(Hypothesis<AdvancedComponent<FloatType>> hypothesisToRemove) {
        hypothesisToRemove.setIsForced(false);
        hypothesisToRemove.setIsForceIgnored(false);
    }

    /**
     * Adds a constraint that forces any solution of this ILP to avoid a certain
     * segment hypothesis.
     *
     * @param hyp2avoid
     * @throws GRBException
     */
    public void addSegmentNotInSolutionConstraint(final Hypothesis<AdvancedComponent<FloatType>> hyp2avoid) throws GRBException {
        hyp2avoid.setIsForceIgnored(true);
    }

    public void addProgressListener(final ProgressListener pl) {
        if (pl != null) {
            this.progressListener.add(pl);
        }
    }

    private void reportProgress() {
        for (final ProgressListener pl : this.progressListener) {
            pl.hasProgressed();
        }
    }

    /**
     * Adds storage lock constraints to all assignment variables in the Gurobi model. This enforces the previous state
     * of the Gurobi model, when it is read from disk and optimized when loading/restoring a previous curation.
     */
    public void addStorageLockConstraintsToAssignments() {
        for (AbstractAssignment assignment : nodes.getAllAssignments()) {
            assignment.addStorageLockConstraint();
        }
        try {
            model.update();
            model.optimize(); /* we need to optimize after adding constraints so that the model state fully defined, if we store it afterwards */
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes storage lock constraints from all assignment variables in the Gurobi model. This must be performed after
     * reading a Gurobi model from disk and optimizing it, so that the user can continue modifying it.
     */
    public void removeStorageLockConstraintsFromAssignments() {
        for (AbstractAssignment assignment : nodes.getAllAssignments()) {
            assignment.removeStorageLockConstraint();
        }
        try {
            model.update();
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param file
     */
    public void saveState(final File file) {
        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter(file));
            out.write(String.format("# moma_version: \"%s\"\n", versionString));
            out.newLine();

            // Write characteristics of dataset
            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            out.write(String.format("TIME, %d, %d, %d\n", gl.numberOfFrames(), configurationManager.getMinTime(), configurationManager.getMaxTime()));
            out.write(String.format("SIZE, %d, %d\n", nodes.getNumberOfHypotheses(), nodes.getTotalNumberOfAssignments()));
            out.newLine();

            final int timeOffset = configurationManager.getMinTime();

            // SegmentsInFrameCountConstraints
            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            out.write("# SegmentsInFrameCountConstraints\n");
            for (int t = 0; t < gl.numberOfFrames(); t++) {
                CellCountConstraint constraint = getCellCountConstraint(t);
                final int value = constraint.getNumberOfCells();
                if (value >= 0) {
                    out.write(String.format("\tSIFCC, %d, %d\n", t + timeOffset, value));
                }
            }

            // Include/Exclude Segment Constraints
            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            out.write("# SegmentSelectionConstraints (SSC)\n");
            for (int t = 0; t < gl.numberOfFrames(); t++) {
                final List<Hypothesis<AdvancedComponent<FloatType>>> hyps =
                        nodes.getHypothesesAt(t);
                for (final Hypothesis<AdvancedComponent<FloatType>> hyp : hyps) {
                    if (hyp.getSegmentSpecificConstraint() != null) {
                        double rhs;
                        try {
                            rhs = hyp.getSegmentSpecificConstraint().get(GRB.DoubleAttr.RHS);
                            out.write(String.format(
                                    "\tSSC, %d, %d, %s, %s\n",
                                    t + timeOffset,
                                    hyp.getId(),
                                    rhs,
                                    hyp.getStringId()));
                        } catch (final GRBException e) {
//							out.write( String.format( "\tSSC, %d, %d, GUROBI_ERROR\n", t + timeOffset, hyp.getId() ) );
                        }
                    }
                }
            }

            // Include/Exclude Assignment Constraints
            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            out.write("# AssignmentSelectionConstraints (ASC)\n");
            for (int t = 0; t < gl.numberOfFrames(); t++) {
                final List<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> assmnts =
                        nodes.getAssignmentsAt(t);
                for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assmnt : assmnts) {
                    if (assmnt.getGroundTruthConstraint() != null) {
                        double rhs;
                        try {
                            rhs = assmnt.getGroundTruthConstraint().get(GRB.DoubleAttr.RHS);
                            out.write(String.format(
                                    "\tASC, %d, %d, %s, %s\n",
                                    t + timeOffset,
                                    assmnt.getId(),
                                    rhs,
                                    assmnt.getStringId()));
                        } catch (final GRBException e) {
//							out.write( String.format("\tASC, %d, %d, GUROBI_ERROR\n", t + timeOffset, assmnt.getId() ) );
                        }
                    }
                }
            }

            // Pruning Roots
            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            out.write("# PruningRoots (PR)\n");
            for (int t = 0; t < gl.numberOfFrames(); t++) {
                final List<Hypothesis<AdvancedComponent<FloatType>>> hyps =
                        nodes.getHypothesesAt(t);
                for (final Hypothesis<AdvancedComponent<FloatType>> hyp : hyps) {
                    if (hyp.isPruneRoot()) {
                        out.write(String.format("\tPR, %d, %d, %s\n", t + timeOffset, hyp.getId(), hyp.getStringId()));
                    }
                }
            }

            out.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void loadPruneRoots(final File file) throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        final List<Hypothesis<?>> pruneRoots = new ArrayList<>();
        final int timeOffset = configurationManager.getMinTime();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().startsWith("#") || line.trim().length() == 0) continue;
            final String[] columns = line.split(",");
            if (columns.length > 1) {
                final String constraintType = columns[0].trim();
                // Read Pruning Roots
                // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                if (constraintType.equals("PR")) {
                    try {
                        final int t = Integer.parseInt(columns[1].trim()) - timeOffset;
                        final int id = Integer.parseInt(columns[2].trim());
                        System.out.println(String.format("PR %d %d", t, id));
                        final List<Hypothesis<AdvancedComponent<FloatType>>> hyps =
                                nodes.getHypothesesAt(t);
                        for (final Hypothesis<AdvancedComponent<FloatType>> hyp : hyps) {
                            if (hyp.getId() == id) {
                                pruneRoots.add(hyp);
                            }
                        }
                    } catch (final NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        reader.close();

        for (final Hypothesis<?> hyp : pruneRoots) {
            hyp.setPruneRoot(true);
        }
        MoMA.getGui().dataToDisplayChanged();
    }

    /**
     * @param file
     * @throws IOException
     */
    public void loadState(final File file) throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(file));

        final List<Hypothesis<?>> pruneRoots = new ArrayList<>();

        final int timeOffset = configurationManager.getMinTime();

        String line;
        while ((line = reader.readLine()) != null) {
            // ignore comments and empty lines
            if (line.trim().startsWith("#") || line.trim().length() == 0) continue;

            final String[] columns = line.split(",");
            if (columns.length > 1) {
                final String constraintType = columns[0].trim();

                // DataProperties (to see if this load makes any sense)
                // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                if (constraintType.equals("TIME")) {
                    final int readNumT = Integer.parseInt(columns[1].trim());
                    final int readTmin = Integer.parseInt(columns[2].trim());
                    final int readTmax = Integer.parseInt(columns[3].trim());

                    if (configurationManager.getMinTime() != readTmin || configurationManager.getMaxTime() != readTmax) {
                        if (!configurationManager.getIfRunningHeadless()) {
                            JOptionPane.showMessageDialog(
                                    MoMA.getGui(),
                                    "Tracking to be loaded is at best a partial fit.\nMatching data will be loaded whereever possible...",
                                    "Warning",
                                    JOptionPane.WARNING_MESSAGE);
                        } else {
                            System.out.println("Tracking to be loaded is at most a partial fit. Continue to load matching data...");
                            System.exit(946);
                        }
                    }
                }
                if (constraintType.equals("SIZE")) {
                    final int readNumH = Integer.parseInt(columns[1].trim());
                    final int readNumA = Integer.parseInt(columns[2].trim());
                }

                // SegmentsInFrameCountConstraints
                // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                if (constraintType.equals("SIFCC")) {
                    try {
                        final int t = Integer.parseInt(columns[1].trim()) - timeOffset;
                        final int numCells = Integer.parseInt(columns[2].trim());
                        try {
                            System.out.println(String.format("SIFCC %d %d", t, numCells));
                            this.addCellCountConstraint(t, numCells);
                        } catch (final GRBException e) {
                            e.printStackTrace();
                        }
                    } catch (final NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                // SegmentationConstraints
                // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                if (constraintType.equals("SSC")) {
                    try {
                        final int t = Integer.parseInt(columns[1].trim()) - timeOffset;
                        final int id = Integer.parseInt(columns[2].trim());
                        final double rhs = Double.parseDouble(columns[3].trim());
                        try {
                            System.out.println(String.format("SSC %d %d %f", t, id, rhs));
                            final List<Hypothesis<AdvancedComponent<FloatType>>> hyps =
                                    nodes.getHypothesesAt(t);
                            for (final Hypothesis<AdvancedComponent<FloatType>> hyp : hyps) {
                                if (hyp.getId() == id) {
                                    if (1 == (int) rhs) {
                                        addSegmentInSolutionConstraintAndRemoveConflictingSegmentConstraints(hyp, null);
                                    } else {
                                        addSegmentNotInSolutionConstraint(hyp);
                                    }
                                }
                            }
                        } catch (final GRBException e) {
                            e.printStackTrace();
                        }
                    } catch (final NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                // AssignmentConstraints
                // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                if (constraintType.equals("ASC")) {
                    try {
                        final int t = Integer.parseInt(columns[1].trim()) - timeOffset;
                        final int id = Integer.parseInt(columns[2].trim());
                        final double rhs = Double.parseDouble(columns[3].trim());
                        System.out.println(String.format("ASC %d %d %f", t, id, rhs));
                        final List<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> assmnts =
                                nodes.getAssignmentsAt(t);
                        for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assmnt : assmnts) {
                            if (assmnt.getId() == id) {
                                if (1 == (int) rhs) {
                                    assmnt.setGroundTruth(true);
                                } else {
                                    assmnt.setGroundUntruth(true);
                                }
                            }
                        }
                    } catch (final NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                // Pruning Roots
                // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                if (constraintType.equals("PR")) {
                    try {
                        final int t = Integer.parseInt(columns[1].trim()) - timeOffset;
                        final int id = Integer.parseInt(columns[2].trim());
                        System.out.println(String.format("PR %d %d", t, id));
                        final List<Hypothesis<AdvancedComponent<FloatType>>> hyps =
                                nodes.getHypothesesAt(t);
                        for (final Hypothesis<AdvancedComponent<FloatType>> hyp : hyps) {
                            if (hyp.getId() == id) {
                                pruneRoots.add(hyp);
                            }
                        }
                    } catch (final NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        reader.close();

        try {
            model.update();
            run();
        } catch (final GRBException e) {
            e.printStackTrace();
        }

        // Activate all PruneRoots
        for (final Hypothesis<?> hyp : pruneRoots) {
            hyp.setPruneRoot(true);
        }
        MoMA.getGui().dataToDisplayChanged();
    }

    /**
     * @param t
     */
    public synchronized void fixSegmentationAsIs(final int t) {
        final List<Hypothesis<AdvancedComponent<FloatType>>> hyps =
                nodes.getHypothesesAt(t);
        for (final Hypothesis<AdvancedComponent<FloatType>> hyp : hyps) {
            // only if hypothesis is not already clamped
            if (hyp.getSegmentSpecificConstraint() == null) {
                Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> nh;
                nh = edgeSets.getRightNeighborhood(hyp);

                try {
                    final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> aa =
                            findActiveAssignment(nh);
                    if (aa != null) {
                        // fix this segment
                        addSegmentInSolutionConstraintAndRemoveConflictingSegmentConstraints(hyp, null);
                    } else {
                        // avoid this segment
                        addSegmentNotInSolutionConstraint(hyp);
                    }
                } catch (final GRBException e) {
                    //				e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param t
     */
    public synchronized void fixAssignmentsAsAre(final int t) {
        // TODO: don't forget that assignment constraints removal kills also fixed segmentation
        final List<Hypothesis<AdvancedComponent<FloatType>>> hyps =
                nodes.getHypothesesAt(t);
        for (final Hypothesis<AdvancedComponent<FloatType>> hyp : hyps) {
            Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> nh;
            nh = edgeSets.getRightNeighborhood(hyp);
            if (nh == null) continue;
            for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assmnt : nh) {
                if (assmnt.getGroundTruthConstraint() == null) {
                    try {
                        if (assmnt.isChosen()) {
                            assmnt.setGroundTruth(true);
                        } else {
                            assmnt.setGroundUntruth(true);
                        }
                    } catch (final GRBException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * @param t
     */
    public synchronized void removeAllSegmentConstraints(final int t) {
        final List<Hypothesis<AdvancedComponent<FloatType>>> hyps =
                nodes.getHypothesesAt(t);
        removeSegmentConstraints(hyps);
    }

    /**
     * @param t
     */
    public void removeAllAssignmentConstraints(final int t) {
        final List<Hypothesis<AdvancedComponent<FloatType>>> hyps =
                nodes.getHypothesesAt(t);
        for (final Hypothesis<AdvancedComponent<FloatType>> hyp : hyps) {
            Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> nh;
            nh = edgeSets.getRightNeighborhood(hyp);
            if (nh == null) continue;
            for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assmnt : nh) {
                if (assmnt.getGroundTruthConstraint() != null) {
                    assmnt.setGroundTruth(false);
                }
            }
        }
    }

    /**
     * Ignore all assignments after the end of the tracking range that the user specified.
     *
     * @param tStart: time step after which assignments will be ignored
     */
    public void addPostOptimizationRangeLockConstraintsAfter(final int tStart) {
        for (int i = 0; i < tStart + 1; i++) {
            removePostOptimizationRangeLockConstraintsAt(i);
        }
        try {
            model.update();
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
        for (int i = tStart + 1; i < gl.numberOfFrames(); i++) {
            addPostOptimizationRangeLockConstraintsAt(i);
        }
        try {
            model.update();
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }

        int numberOfPostOptimRangeLockConstraints = getNumberOfPostOptimRangeLockConstraints();
        int numberOfAssignments = getNumberAssignmentsAfter(tStart);
        if(numberOfPostOptimRangeLockConstraints != numberOfAssignments){
            throw new AssertionError(String.format("numberOfPostOptimRangeLockConstraints (=%d) does not equal numberOfAssignments (=%d) after the time step tStart=%d", numberOfPostOptimRangeLockConstraints, numberOfAssignments, tStart));
        }
        if ((tStart + 1 == gl.numberOfFrames()) && (numberOfPostOptimRangeLockConstraints != 0)) {
            throw new AssertionError(String.format("numberOfPostOptimRangeLockConstraints (=%d) is not zero", numberOfPostOptimRangeLockConstraints));
        }
        int retrievedTimeStep = getLastTimeStepWithoutPostOptimizationRangeConstraint();
        if (tStart != retrievedTimeStep) {
            throw new AssertionError(String.format("method getLastTimeStepWithoutPostOptimizationRangeConstraint reported a value (=%d) that differs from the expected time step (=%d).", retrievedTimeStep, tStart));
        }
    }

    /**
     * @param t
     */
    private void removePostOptimizationRangeLockConstraintsAt(final int t) {
        List<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> assignments = nodes.getAssignmentsAt(t);
        for (AbstractAssignment<?> assignment : assignments) {
            assignment.removePostOptimizationRangeLockConstraint();
        }
    }

    /**
     * @param t
     */
    public void addPostOptimizationRangeLockConstraintsAt(int t) {
        List<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> assignments = nodes.getAssignmentsAt(t);
        for (AbstractAssignment<?> assignment : assignments) {
            if (assignment.hasPostOptimizationRangeLockConstraint()) {
                return; /* if one assignment has an optimization-range constraint lock, then all will have it; we can therefore return immediately */
            }
            assignment.addPostOptimizationRangeLockConstraint();
        }
    }

    private int getNumberAssignmentsAfter(int timeStep) {
        int total = 0;
        for (int t = timeStep+1; t < gl.numberOfFrames(); t++) {
            total += getNumberOfAssignmentsAt(t);
        }
        return total;
    }

    private int getNumberAssignmentsBefore(int timeStep) {
        int total = 0;
        for (int t = 0; t < timeStep; t++) {
            total += getNumberOfAssignmentsAt(t);
        }
        return total;
    }

    private int getNumberOfAssignmentsAt(int timeStep){
        return nodes.getAssignmentsAt(timeStep).size();
    }

    private int getNumberOfPostOptimRangeLockConstraints() {
        return getNumberConstraintsContaining(AbstractAssignment.getPostOptimizationRangeConstraintNamePrefix());
    }

    private int getNumberOfPreOptimRangeLockConstraints() {
        return getNumberConstraintsContaining(AbstractAssignment.getPreOptimizationRangeConstraintNamePrefix());
    }

    private int getNumberConstraintsContaining(String substring) {
        int numberOfConstraints = 0;
        GRBConstr[] constraintList = model.getConstrs();
        try {
            for (GRBConstr constr : constraintList) {
                String constrName = constr.get(GRB.StringAttr.ConstrName);
                if (constrName.contains(substring)) {
                    numberOfConstraints++;
                }
            }
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
        return numberOfConstraints;
    }

    /***
     * Freezes all assignments before time step t, so that they will not change during optimization.
     *
     * @param tEnd: time step before which to freeze
     */
    public void addPreOptimizationRangeLockConstraintsBefore(final int tEnd) {
        for (int t = tEnd-1; t < gl.numberOfFrames(); t++) {
            removePreOptimizationRangeLockConstraintsAt(t);
        }
        try {
            model.update();
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
        for (int t = 0; t < tEnd; t++) {
            addPreOptimizationRangeLockConstraintsAt(t);
        }
        try {
            model.update();
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }

        int numberOfPreOptimRangeLockConstraints = getNumberOfPreOptimRangeLockConstraints();
        int numberOfAssignments = getNumberAssignmentsBefore(tEnd);
        if(numberOfPreOptimRangeLockConstraints != numberOfAssignments){
            throw new AssertionError(String.format("numberOfPreOptimRangeLockConstraints (=%d) does not equal numberOfAssignments (=%d) before time step tEnd=%d", numberOfPreOptimRangeLockConstraints,numberOfAssignments,tEnd));
        }
        if ((tEnd == 0) && (numberOfPreOptimRangeLockConstraints != 0)) {
            throw new AssertionError(String.format("numberOfPreOptimRangeLockConstraints (=%d) is not zero", numberOfPreOptimRangeLockConstraints));
        }
        int retrievedTimeStep = getFirstTimeStepWithoutPreOptimizationRangeConstraint();
        if (tEnd != retrievedTimeStep) {
            throw new AssertionError(String.format("method getFirstTimeStepWithoutPreOptimizationRangeConstraint reported a value (=%d) that differs from the expected time step (=%d).", retrievedTimeStep, tEnd));
        }
    }

    /**
     * This method adds constraints for all outgoing assignments of hypotheses at time t, so that these assignments will
     * be forced to their current selection-state. This is done by setting the value coefficients in
     * such a way that the equality expression of the constraint can only be fulfilled, when active assignments are
     * maintained active and inactive assignments are maintained inactive.
     */
    public void addPreOptimizationRangeLockConstraintsAt(int t) {
        List<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> assignments = nodes.getAssignmentsAt(t);
        for (AbstractAssignment<?> assignment : assignments) {
            if (assignment.hasPreOptimizationRangeLockConstraint()) {
                return; /* if one assignment has an optimization-range constraint lock, then all will have it; we can therefore return immediately */
            }
            assignment.addPreOptimizationRangeLockConstraint();
        }
    }

    private void removePreOptimizationRangeLockConstraintsAt(final int t) {
        List<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> assignments = nodes.getAssignmentsAt(t);
        for (AbstractAssignment<?> assignment : assignments) {
            assignment.removePreOptimizationRangeLockConstraint();
        }
    }

    public void setRemoveStorageLockConstraintAfterFirstOptimization() {
        removeStorageLockConstraintAfterFirstOptimization = true;
    }

    public void addDialogManger(IDialogManager dialogManager) {
        this.dialogManager = dialogManager;
    }

    private List<ChangeListener> listenerList = new ArrayList<>();

    public void addChangeListener(ChangeListener l) {
        listenerList.add(l);
    }

    public void fireStateChanged() {
        for (ChangeListener listener : listenerList) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }

    public int getOptimizationRangeStart() {
        return getFirstTimeStepWithoutPreOptimizationRangeConstraint();
    }

    private int getFirstTimeStepWithoutPreOptimizationRangeConstraint() {
        int tStart = 0;
        for (int t = 0; t < gl.numberOfFrames(); t++) {
            if (assignmentsHavePreOptimizationRangeConstraintAt(t)) {
                tStart = t + 1;
            }
        }
        return tStart;
    }

    private boolean assignmentsHavePreOptimizationRangeConstraintAt(int t){
        List<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> assignments = nodes.getAssignmentsAt(t);
        if (assignments.isEmpty()) {
            return false; /* if no assignments exist for this time-step, then no constraints exist */
        }
        for (AbstractAssignment<?> assignment : assignments) {
            if (assignment.hasPreOptimizationRangeLockConstraint()) {
                return true;
            }
        }
        return false;
    }

    public int getOptimizationRangeEnd() {
        return getLastTimeStepWithoutPostOptimizationRangeConstraint();
    }

    private int getLastTimeStepWithoutPostOptimizationRangeConstraint() {
        int tEnd = gl.numberOfFrames() - 1;
        for (int t = gl.numberOfFrames() - 1; t >= 0; t--) {
            if (assignmentsHavePostOptimizationRangeConstraintAt(t)) {
                tEnd = t - 1;
            }
        }
        return tEnd;
    }

    private boolean assignmentsHavePostOptimizationRangeConstraintAt(int t){
        List<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> assignments = nodes.getAssignmentsAt(t);
        if (assignments.isEmpty()) {
            return false; /* if no assignments exist for this time-step, then no constraints exist */
        }
        for (AbstractAssignment<?> assignment : assignments) {
            if (assignment.hasPostOptimizationRangeLockConstraint()) {
                return true;
            }
        }
        return false;
    }

    public List<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> getAllAssignments() {
        return nodes.getAllAssignments();
    }

    public List<Hypothesis<AdvancedComponent<FloatType>>> getAllHypotheses() {
        return nodes.getAllHypotheses();
    }
}
