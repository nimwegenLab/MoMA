package com.jug.export;

import com.jug.Growthlane;
import com.jug.GrowthlaneFrame;
import com.jug.lp.AbstractAssignment;
import com.jug.lp.GrowthlaneTrackingILP;
import com.jug.lp.Hypothesis;
import com.jug.util.ComponentTreeUtils;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.ComponentProperties;
import gurobi.GRBException;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;

import java.io.*;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * This class exports the cost values for all assignments that belong to the segments that were selected as part of the
 * tracking solution.
 */
public class AssignmentCostExporter implements ResultExporterInterface {
    private final ResultTable resultTable;
    private final ResultTableColumn<Integer> frameCol;
    private final ResultTableColumn<String> assignmentTypeCol;
    private final ResultTableColumn<Integer> assignmentInIlpSolutionCol;
    private final ResultTableColumn<Integer> cellRankCol;
    private final ResultTableColumn<Float> assignmentCostCol;
    private final ResultTableColumn<Integer> sourceHypInIlpSolutionCol;
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
    private Supplier<String> defaultFilenameDecorationSupplier;
    private ComponentProperties componentProperties;

    public AssignmentCostExporter(Growthlane growthlane, Supplier<String> defaultFilenameDecorationSupplier, ComponentProperties componentProperties) {
        this.ilp = growthlane.getIlp();
        this.growthlane = growthlane;
        this.defaultFilenameDecorationSupplier = defaultFilenameDecorationSupplier;
        this.componentProperties = componentProperties;
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
        sourceYCol = resultTable.addColumn(new ResultTableColumn<>("source_center_x_px", "%.3f"));
        target1CenterYCol = resultTable.addColumn(new ResultTableColumn<>("target_1_center_x_px", "%.3f"));
        target2CenterYCol = resultTable.addColumn(new ResultTableColumn<>("target_1_center_x_px", "%.3f"));
        sourceTopLimitCol = resultTable.addColumn(new ResultTableColumn<>("source_top_px"));
        sourceBottomLimitCol = resultTable.addColumn(new ResultTableColumn<>("source_bottom_px"));
        target1TopLimitCol = resultTable.addColumn(new ResultTableColumn<>("target_1_top_px"));
        target1BottomLimitCol = resultTable.addColumn(new ResultTableColumn<>("target_1_bottom_px"));
        target2TopLimitCol = resultTable.addColumn(new ResultTableColumn<>("target_2_top_px"));
        target2BottomLimitCol = resultTable.addColumn(new ResultTableColumn<>("target_2_bottom_px"));
    }

    @Override
    public void export(File outputFolder, List<SegmentRecord> cellTrackStartingPoints) {
        System.out.println("Exporting assignment costs...");

        int tmax = growthlane.getFrames().size();
        for (int t = 0; t < tmax; t++) {
            Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> allAssignments = ilp.getAssignmentsAt(t);
            exportAllAssignmentInformationForHypothesisNew(t, allAssignments);
            System.out.println("t: " + t);
        }
        File outputCsvFile = new File(outputFolder, "AssignmentCosts__" + defaultFilenameDecorationSupplier.get() + ".csv");
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

    private void exportAllAssignmentInformationForHypothesisNew(int frame, Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> allAssignments){
        final GrowthlaneFrame glf = growthlane.getFrames().get(frame);
        for (AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assignment : allAssignments){
            boolean assignmentInIlpSolution;
            try {
                assignmentInIlpSolution = assignment.isChoosen();
            } catch (GRBException err) {
                assignmentInIlpSolution = false;
            }
            Hypothesis<AdvancedComponent<FloatType>> sourceHypothesis = assignment.getSourceHypothesis();
            List<Hypothesis<AdvancedComponent<FloatType>>> targetHypotheses = assignment.getTargetHypotheses();

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

            addComponentAreaToTable(targetComponent1, target1AreaCol);
            addComponentLengthToTable(targetComponent1, target1EllipseMajorCol);
            addComponentCenterYToTable(targetComponent1, target1CenterYCol);
            addComponentLimitsToTable(targetComponent1, target1TopLimitCol, target1BottomLimitCol);

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