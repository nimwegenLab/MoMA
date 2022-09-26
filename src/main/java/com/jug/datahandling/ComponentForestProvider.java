package com.jug.datahandling;

import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.IComponentForestGenerator;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

public class ComponentForestProvider implements IComponentForestGenerator {
    private IGlExportFilePathGetter paths;

    public ComponentForestProvider(IGlExportFilePathGetter paths) {
        this.paths = paths;
    }

    @Override
    public ComponentForest<AdvancedComponent<FloatType>> buildComponentForest(Img<FloatType> raiFkt, int frameIndex, float componentSplittingThreshold) {
//        paths.getComponentTreeJsonFile()
        return null;
    }
}
