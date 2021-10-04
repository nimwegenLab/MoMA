package com.jug.export;

import com.jug.GrowthlaneFrame;
import gurobi.GRBException;

import java.io.File;
import java.util.List;

public class ResultExporter {
    private CellStatsExporter cellStatsExporter;
    private CellMaskExporter groundTruthExporter;
    private GroundTruthFramesExporter groundTruthFramesExporter;
    private List<ResultExporterInterface> exporters;

    public ResultExporter(List<ResultExporterInterface> exporters) {
        this.exporters = exporters;
        this.cellStatsExporter = cellStatsExporter;
        this.groundTruthExporter = groundTruthExporter;
        this.groundTruthFramesExporter = groundTruthFramesExporter;
    }

    public void export(File outputFolder, int tmax, GrowthlaneFrame firstGLF) {
        try {
            List<SegmentRecord> cellTrackStartingPoints = getCellTrackStartingPoints(firstGLF, tmax);
            for(ResultExporterInterface exporter: exporters){
                exporter.export(outputFolder, cellTrackStartingPoints);
            }
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

    private List<SegmentRecord> getCellTrackStartingPoints(GrowthlaneFrame firstGLF, int tmax) throws GRBException {
        CellTrackBuilder trackBuilder = new CellTrackBuilder();
        trackBuilder.buildSegmentTracks(firstGLF.getSortedActiveHypsAndPos(),
                firstGLF,
                firstGLF.getParent().getIlp(),
                tmax);
        List<SegmentRecord> startingPoints = trackBuilder.getStartingPoints();
        return startingPoints;
    }
}
