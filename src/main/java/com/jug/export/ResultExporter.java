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

    public void export(Growthlane gl, File outputFolder, GrowthlaneFrame firstGLF) {
        try {
            List<SegmentRecord> cellTrackStartingPoints = gl.getCellTrackStartingPoints(firstGLF);
            ResultExporterData resultData = new ResultExporterData(outputFolder, cellTrackStartingPoints, firstGLF.getParent().getIlp().model, firstGLF.getParent().getIlp());
            for(ResultExporterInterface exporter: exporters){
                exporter.export(resultData);
            }
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }
}
