package com.jug.export;

import com.jug.Growthlane;
import com.jug.GrowthlaneFrame;
import gurobi.GRBException;

import java.io.File;
import java.util.List;

public class ResultExporter {
    private List<ResultExporterInterface> exporters;

    public ResultExporter(List<ResultExporterInterface> exporters) {
        this.exporters = exporters;
    }

    public void export(Growthlane gl) {
        try {
            ResultExporterData resultData = new ResultExporterData(gl);
            for(ResultExporterInterface exporter: exporters){
                exporter.export(resultData);
            }
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }
}
