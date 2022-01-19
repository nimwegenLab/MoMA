package com.jug.exploration;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;

import java.awt.*;

public class ExploreRoiOverlays {
    public static void main(String... args){
//        explorationTest1();
//        explorationTest2();
        explorationTest3();
    }

    private static void explorationTest1() {
        ImagePlus imp = IJ.createImage("HyperStack", "8-bit grayscale-mode label", 400, 300, 3, 4, 5);
//IJ.setTool("text");
        IJ.run(imp, "Select All", "");
        IJ.run("Properties... ", "");
//IJ.setTool("rectangle");
        IJ.run(imp, "Properties... ", "  position=1,1,1");
        imp.setRoi(116,168,124,62);
        IJ.run(imp, "Add Selection...", "");
        imp.show();
    }

    private static void explorationTest2() {
        ImagePlus imp = IJ.createImage("HyperStack", "8-bit grayscale-mode label", 400, 300, 3, 4, 5);
        Roi roi = new Roi(116, 168, 124, 62);
        roi.setStrokeColor(Color.RED);
        roi.setPosition(1,1,1);
        imp.getOverlay().add(roi);
        roi.setStrokeColor(Color.RED);
        roi.setPosition(1,1,1);
        imp.getOverlay().add(roi);
        IJ.run(imp, "Add Slice", "add=channel");

//        imp.show();
        IJ.save(imp, "/home/micha/TemporaryFiles/20220119_imagej_overlays/overlay_test_image.tif");
//        imp.
    }

    private static void explorationTest3() {
//        ImagePlus imp = IJ.createImage("HyperStack", "8-bit grayscale-mode label", 400, 300, 3, 4, 5);
        ImagePlus imp = IJ.createHyperStack("HyperStack", 400, 300, 2, 1, 5, 8);
        int nChannels = imp.getNChannels();
        System.out.println("nChannels: " + nChannels);
        IJ.run(imp, "Add Slice", "add=channel");
        nChannels = imp.getNChannels();
        imp.updateImage();
        System.out.println("nChannels: " + nChannels);
        Overlay overlay = new Overlay();
        Roi roi = new Roi(116, 168, 124, 62);
        roi.setPosition(1,1,1);
        roi.setStrokeColor(Color.RED);
        overlay.add(roi);
        Roi roi2 = new Roi(50, 50, 60, 30);
        roi2.setPosition(2,1,1);
        roi2.setStrokeColor(Color.BLUE);
        overlay.add(roi2);
        imp.setOverlay(overlay);
        imp.show();
//        IJ.save(imp, "/home/micha/TemporaryFiles/20220119_imagej_overlays/overlay_test_image.tif");

//        imagePlus.getRoi();
//        imagePlus.setRoi(roi);
//        imagePlus.getOverlay();
//        imagePlus.setOverlay(overlay);
    }
}
