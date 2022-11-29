package com.jug.export;

import com.jug.Growthlane;
import com.jug.GrowthlaneFrame;
import com.jug.datahandling.IGlExportFilePathGetter;
import com.jug.lp.AbstractAssignment;
import com.jug.lp.GrowthlaneTrackingILP;
import com.jug.lp.Hypothesis;
import com.jug.lp.costs.CostFactory;
import com.jug.util.ComponentTreeUtils;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.ComponentProperties;
import gurobi.GRBException;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

import java.io.*;
import java.util.List;
import java.util.Set;

/**
 * This class exports the cost values for all assignments that belong to the segments that were selected as part of the
 * tracking solution.
 */
public class AssignmentCostExporterOld implements ResultExporterInterface {
    private final ResultTable resultTable;
    private final ResultTableColumn<String> componentId;
    private final ResultTableColumn<String> parentId;
    private final ResultTableColumn<String> rootId;
    private final ResultTableColumn<String> child1Id;
    private final ResultTableColumn<String> child2Id;
    private final ResultTableColumn<String> target1Id;
    private final ResultTableColumn<String> target2Id;
    private final ResultTableColumn<Integer> frameCol;
    private final ResultTableColumn<String> assignmentTypeCol;
    private final ResultTableColumn<Integer> assignmentInIlpSolutionCol;
    private final ResultTableColumn<Integer> cellRankCol;
    private final ResultTableColumn<Float> assignmentCostCol;
    private final ResultTableColumn<Integer> sourceHypInIlpSolutionCol;
    private final ResultTableColumn<Integer> componentTreeNodeLevelCol;
    private final ResultTableColumn<Double> offLikelihoodForComponentCol;
    private final ResultTableColumn<Double> offLogLikelihoodForComponentCol;
//    private final ResultTableColumn<Double> offLogLikelihoodForCompatibleChildNodesCol;
    private final ResultTableColumn<Double> onLikelihoodForComponentCol;
    private final ResultTableColumn<Double> onLogLikelihoodForComponentCol;
//    private final ResultTableColumn<Double> onLogLikelihoodForCompatibleChildNodesCol;
    private final ResultTableColumn<Double> offLikelihoodForComponentWatershedLineCol;
    private final ResultTableColumn<Double> offLogLikelihoodForComponentWatershedLineCol;
    private final ResultTableColumn<Double> maxLikelihoodLowerThanOneCol;
    private final ResultTableColumn<Double> minLikelihoodLargerThanZeroCol;
    private final ResultTableColumn<Double> onLikelihoodForComponentWatershedLineCol;
    private final ResultTableColumn<Double> onLogLikelihoodForComponentWatershedLineCol;
    ResultTableColumn<Integer> sourceTopLimitCol;
    ResultTableColumn<Integer> sourceBottomLimitCol;
    private final ResultTableColumn<Integer> target1TopLimitCol;
    private final ResultTableColumn<Integer> target1BottomLimitCol;
    private final ResultTableColumn<Integer> target2TopLimitCol;
    private final ResultTableColumn<Integer> target2BottomLimitCol;
    ResultTableColumn<Integer> sourceAreaCol;
    ResultTableColumn<Integer> target1AreaCol;
    ResultTableColumn<Integer> target2AreaCol;
    ResultTableColumn<Double> sourceEllipseMajorCol;
    ResultTableColumn<Double> target1EllipseMajorCol;
    ResultTableColumn<Double> target2EllipseMajorCol;
    ResultTableColumn<Double> sourceYCol;
    ResultTableColumn<Double> target1CenterYCol;
    ResultTableColumn<Double> target2CenterYCol;
    private GrowthlaneTrackingILP ilp;
    private Growthlane growthlane;
    private ComponentProperties componentProperties;
    private CostFactory costFactory;

    public AssignmentCostExporterOld(Growthlane growthlane, ComponentProperties componentProperties, CostFactory costFactory) {
        this.ilp = growthlane.getIlp();
        this.growthlane = growthlane;
        this.componentProperties = componentProperties;
        this.costFactory = costFactory;
        this.resultTable = new ResultTable(",");
        this.frameCol = resultTable.addColumn(new ResultTableColumn<>("frame"));
        this.assignmentTypeCol = resultTable.addColumn(new ResultTableColumn<>("assignment_type"));
        this.assignmentCostCol = resultTable.addColumn(new ResultTableColumn<>("assignment_cost"));
        this.assignmentInIlpSolutionCol = resultTable.addColumn(new ResultTableColumn<>("assignment_in_ilp_solution"));
        this.sourceHypInIlpSolutionCol = resultTable.addColumn(new ResultTableColumn<>("source_hypothesis_is_in_solution"));
        this.cellRankCol = resultTable.addColumn(new ResultTableColumn<>("source_cell_rank"));
        sourceAreaCol = resultTable.addColumn(new ResultTableColumn<>("source_area_px"));
        target1AreaCol = resultTable.addColumn(new ResultTableColumn<>("target_1_area_px"));
        target2AreaCol = resultTable.addColumn(new ResultTableColumn<>("target_2_area_px"));
        sourceEllipseMajorCol = resultTable.addColumn(new ResultTableColumn<>("source_ellipse_major_axis_px", "%.3f"));
        target1EllipseMajorCol = resultTable.addColumn(new ResultTableColumn<>("target_1_ellipse_major_axis_px", "%.3f"));
        target2EllipseMajorCol = resultTable.addColumn(new ResultTableColumn<>("target_2_ellipse_major_axis_px", "%.3f"));
        sourceYCol = resultTable.addColumn(new ResultTableColumn<>("source_center_y_px", "%.3f"));
        target1CenterYCol = resultTable.addColumn(new ResultTableColumn<>("target_1_center_y_px", "%.3f"));
        target2CenterYCol = resultTable.addColumn(new ResultTableColumn<>("target_1_center_y_px", "%.3f"));
        sourceTopLimitCol = resultTable.addColumn(new ResultTableColumn<>("source_top_px"));
        sourceBottomLimitCol = resultTable.addColumn(new ResultTableColumn<>("source_bottom_px"));
        target1TopLimitCol = resultTable.addColumn(new ResultTableColumn<>("target_1_top_px"));
        target1BottomLimitCol = resultTable.addColumn(new ResultTableColumn<>("target_1_bottom_px"));
        target2TopLimitCol = resultTable.addColumn(new ResultTableColumn<>("target_2_top_px"));
        target2BottomLimitCol = resultTable.addColumn(new ResultTableColumn<>("target_2_bottom_px"));
        componentTreeNodeLevelCol = resultTable.addColumn(new ResultTableColumn<>("source_component_tree_level"));
        offLikelihoodForComponentCol = resultTable.addColumn(new ResultTableColumn<>("likelihood_for_component_off"));
        offLogLikelihoodForComponentCol = resultTable.addColumn(new ResultTableColumn<>("log_likelihood_for_component_off"));
        onLikelihoodForComponentCol = resultTable.addColumn(new ResultTableColumn<>("likelihood_for_component_on"));
        onLogLikelihoodForComponentCol = resultTable.addColumn(new ResultTableColumn<>("log_likelihood_for_component_on"));
        offLikelihoodForComponentWatershedLineCol = resultTable.addColumn(new ResultTableColumn<>("likelihood_for_component_watershed_line_off"));
        offLogLikelihoodForComponentWatershedLineCol = resultTable.addColumn(new ResultTableColumn<>("log_likelihood_for_component_watershed_line_off"));
        onLikelihoodForComponentWatershedLineCol = resultTable.addColumn(new ResultTableColumn<>("likelihood_for_component_watershed_line_on"));
        onLogLikelihoodForComponentWatershedLineCol = resultTable.addColumn(new ResultTableColumn<>("log_likelihood_for_component_watershed_line_on"));
        maxLikelihoodLowerThanOneCol = resultTable.addColumn(new ResultTableColumn<>("max_likelihood_lower_than_one"));
        minLikelihoodLargerThanZeroCol = resultTable.addColumn(new ResultTableColumn<>("min_likelihood_larger_than_zero"));
        componentId = resultTable.addColumn(new ResultTableColumn<>("component_id"));
        parentId = resultTable.addColumn(new ResultTableColumn<>("parent_id"));
        rootId = resultTable.addColumn(new ResultTableColumn<>("root_id"));
        child1Id = resultTable.addColumn(new ResultTableColumn<>("child_1_id"));
        child2Id = resultTable.addColumn(new ResultTableColumn<>("child_2_id"));
        target1Id = resultTable.addColumn(new ResultTableColumn<>("target_1_id"));
        target2Id = resultTable.addColumn(new ResultTableColumn<>("target_2_id"));
//        onLogLikelihoodForCompatibleChildNodesCol = resultTable.addColumn(new ResultTableColumn<>("log_likelihood_for_compatible_child_components_on"));
//        offLogLikelihoodForCompatibleChildNodesCol = resultTable.addColumn(new ResultTableColumn<>("log_likelihood_for_compatible_child_components_off"));
    }

    @Override
    public void export(Growthlane gl, IGlExportFilePathGetter exportFilePaths) {
        System.out.println("Exporting assignment costs...");

        int tmax = growthlane.getFrames().size();
        for (int t = 0; t < tmax; t++) {
            Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> allAssignments = ilp.getAssignmentsAt(t);
            exportAllAssignmentInformationForHypothesisNew(t, allAssignments);
        }
        exportFilePaths.makeExportDataOutputDirectory();
        File outputCsvFile = exportFilePaths.getAssignmentCostsCsvFilePath().toFile();
        try {
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(outputCsvFile));
            try {
                resultTable.writeTable(out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

//    private int counter = 0;

    private void exportAllAssignmentInformationForHypothesisNew(int frame, Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> allAssignments){
        System.out.println("frame: " + frame);

        final GrowthlaneFrame glf = growthlane.getFrames().get(frame);
        for (AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assignment : allAssignments){
            boolean assignmentInIlpSolution;
            try {
                assignmentInIlpSolution = assignment.isChosen();
            } catch (GRBException err) {
                assignmentInIlpSolution = false;
            }
            Hypothesis<AdvancedComponent<FloatType>> sourceHypothesis = assignment.getSourceHypothesis();
            List<Hypothesis<AdvancedComponent<FloatType>>> targetHypotheses = assignment.getTargetHypotheses();

//            System.out.println("counter: " + counter);
//            counter++;
//            System.out.println("frame: " + frame);

            componentId.addValue(sourceHypothesis.getWrappedComponent().getStringId());
            if(sourceHypothesis.getWrappedComponent().getParent() != null) {
                parentId.addValue(sourceHypothesis.getWrappedComponent().getParent().getStringId());
            } else{
                parentId.addValue("NA");
            }

            if(sourceHypothesis.getWrappedComponent().getRoot() != null) {
                rootId.addValue(sourceHypothesis.getWrappedComponent().getRoot().getStringId());
            } else{
                rootId.addValue("NA");
            }

            List<AdvancedComponent<FloatType>> children = sourceHypothesis.getWrappedComponent().getChildren();
            if (children.isEmpty()) {
                child1Id.addValue("NA");
                child2Id.addValue("NA");
            } else {
                child1Id.addValue(children.get(0).getStringId());
                if (children.size() > 1) {
                    child2Id.addValue(children.get(1).getStringId());
                } else {
                    child2Id.addValue("NA");
                }
            }

            componentTreeNodeLevelCol.addValue(sourceHypothesis.getWrappedComponent().getNodeLevel());

            Pair<Double, Double> minMaxTuple = costFactory.getValuesForLikelihoodCalculation(ilp.getAllComponentsInIlp());

            double offLikelihoodForComponent = costFactory.getOffLikelihoodForComponent(sourceHypothesis.getWrappedComponent(), minMaxTuple);
            offLikelihoodForComponentCol.addValue(offLikelihoodForComponent);
            double offLogLikelihoodForComponent = costFactory.getOffLogLikelihoodForComponent(sourceHypothesis.getWrappedComponent(), minMaxTuple);
            offLogLikelihoodForComponentCol.addValue(offLogLikelihoodForComponent);
            double onLikelihoodForComponent = costFactory.getOnLikelihoodForComponent(sourceHypothesis.getWrappedComponent(), minMaxTuple);
            onLikelihoodForComponentCol.addValue(onLikelihoodForComponent);
            double onLogLikelihoodForComponent = costFactory.getOnLogLikelihoodForComponent(sourceHypothesis.getWrappedComponent(), minMaxTuple);
            onLogLikelihoodForComponentCol.addValue(onLogLikelihoodForComponent);
            double onLikelihoodForComponentWatershedLine = costFactory.getOnLikelihoodForComponentWatershedLine(sourceHypothesis.getWrappedComponent(), minMaxTuple);
            onLikelihoodForComponentWatershedLineCol.addValue(onLikelihoodForComponentWatershedLine);
            double onLogLikelihoodForComponentWatershedLine = costFactory.getOnLogLikelihoodForComponentWatershedLine(sourceHypothesis.getWrappedComponent(), minMaxTuple);
            onLogLikelihoodForComponentWatershedLineCol.addValue(onLogLikelihoodForComponentWatershedLine);
            double offLikelihoodForComponentWatershedLine = costFactory.getOffLikelihoodForComponentWatershedLine(sourceHypothesis.getWrappedComponent(), minMaxTuple);
            offLikelihoodForComponentWatershedLineCol.addValue(offLikelihoodForComponentWatershedLine);
            double offLogLikelihoodForComponentWatershedLine = costFactory.getOffLogLikelihoodForComponentWatershedLine(sourceHypothesis.getWrappedComponent(), minMaxTuple);
            offLogLikelihoodForComponentWatershedLineCol.addValue(offLogLikelihoodForComponentWatershedLine);
            Pair<Double, Double> likelihoodExtrema = sourceHypothesis.getWrappedComponent().getPixelValueExtremaInsideRange(0.0, 1.0);
            minLikelihoodLargerThanZeroCol.addValue(likelihoodExtrema.getA());
            maxLikelihoodLowerThanOneCol.addValue(likelihoodExtrema.getB());

            int cellRank;
            if (assignmentInIlpSolution) {
                cellRank = glf.getSolutionStats_cellRank(sourceHypothesis); /* output the cell-rank, if assignment is part of the ILP solution */
            } else {
                cellRank = -1;
            }
            boolean sourceHypthesisInIlpSolution = ilp.isSelected(sourceHypothesis);

            /* Write results to table */
            cellRankCol.addValue(cellRank);
            assignmentInIlpSolutionCol.addValue(assignmentInIlpSolution ? 1 : 0);
            frameCol.addValue(frame);
            this.assignmentCostCol.addValue(assignment.getCost());
            this.sourceHypInIlpSolutionCol.addValue(sourceHypthesisInIlpSolution ? 1 : 0);

            AdvancedComponent<FloatType> sourceComponent = sourceHypothesis.getWrappedComponent();
            addComponentAreaToTable(sourceComponent, sourceAreaCol);
            addComponentLengthToTable(sourceComponent, sourceEllipseMajorCol);
            addComponentCenterYToTable(sourceComponent, sourceYCol);
            addComponentLimitsToTable(sourceComponent, sourceTopLimitCol, sourceBottomLimitCol);

            AdvancedComponent<FloatType> targetComponent1 = null;
            AdvancedComponent<FloatType> targetComponent2 = null;
            if(assignment.getType() == ilp.ASSIGNMENT_EXIT) {
                assignmentTypeCol.addValue("exit");
            }
            if (assignment.getType() == GrowthlaneTrackingILP.ASSIGNMENT_MAPPING) {
                assignmentTypeCol.addValue("map");
                targetComponent1 = targetHypotheses.get(0).getWrappedComponent();
            }
            if(assignment.getType() == ilp.ASSIGNMENT_DIVISION) {
                assignmentTypeCol.addValue("div");
                targetComponent1 = targetHypotheses.get(0).getWrappedComponent();
                targetComponent2 = targetHypotheses.get(1).getWrappedComponent();
            }
            if(assignment.getType() == ilp.ASSIGNMENT_LYSIS) {
                assignmentTypeCol.addValue("lys");
            }


            addComponentIdToTable(targetComponent1, target1Id); /* assign targetComponent1 to target2id, so that the cell-rank order corresponds to that of the child-component */
            addComponentAreaToTable(targetComponent1, target1AreaCol);
            addComponentLengthToTable(targetComponent1, target1EllipseMajorCol);
            addComponentCenterYToTable(targetComponent1, target1CenterYCol);
            addComponentLimitsToTable(targetComponent1, target1TopLimitCol, target1BottomLimitCol);

            addComponentIdToTable(targetComponent2, target2Id); /* assign targetComponent2 to target1id, so that the cell-rank order corresponds to that of the child-component */
            addComponentAreaToTable(targetComponent2, target2AreaCol);
            addComponentLengthToTable(targetComponent2, target2EllipseMajorCol);
            addComponentCenterYToTable(targetComponent2, target2CenterYCol);
            addComponentLimitsToTable(targetComponent2, target2TopLimitCol, target2BottomLimitCol);
        }
    }

    void addComponentCenterYToTable(AdvancedComponent<FloatType> component, ResultTableColumn<Double> column) {
        Double value = -1.0;
        if (component != null){
            value = componentProperties.getCentroid(component).getB();
        }
        column.addValue(value);
    }

    void addComponentLengthToTable(AdvancedComponent<FloatType> component, ResultTableColumn<Double> column) {
        Double value = -1.0;
        if (component != null){
            value = componentProperties.getMinorMajorAxis(component).getB();
        }
        column.addValue(value);
    }

    void addComponentIdToTable(AdvancedComponent<FloatType> component, ResultTableColumn<String> column) {
        String value = "NA";
        if (component != null){
            value = component.getStringId();
        }
        column.addValue(value);
    }

    void addComponentAreaToTable(AdvancedComponent<FloatType> component, ResultTableColumn<Integer> column) {
        Integer value = -1;
        if (component != null){
            value = componentProperties.getArea(component);
        }
        column.addValue(value);
    }

    void addComponentLimitsToTable(AdvancedComponent<FloatType> component, ResultTableColumn<Integer> cellTopCol, ResultTableColumn<Integer> cellBottomCol) {
        Integer cellBboxTop = -1;
        Integer cellBboxBottom = -1;
        if (component != null){
            ValuePair<Integer, Integer> limits = ComponentTreeUtils.getTreeNodeInterval(component);
            cellBboxTop = limits.getA();
            cellBboxBottom = limits.getB();
        }
        cellTopCol.addValue(cellBboxTop);
        cellBottomCol.addValue(cellBboxBottom);
    }
}
