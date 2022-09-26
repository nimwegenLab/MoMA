package com.jug.util.componenttree;

import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

public interface IComponentForestGenerator {
    AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> buildComponentForest(Img<FloatType> raiFkt, int frameIndex, float componentSplittingThreshold);
}
