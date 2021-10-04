package com.jug.export;

import com.jug.MoMA;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GroundTruthFramesExporter {
    private final List<Integer> listOfTimeSteps;

    public GroundTruthFramesExporter() {
        listOfTimeSteps = new ArrayList<>();
    }

    public void addFrame(Integer timeStepToDisplay) {
        listOfTimeSteps.add(timeStepToDisplay);
    }

    public void removeFrame(Integer timeStepToDisplay) {
        listOfTimeSteps.remove(timeStepToDisplay);
    }

    public void export(File outputFolder, List<SegmentRecord> cellTrackStartingPoints) { /* Note: the unused argument cellTrackStartingPoints is to maintain signature compatibility with other exporters */
        ResultTable resultTable = new ResultTable(",");
        ResultTableColumn<Integer> timeStepColumn = resultTable.addColumn(new ResultTableColumn<>("time_step"));

        for (int frameNr : listOfTimeSteps){
            timeStepColumn.addValue(frameNr);
        }

        File outputFile = new File(outputFolder, "GroundTruthFrames_" + MoMA.getDefaultFilenameDecoration() + ".csv");
        String path = outputFile.getAbsolutePath();
        OutputStreamWriter out = null;
        try {
            out = new OutputStreamWriter(new FileOutputStream(path));
            resultTable.writeTable(out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
