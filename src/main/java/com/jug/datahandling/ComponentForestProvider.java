package com.jug.datahandling;

import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.IComponentForestGenerator;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

/**
 * This class return the component-forests for the GL. It handles two cases:
 * 1. The GL is being analyzed for the first time: In this case it will build and use {@link com.jug.util.componenttree.ComponentForestGenerator}
 * to generate the component-forest anew. This happens, when a GL is being loaded and no JSON file containing the serialized
 * component-forests is found.
 * 2. The GL is being reloaded and the JSON file with the component forests exists: In this case it will read the JSON file
 * and use {@link com.jug.util.componenttree.ComponentForestDeserializer} to deserialize the component forests and return
 * them.
 */

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
