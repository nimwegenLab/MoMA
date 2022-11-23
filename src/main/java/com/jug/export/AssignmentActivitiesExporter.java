package com.jug.export;

import com.jug.Growthlane;
import com.jug.datahandling.IGlExportFilePathGetter;
import com.jug.lp.AbstractAssignment;
import com.jug.lp.GrowthlaneTrackingILP;
import com.jug.util.ITimer;
import gurobi.GRBException;

import java.io.*;

public class AssignmentActivitiesExporter implements ResultExporterInterface {
    private final GrowthlaneTrackingILP ilp;
    private ITimer timer;

    public AssignmentActivitiesExporter(Growthlane growthlane, ITimer timer) {
        this.ilp = growthlane.getIlp();
        this.timer = timer;
    }

    @Override
    public void export(Growthlane gl, IGlExportFilePathGetter exportFilePaths) throws GRBException {
        timer.start();
        ResultTable table = new ResultTable(",");

        ResultTableColumn<String> idCol = table.getColumn(String.class, "id");
        ResultTableColumn<Integer> isActiveCol = table.getColumn(Integer.class, "is_active");
        ResultTableColumn<Integer> isGroundTruthCol = table.getColumn(Integer.class, "is_forced_by_user");
        ResultTableColumn<Integer> isGroundUntruthCol = table.getColumn(Integer.class, "is_ignored_by_user");
        ResultTableColumn<Integer> isPrunedCol = table.getColumn(Integer.class, "is_pruned_by_user");

        for (AbstractAssignment<?> assignment : ilp.getAllAssignments()) {
            if(assignment.isChoosen() || assignment.isGroundTruth() || assignment.isGroundUntruth() || assignment.isPruned()) {
                idCol.addValue(assignment.getStringId());
                isActiveCol.addValue(assignment.isChoosen() ? 1 : 0);
                isGroundTruthCol.addValue(assignment.isGroundTruth() ? 1 : 0);
                isGroundUntruthCol.addValue(assignment.isGroundUntruth() ? 1 : 0);
                isPrunedCol.addValue(assignment.isPruned() ? 1 : 0);
            }
        }

        File outputCsvFile = exportFilePaths.getAssignmentActivitiesCsvFilePath().toFile();
        try {
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(outputCsvFile));
            try {
                out.write(String.format("# Comment: This file lists only assignments that are true for at least one of the categories. The file containing the assignment costs lists all assignments.\n"));
                table.writeTable(out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        timer.stop();
        timer.printExecutionTime("Timer result for AssignmentActivitiesExporter");
    }
}
