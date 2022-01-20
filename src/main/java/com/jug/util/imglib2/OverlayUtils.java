package com.jug.util.imglib2;

import ij.gui.Roi;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.roi.geom.real.Polyline;
import org.scijava.convert.ConvertService;

public class OverlayUtils {
    private ConvertService convertService;

    public OverlayUtils(ConvertService convertService) {

        this.convertService = convertService;
    }

    public Roi convertToRoi(Polygon2D polygon) {
//        Roi roi = ij.convert().convert(polygon, Roi.class);
        Roi roi = convertService.convert(polygon, Roi.class);
        return roi;
    }

    public Roi convertToRoi(Polyline polyline) {
//        Roi roi = ij.convert().convert(polygon, Roi.class);
        Roi roi = convertService.convert(polyline, Roi.class);
        return roi;
    }
}
