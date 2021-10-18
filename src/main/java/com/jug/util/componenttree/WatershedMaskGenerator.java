package com.jug.util.componenttree;

import net.imagej.ops.OpService;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.algorithm.binary.Thresholder;

public class WatershedMaskGenerator {
    private OpService ops;

    public WatershedMaskGenerator(OpService ops) {
        this.ops = ops;
    }

    public Img<BitType> generateMask(Img<FloatType> image, float threshold) {
//        IterableInterval<BitType> thresholdImage = ops.threshold().apply(image, new FloatType(threshold));
//        return thresholdImage;
        Img<BitType> thresholdImage = Thresholder.threshold(image, new FloatType(threshold), true, 1);
        return thresholdImage;
    }
}
