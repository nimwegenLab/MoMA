package com.jug.export;

import com.jug.Growthlane;
import com.jug.GrowthlaneFrame;
import com.jug.lp.AbstractAssignment;
import com.jug.lp.GrowthlaneTrackingILP;
import com.jug.lp.Hypothesis;
import com.jug.util.componenttree.AdvancedComponent;
import gurobi.GRBException;
import net.imglib2.type.numeric.real.FloatType;

import javax.security.sasl.SaslServer;
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
    private final ResultTableColumn<Integer> cellIdCol;
    private final ResultTableColumn<String> assignmentTypeCol;
    private final ResultTableColumn<Integer> assignmentInIlpSolutionCol;
    private final ResultTableColumn<Integer> cellRankCol;
    private final ResultTableColumn<Float> assignmentCostCol;
    private GrowthlaneTrackingILP ilp;
    private Growthlane growthlane;
    private Supplier<String> defaultFilenameDecorationSupplier;

    public AssignmentCostExporter(Growthlane growthlane, Supplier<String> defaultFilenameDecorationSupplier) {
        this.ilp = growthlane.getIlp();
        this.growthlane = growthlane;
        this.defaultFilenameDecorationSupplier = defaultFilenameDecorationSupplier;
        this.resultTable = new ResultTable(",");
        this.cellIdCol = resultTable.addColumn(new ResultTableColumn<>("cell_ID"));
        this.frameCol = resultTable.addColumn(new ResultTableColumn<>("frame"));
        this.cellRankCol = resultTable.addColumn(new ResultTableColumn<>("cell_rank"));
        this.assignmentTypeCol = resultTable.addColumn(new ResultTableColumn<>("assignment_type"));
        this.assignmentInIlpSolutionCol = resultTable.addColumn(new ResultTableColumn<>("assignment_in_ilp_solution"));
        this.assignmentCostCol = resultTable.addColumn(new ResultTableColumn<>("assignment_cost"));
    }

    @Override
    public void export(File outputFolder, List<SegmentRecord> cellTrackStartingPoints) {
        for (SegmentRecord segmentRecord : cellTrackStartingPoints) {
            do {
                int frame = segmentRecord.timestep;
                final GrowthlaneFrame glf = growthlane.getFrames().get(frame);

                final int cellRank = glf.getSolutionStats_cellRank(segmentRecord.hyp);
                final int segmentId = segmentRecord.getId();
                exportAllAssignmentInformationForHypothesis(frame, segmentRecord.hyp, cellRank, segmentId);

                segmentRecord = segmentRecord.nextSegmentInTime();
            }
            while (segmentRecord.exists());
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

    private void exportAllAssignmentInformationForHypothesis(int frame, Hypothesis<AdvancedComponent<FloatType>> hypothesis, int cellRank, int segmentId){
        try {
            AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> activeAssignment = ilp.getOptimalRightAssignment(hypothesis);
            Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> allAssignments = ilp.getAllRightAssignmentsForHypothesis(hypothesis);
            if(!allAssignments.contains(activeAssignment)){
                throw new RuntimeException("There is no active assignment for the selected hypothesis. This is not allowed to happen.");
            }
            allAssignments.remove(activeAssignment);
            outputAssignmentInformationToTable(frame, activeAssignment, true, cellRank, segmentId);
            for (AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assignment : allAssignments) {
                outputAssignmentInformationToTable(frame, assignment, false, cellRank, segmentId);
            }
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

    private void outputAssignmentInformationToTable(int frame, AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assignment, boolean assignmentInIlpSolution, int cellRank, int segmentId) {
        cellIdCol.addValue(segmentId);
        cellRankCol.addValue(cellRank);
        assignmentInIlpSolutionCol.addValue(assignmentInIlpSolution ? 1 : 0);
        frameCol.addValue(frame);
        if(assignment.getType() == ilp.ASSIGNMENT_EXIT) assignmentTypeCol.addValue("exit");
        if(assignment.getType() == ilp.ASSIGNMENT_MAPPING) assignmentTypeCol.addValue("map");
        if(assignment.getType() == ilp.ASSIGNMENT_DIVISION) assignmentTypeCol.addValue("div");
        if(assignment.getType() == ilp.ASSIGNMENT_LYSIS) assignmentTypeCol.addValue("lys");
        this.assignmentCostCol.addValue(assignment.getCost());
    }
}
