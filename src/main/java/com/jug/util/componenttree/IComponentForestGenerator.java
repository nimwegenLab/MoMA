package com.jug.util.componenttree;

import com.jug.datahandling.IImageProvider;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

public interface IComponentForestGenerator {
    AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> buildComponentForest(IImageProvider imageProvider, int frameIndex, float componentSplittingThreshold);
}
