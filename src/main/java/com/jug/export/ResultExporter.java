package com.jug.export;

import com.jug.Growthlane;
import com.jug.datahandling.IGlExportFilePaths;
import gurobi.GRBException;

import java.io.File;
import java.util.List;

public class ResultExporter {
    private List<ResultExporterInterface> exporters;

    public ResultExporter(List<ResultExporterInterface> exporters) {
        this.exporters = exporters;
    }

    public void export(Growthlane gl, IGlExportFilePaths exportFilePaths) {
        try {
//            ResultExporterData resultData = new ResultExporterData(gl);
            for(ResultExporterInterface exporter: exporters){
                exporter.export(gl, exportFilePaths);
            }
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }
}
