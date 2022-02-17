package com.jug.export;

import com.jug.lp.GRBModel.IGRBModelAdapter;
import gurobi.GRBException;

import java.io.File;
import java.util.List;
import java.util.function.Supplier;

public class IlpModelExporter implements ResultExporterInterface {
    private Supplier<String> defaultFilenameDecorationSupplier;

    public IlpModelExporter(Supplier<String> defaultFilenameDecorationSupplier) {
        this.defaultFilenameDecorationSupplier = defaultFilenameDecorationSupplier;
    }

    @Override
    public void export(ResultExporterData resultData) throws GRBException {
        File outputFolder = resultData.getOutputFolder();
        List<SegmentRecord> cellTrackStartingPoints = resultData.getCellTrackStartingPoints();
        String filename = defaultFilenameDecorationSupplier.get();
        IGRBModelAdapter model = resultData.getIlpModel();
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
