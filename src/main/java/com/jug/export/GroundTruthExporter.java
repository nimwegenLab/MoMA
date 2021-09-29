package com.jug.export;

import org.apache.commons.lang.NotImplementedException;

import java.io.File;
import java.util.List;

public class GroundTruthExporter {
    public void export(File outputFolder, List<SegmentRecord> cellTrackStartingPoints){
        SegmentRecord firstEntry = cellTrackStartingPoints.get(0);
        System.out.println("stop");
    }

    public void createGroundTruthTiffStacks(int nrOfFrames) {
        // TODO: Implement generation of GT stacks
        throw new NotImplementedException();
    }
}
