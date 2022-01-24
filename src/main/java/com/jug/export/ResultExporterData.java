package com.jug.export;

import java.io.File;
import java.util.List;

public class ResultExporterData {
    private File outputFolder;
    private List<SegmentRecord> cellTrackStartingPoints;

    public ResultExporterData(File outputFolder, List<SegmentRecord> cellTrackStartingPoints) {
        this.outputFolder = outputFolder;
        this.cellTrackStartingPoints = cellTrackStartingPoints;
    }

    public File getOutputFolder() {
        return outputFolder;
    }

    public List<SegmentRecord> getCellTrackStartingPoints() {
        return cellTrackStartingPoints;
    }
}
