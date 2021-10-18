package com.jug.util.componenttree;

import net.imagej.ops.OpService;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.algorithm.binary.Thresholder;

public class WatershedMaskGenerator {
    public Img<BitType> generateMask(Img<FloatType> image, float threshold) {
        Img<BitType> thresholdImage = Thresholder.threshold(image, new FloatType(threshold), true, 1);
        return thresholdImage;
    }
}
