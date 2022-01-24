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
        IGRBModelAdapter tmp = resultData.getIlpModel();
        String outputPath = outputFolder.getAbsolutePath() + "/ilpModel.lp";
        tmp.write(outputPath);
        String outputPath2 = outputFolder.getAbsolutePath() + "/ilpModel.mps";
        tmp.write(outputPath2);
//        System.out.println("stop");
    }
}
