package com.jug.export;

import com.jug.Growthlane;
import com.jug.datahandling.IGlExportFilePathGetter;
import com.jug.lp.GrowthlaneTrackingILP;
import gurobi.GRBException;

import java.io.File;

public class AssignmentActivitiesExporter implements ResultExporterInterface {
    private final GrowthlaneTrackingILP ilp;
    private final Growthlane growthlane;

    public AssignmentActivitiesExporter(Growthlane growthlane) {
        this.ilp = growthlane.getIlp();
        this.growthlane = growthlane;
    }

    @Override
    public void export(Growthlane gl, IGlExportFilePathGetter exportFilePaths) throws GRBException {
        ResultTable table = new ResultTable(",");

        ResultTableColumn<String> idCol = table.getColumn(String.class, "id");
        ResultTableColumn<String> isActiveCol = table.getColumn(String.class, "is_active");

//        for(ilp.getAllAssignments)

        File csvFile = exportFilePaths.getAssignmentActivitiesCsvFilePath().toFile();

    }
}
