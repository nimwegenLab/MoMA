package com.jug.export;

import com.jug.lp.GRBModel.IGRBModelAdapter;
import com.jug.lp.GrowthlaneTrackingILP;

import java.io.File;
import java.util.List;

public class ResultExporterData {
    private File outputFolder;
    private List<SegmentRecord> cellTrackStartingPoints;
    private IGRBModelAdapter ilpModel;
    private GrowthlaneTrackingILP growthlaneTrackingILP;

    public ResultExporterData(File outputFolder, List<SegmentRecord> cellTrackStartingPoints, IGRBModelAdapter ilpModel, GrowthlaneTrackingILP growthlaneTrackingILP) {
        this.outputFolder = outputFolder;
        this.cellTrackStartingPoints = cellTrackStartingPoints;
        this.ilpModel = ilpModel;
        this.growthlaneTrackingILP = growthlaneTrackingILP;
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

    public GrowthlaneTrackingILP getGrowthlaneTrackingILP() {
        return growthlaneTrackingILP;
    }
}
