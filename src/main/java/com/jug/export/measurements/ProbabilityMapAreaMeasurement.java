package com.jug.export.measurements;

import com.jug.export.ResultTable;
import com.jug.util.componenttree.ComponentInterface;
import net.imglib2.algorithm.morphology.Dilation;
import net.imglib2.roi.Regions;
import net.imglib2.roi.util.PositionableInterval;


public class ProbabilityMapAreaMeasurement implements SegmentMeasurementInterface {
    public ProbabilityMapAreaMeasurement() {
//        new Dilation
    }


    @Override
    public void setOutputTable(ResultTable outputTable) {

    }

    @Override
    public void measure(ComponentInterface component) {
//        PositionableInterval
//        component.getRegion();

//        Regions.sample(orientedBoundingBoxPolygon)
//        ImgLabeling.getIndexImg
    }
}
