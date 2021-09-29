package com.jug.export;

import com.jug.GrowthlaneFrame;
import gurobi.GRBException;

import java.io.File;
import java.util.List;

public class ResultExporter {
    private CellStatsExporter cellStatsExporter;
    private GroundTruthExporter groundTruthExporter;

    public ResultExporter(CellStatsExporter cellStatsExporter, GroundTruthExporter groundTruthExporter) {
        this.cellStatsExporter = cellStatsExporter;
        this.groundTruthExporter = groundTruthExporter;
    }

    public void export(File outputFolder, int tmax, GrowthlaneFrame firstGLF) {
        try {
            List<SegmentRecord> cellTrackStartingPoints = getCellTrackStartingPoints(firstGLF, tmax);
            cellStatsExporter.export(outputFolder, cellTrackStartingPoints);
            groundTruthExporter.export(outputFolder, cellTrackStartingPoints);
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
