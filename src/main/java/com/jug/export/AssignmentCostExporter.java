package com.jug.export;

import com.jug.Growthlane;
import com.jug.datahandling.IGlExportFilePathGetter;
import com.jug.lp.AbstractAssignment;
import com.jug.lp.GrowthlaneTrackingILP;
import com.jug.lp.Hypothesis;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * This class exports the cost values for all assignments that belong to the segments that were selected as part of the
 * tracking solution.
 */
public class AssignmentCostExporter implements ResultExporterInterface {
    private ResultTable table;
    private GrowthlaneTrackingILP ilp;
    private Growthlane growthlane;
    private ResultTableColumn<String> assignmentIdCol;
    private ResultTableColumn<String> assignmentTypeCol;
    private ResultTableColumn<Integer> frameCol;
    private ResultTableColumn<Object> assignmentCostCol;

    public AssignmentCostExporter(Growthlane growthlane) {
        this.ilp = growthlane.getIlp();
        this.growthlane = growthlane;
    }

    @Override
    public void export(Growthlane gl, IGlExportFilePathGetter exportFilePaths) {
        double start = System.currentTimeMillis();
        System.out.println("Exporting assignment costs...");
        table = new ResultTable(",");
        assignmentIdCol = table.getColumn(String.class, "id");
        assignmentTypeCol = table.getColumn(String.class, "type");
        frameCol = table.getColumn(Integer.class, "source_frame");
        assignmentCostCol = table.addColumn(new ResultTableColumn<>("cost"));

        int tmax = growthlane.getFrames().size();
        for (int t = 0; t < tmax; t++) {
            List<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> assignmentList = new ArrayList(ilp.getAssignmentsAt(t));
            assignmentList.sort(Comparator.comparing(a -> a.getStringId()));
            exportAssignmentInformation(assignmentList);
        }
        File outputCsvFile = exportFilePaths.getAssignmentCostsCsvFilePath().toFile();
        try {
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(outputCsvFile));
            try {
                out.write(String.format("# Comment: This file lists costs and additional information on all assignments in the tracking problem.\n"));
                table.writeTable(out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        double end = System.currentTimeMillis();
        System.out.println("Execution time: " + (end-start)/1000.0);
        System.out.println("");
    }

    private void exportAssignmentInformation(List<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> allAssignments){
        for (AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assignment : allAssignments){

            /* Write results to table */
            assignmentIdCol.addValue(assignment.getStringId());
            frameCol.addValue(assignment.getSourceTimeStep());
            this.assignmentCostCol.addValue(assignment.getCost());

            if(assignment.getType() == ilp.ASSIGNMENT_EXIT) {
                assignmentTypeCol.addValue("exit");
            }
            if (assignment.getType() == GrowthlaneTrackingILP.ASSIGNMENT_MAPPING) {
                assignmentTypeCol.addValue("map");
            }
            if(assignment.getType() == ilp.ASSIGNMENT_DIVISION) {
                assignmentTypeCol.addValue("div");
            }
            if(assignment.getType() == ilp.ASSIGNMENT_LYSIS) {
                assignmentTypeCol.addValue("lys");
            }
        }
    }
}
