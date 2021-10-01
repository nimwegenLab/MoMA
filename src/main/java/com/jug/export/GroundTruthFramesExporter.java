package com.jug.export;

import java.util.ArrayList;
import java.util.List;

public class GroundTruthFramesExporter {
    private final List<Integer> listOfFrames;

    public GroundTruthFramesExporter() {
        listOfFrames = new ArrayList<>();
    }

    public void addFrame(Integer timeStepToDisplay) {
        listOfFrames.add(timeStepToDisplay);
    }

    public void removeFrame(Integer timeStepToDisplay) {
        listOfFrames.remove(timeStepToDisplay);
    }

//    public export(){
//
//    }
}
