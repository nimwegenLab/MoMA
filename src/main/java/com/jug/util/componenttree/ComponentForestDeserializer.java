package com.jug.util.componenttree;

import com.google.gson.Gson;
import com.jug.datahandling.IImageProvider;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

public class ComponentForestDeserializer
        implements IComponentForestGenerator {
    private ComponentProperties componentProperties;
    private String jsonString;
    private IImageProvider imageProvider;

    public ComponentForestDeserializer(
            ComponentProperties componentProperties,
            String jsonString,
            IImageProvider imageProvider) {
        this.componentProperties = componentProperties;
        this.jsonString = jsonString;
        this.imageProvider = imageProvider;
    }

    private Map<Integer, Map<String, AdvancedComponentPojo>> frameToPojosMap; /* this maps the frame-index to a Map of string-id to component */

    @Override
    public synchronized AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>
    buildComponentForest(IImageProvider imageProvider,
                         int frameIndex,
                         float componentSplittingThreshold) {
        if (isNull(frameToPojosMap)) {
            frameToPojosMap = buildMappingFrameToPojos(jsonString);
        }
        Map<String, AdvancedComponentPojo> pojosInFrame = frameToPojosMap.get(frameIndex);
        if (isNull(pojosInFrame)) {
            return new AdvancedComponentForest<>(new ArrayList<>());
        }
        return buildForest(pojosInFrame, imageProvider.getImgProbsAt(frameIndex));
    }

    public AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> buildForest(Map<String, AdvancedComponentPojo> pojosInFrame, Img<FloatType> raiFkt) {
        List<AdvancedComponent<FloatType>> roots = new ArrayList<>();
        for (AdvancedComponentPojo pojo : pojosInFrame.values()) {
            if (pojo.getParentStringId().equals("NA")) {
                AdvancedComponent<FloatType> rootComponent = AdvancedComponent.createFromPojo(pojo, componentProperties, raiFkt, imageProvider);
                roots.add(rootComponent);
            }
        }
        for (AdvancedComponent<FloatType> root : roots) {
            recursivelyBuildTree(root, pojosInFrame, raiFkt);
        }
        return new AdvancedComponentForest<>(roots);
    }

    private void recursivelyBuildTree(AdvancedComponent<FloatType> component, Map<String, AdvancedComponentPojo> pojosInFrame, Img<FloatType> raiFkt) {
        if (component.getChildrenStringIds().isEmpty()) {
            return;
        }
        for (String id : component.getChildrenStringIds()) {
            AdvancedComponent<FloatType> child = AdvancedComponent.createFromPojo(pojosInFrame.get(id), componentProperties, raiFkt, imageProvider);
            child.setParent(component);
            component.addChild(child);
            recursivelyBuildTree(child, pojosInFrame, raiFkt);
        }
    }

    public static Map<Integer, Map<String, AdvancedComponentPojo>> buildMappingFrameToPojos(String jsonString) {
        List<AdvancedComponentPojo> pojoList = deserializeFromJson(jsonString);
        Map<Integer, Map<String, AdvancedComponentPojo>> pojoMap = new HashMap<>();
        for (AdvancedComponentPojo pojo : pojoList) {
            int frame = pojo.getFrameNumber();
            Map<String, AdvancedComponentPojo> currentMap = pojoMap.get(frame);
            if (isNull(currentMap)) {
                currentMap = new HashMap<>();
                pojoMap.put(frame, currentMap);
            }
            currentMap.put(pojo.getStringId(), pojo);
        }
        return pojoMap;
    }

    public static List<AdvancedComponentPojo> deserializeFromJson(String jsonString) {
        ComponentSerializationContainer componentContainer = new Gson().fromJson(jsonString, ComponentSerializationContainer.class);
        return componentContainer.getAdvancedComponentPojos();
    }
}
