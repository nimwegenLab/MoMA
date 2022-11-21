package com.jug.datahandling;

import com.jug.config.IConfiguration;
import com.jug.util.componenttree.*;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

import java.io.*;
import java.nio.file.Files;

import static java.util.Objects.isNull;

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
    private ComponentForestGenerator componentForestGenerator;
    private IImageProvider imageProvider;
    private ComponentProperties componentProperties;
    private IConfiguration configuration;
    private ComponentForestDeserializer componentForestDeserializer;

    public ComponentForestProvider(ComponentProperties componentProperties, ComponentForestGenerator componentForestGenerator, IGlExportFilePathGetter paths, IConfiguration configuration, IImageProvider imageProvider) {
        this.componentProperties = componentProperties;
        this.paths = paths;
        this.configuration = configuration;
        this.componentForestGenerator = componentForestGenerator;
        this.imageProvider = imageProvider;
    }

    @Override
    public AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> buildComponentForest(IImageProvider imageProvider, int frameIndex, float componentSplittingThreshold) {
        if (configuration.getIsReloading() && paths.getComponentTreeJsonFile().exists()) {
            componentForestDeserializer = getComponentForestDeserializer(paths.getComponentTreeJsonFile());
            return componentForestDeserializer.buildComponentForest(imageProvider, frameIndex, componentSplittingThreshold);
        }
        return componentForestGenerator.buildComponentForest(imageProvider, frameIndex, componentSplittingThreshold);
    }

    private synchronized ComponentForestDeserializer getComponentForestDeserializer(File jsonFile) {
        if (!jsonFileIsLoaded(paths.getComponentTreeJsonFile())) { /* in case the JSON file change, we need parse it again */
            if (!jsonFile.exists()) {
                throw new RuntimeException("File does not exist: " + jsonFile);
            }
            currentJsonFile = jsonFile;
            String jsonString = readFileAsString(jsonFile);
            componentForestDeserializer = new ComponentForestDeserializer(componentProperties, jsonString, this.imageProvider);
        }
        return componentForestDeserializer;
    }

    private File currentJsonFile;

    private boolean jsonFileIsLoaded(File jsonFileToLoad) {
        return !isNull(currentJsonFile) && currentJsonFile.equals(jsonFileToLoad);
    }

    private String readFileAsString(File jsonFile)
    {
        try{
            String jsonString = new String(Files.readAllBytes(jsonFile.toPath()));
            return jsonString;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
