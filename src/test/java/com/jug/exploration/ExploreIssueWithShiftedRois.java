package com.jug.exploration;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;

import java.awt.*;

public class ExploreIssueWithShiftedRois {
    public static void main(String[] args){
        test1();
    }

    /**
     * This test establishes that the close polygon become misaligned from polyline, when saved and reopened. This is
     * not the case, when displaying directly from code.
     */
    public static void test1(){
        String CHANGE_THIS_PATH = "/home/micha/TemporaryFiles/20220119_imagej_overlays/overlay_test_image_3.tif";
        ImagePlus imp = IJ.createHyperStack("HyperStack", 100, 100, 1, 1, 1, 8);
        PolygonRoi polygonRoi = new PolygonRoi(new float[]{25, 75, 75, 25}, new float[]{25, 25, 75, 75}, Roi.POLYGON);
//        PolygonRoi polygonRoi = new PolygonRoi(new float[]{25, 75, 75, 25}, new float[]{25, 25, 75, 75}, Roi.POLYLINE); /* this will align the rectangle with the vertical polyline */
//        PolygonRoi polygonRoi = new PolygonRoi(new float[]{25.5f, 75.5f, 75.5f, 25.5f}, new float[]{25.5f, 25.5f, 75.5f, 75.5f}, Roi.POLYGON); /* this will align the rectangle to the vertical polyline */
        polygonRoi.setPosition(1,1,1);
        polygonRoi.setStrokeColor(Color.BLUE);
//        PolygonRoi polylineRoi = new PolygonRoi(new float[]{50, 50}, new float[]{25, 75}, Roi.POLYGON); /* this will align the vertical line with the rectangle polygon */
        PolygonRoi polylineRoi = new PolygonRoi(new float[]{50, 50}, new float[]{25, 75}, Roi.POLYLINE);
//        PolygonRoi polylineRoi = new PolygonRoi(new float[]{50.5f, 50.5f}, new float[]{25, 75}, Roi.POLYLINE);
        polylineRoi.setPosition(1,1,1);
        polylineRoi.setStrokeColor(Color.RED);
        Overlay overlay = new Overlay();
        overlay.add(polygonRoi);
        overlay.add(polylineRoi);
        imp.setOverlay(overlay);
        IJ.save(imp, CHANGE_THIS_PATH);
        imp.show();
    }
}
