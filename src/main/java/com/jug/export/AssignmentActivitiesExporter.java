package com.jug.export;

import com.jug.Growthlane;
import com.jug.config.IConfiguration;
import com.jug.datahandling.IGlExportFilePathGetter;
import com.jug.lp.AbstractAssignment;
import com.jug.lp.GrowthlaneTrackingILP;
import com.jug.util.ITimer;
import gurobi.GRBException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.*;

public class AssignmentActivitiesExporter implements ResultExporterInterface {
    private final GrowthlaneTrackingILP ilp;
    private ITimer timer;
    private IConfiguration configuration;

    public AssignmentActivitiesExporter(Growthlane growthlane, ITimer timer, IConfiguration configuration) {
        this.ilp = growthlane.getIlp();
        this.timer = timer;
        this.configuration = configuration;
    }

    @Override
    public void export(Growthlane gl, IGlExportFilePathGetter exportFilePaths) throws GRBException {
        timer.start();
        ResultTable table = new ResultTable(",");

        ResultTableColumn<String> idCol = table.getColumn(String.class, "id");
        ResultTableColumn<Integer> isActiveCol = table.getColumn(Integer.class, "is_active");
        ResultTableColumn<Integer> isGroundTruthCol = table.getColumn(Integer.class, "is_forced");
        ResultTableColumn<Integer> isGroundUntruthCol = table.getColumn(Integer.class, "is_force_ignored");
        ResultTableColumn<Integer> isPrunedCol = table.getColumn(Integer.class, "is_pruned");

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
                if (!configuration.getIsReloading()) {
                    createCopyAtFirstRun(outputCsvFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        timer.stop();
        timer.printExecutionTime("Timer result for AssignmentActivitiesExporter");
    }

    /**
     * Creates a copy of the activities files during the tracking of the GL. This file will reflect the ILP state after
     * initial tracking before any curation was performed by the user.
     *
     * @param sourceFile
     * @throws IOException
     */
    private void createCopyAtFirstRun(File sourceFile) throws IOException {
        String sourcePath = sourceFile.getParent();
        String sourceName = FilenameUtils.getBaseName(sourceFile.getName());
        String sourceExtension = FilenameUtils.getExtension(sourceFile.getName());
        Path targetPath = Paths.get(sourcePath, sourceName + "_initial." + sourceExtension);
        if (!targetPath.toFile().exists()) {
            FileUtils.copyFile(sourceFile, targetPath.toFile());
        }
    }
}
