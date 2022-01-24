package com.jug.export;

import com.jug.lp.GRBModel.IGRBModelAdapter;

import java.io.File;
import java.util.List;

public class ResultExporterData {
    private File outputFolder;
    private List<SegmentRecord> cellTrackStartingPoints;
    private IGRBModelAdapter ilpModel;

    public ResultExporterData(File outputFolder, List<SegmentRecord> cellTrackStartingPoints, IGRBModelAdapter ilpModel) {
        this.outputFolder = outputFolder;
        this.cellTrackStartingPoints = cellTrackStartingPoints;
        this.ilpModel = ilpModel;
    }

    public File getOutputFolder() {
        return outputFolder;
    }

    public List<SegmentRecord> getCellTrackStartingPoints() {
        return cellTrackStartingPoints;
    }

    public IGRBModelAdapter getIlpModel() {
        return ilpModel;
    }
}
