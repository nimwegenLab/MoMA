package com.jug.util.componenttree;

import com.google.gson.Gson;
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

    public ComponentForestDeserializer(
            ComponentProperties componentProperties,
            String jsonString) {
        this.componentProperties = componentProperties;
        this.jsonString = jsonString;
    }

    private Map<Integer, Map<String, AdvancedComponentPojo>> frameToPojosMap; /* this maps the frame-index to a Map of string-id to component */

    @Override
    public synchronized AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>
    buildComponentForest(Img<FloatType> raiFkt,
                         int frameIndex,
                         float componentSplittingThreshold) {
        if (isNull(frameToPojosMap)) {
            frameToPojosMap = buildMappingFrameToPojos(jsonString);
        }
        Map<String, AdvancedComponentPojo> pojosInFrame = frameToPojosMap.get(frameIndex);
        return buildForest(pojosInFrame, raiFkt);
    }

    public AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> buildForest(Map<String, AdvancedComponentPojo> pojosInFrame, Img<FloatType> raiFkt) {
        List<AdvancedComponent<FloatType>> roots = new ArrayList<>();
        for (AdvancedComponentPojo pojo : pojosInFrame.values()) {
            if (pojo.getParentStringId().equals("NA")) {
                AdvancedComponent<FloatType> rootComponent = AdvancedComponent.createFromPojo(pojo, componentProperties, raiFkt);
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
            AdvancedComponent<FloatType> child = AdvancedComponent.createFromPojo(pojosInFrame.get(id), componentProperties, raiFkt);
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
