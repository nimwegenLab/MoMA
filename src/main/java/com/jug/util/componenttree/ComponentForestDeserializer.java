package com.jug.util.componenttree;

import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import org.apache.commons.lang.NotImplementedException;

public class ComponentForestDeserializer {
    private String jsonString;

    public ComponentForestDeserializer(String jsonString) {
        this.jsonString = jsonString;
    }

    public ComponentForest<AdvancedComponent<FloatType>> buildComponentForest(Img<FloatType> raiFkt, int frameIndex, float componentSplittingThreshold) {
        throw new NotImplementedException();
    }
}
