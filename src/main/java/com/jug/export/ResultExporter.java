package com.jug.export;

import com.jug.Growthlane;
import com.jug.datahandling.IGlExportFilePathGetter;
import gurobi.GRBException;

import java.util.List;
import java.util.Objects;

public class ResultExporter {
    private List<ResultExporterInterface> exporters;

    public ResultExporter(List<ResultExporterInterface> exporters) {
        this.exporters = Objects.requireNonNull(exporters);
    }

    public void export(Growthlane gl, IGlExportFilePathGetter exportFilePaths) {
        try {
            for(ResultExporterInterface exporter: exporters){
                exporter.export(gl, exportFilePaths);
            }
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }
}
