package com.jug.export;

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
    public void export(ResultExporterData resultData) throws GRBException {
        File outputFolder = resultData.getOutputFolder();

        resultData.getGrowthlaneTrackingILP().lockAssignmentsForStorage();

        IGRBModelAdapter model = resultData.getIlpModel();

        int ignoreNamesVal = model.get(GRB.IntParam.IgnoreNames);
        System.out.println(String.format("ignoreNamesVal: %d", ignoreNamesVal));

        String outputPath = outputFolder.getAbsolutePath() + "/ilpModel.lp";
        model.write(outputPath);
        String outputPath2 = outputFolder.getAbsolutePath() + "/ilpModel.mps";
        model.write(outputPath2);
        String outputPath3 = outputFolder.getAbsolutePath() + "/ilpModel.sol";
        model.write(outputPath3);
//        String outputPath4 = outputFolder.getAbsolutePath() + "/ilpModel.json";
//        model.write(outputPath4);
        String outputPath5 = outputFolder.getAbsolutePath() + "/ilpModel.mst";
        model.write(outputPath5);
//        System.out.println("stop");
    }
}
