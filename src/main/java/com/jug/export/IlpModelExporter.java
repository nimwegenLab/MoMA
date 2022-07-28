package com.jug.export;

import com.jug.Growthlane;
import com.jug.datahandling.IGlExportFilePaths;
import com.jug.lp.GRBModel.IGRBModelAdapter;
import gurobi.GRB;
import gurobi.GRBException;

import java.io.File;
import java.util.function.Supplier;

public class IlpModelExporter implements ResultExporterInterface {
    private final Supplier<String> defaultFilenameDecorationSupplier;

    public IlpModelExporter(Supplier<String> defaultFilenameDecorationSupplier) {
        this.defaultFilenameDecorationSupplier = defaultFilenameDecorationSupplier;
    }

    @Override
    public void export(Growthlane gl, IGlExportFilePaths exportFilePaths) throws GRBException {
        File outputFolder = exportFilePaths.getOutputPath().toFile();

        gl.getIlp().addStorageLockConstraintsToAssignments();

        IGRBModelAdapter model = gl.getIlp().model;

//        int ignoreNamesVal = model.get(GRB.IntParam.IgnoreNames);
//        System.out.println(String.format("ignoreNamesVal: %d", ignoreNamesVal));

        String outputPath = outputFolder.getAbsolutePath() + "/gurobi_model.lp";
        model.write(outputPath);
        String outputPath2 = outputFolder.getAbsolutePath() + "/gurobi_model.mps";
        model.write(outputPath2);
        String outputPath3 = outputFolder.getAbsolutePath() + "/gurobi_model.sol";
        model.write(outputPath3);
//        String outputPath4 = outputFolder.getAbsolutePath() + "/gurobi_model.json";
//        model.write(outputPath4);
        String outputPath5 = outputFolder.getAbsolutePath() + "/gurobi_model.mst";
        model.write(outputPath5);
//        System.out.println("stop");
        gl.getIlp().removeStorageLockConstraintsFromAssignments();
    }
}
