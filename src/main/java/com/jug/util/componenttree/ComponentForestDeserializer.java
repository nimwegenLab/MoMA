package com.jug.util.componenttree;

import com.google.gson.Gson;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import org.apache.commons.lang.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

public class ComponentForestDeserializer implements IComponentForestGenerator {
    private ComponentProperties componentProperties;
    private String jsonString;

    public ComponentForestDeserializer(ComponentProperties componentProperties, String jsonString) {
        this.componentProperties = componentProperties;
        this.jsonString = jsonString;
    }

    private Map<Integer, Map<String, AdvancedComponentPojo>> pojoMap; /* this maps the frame-index to a Map of string-id to component */

    @Override
    public ComponentForest<AdvancedComponent<FloatType>> buildComponentForest(Img<FloatType> raiFkt, int frameIndex, float componentSplittingThreshold) {
        if (isNull(pojoMap)) {
            pojoMap = buildPojoMap(jsonString);
        }
        Map<String, AdvancedComponentPojo> pojosInFrame = pojoMap.get(frameIndex);
        return buildForest(pojosInFrame, raiFkt);
    }

    public ComponentForest<AdvancedComponent<FloatType>> buildForest(Map<String, AdvancedComponentPojo> pojosInFrame, Img<FloatType> raiFkt) {
//        List<AdvancedComponent<?>> allComponents = new ArrayList<>();
//        for (AdvancedComponentPojo pojo : pojosInFrame) {
//            allComponents.add(AdvancedComponent.createFromPojo(pojo, componentProperties));
//        }
        List<AdvancedComponent<FloatType>> roots = new ArrayList<>();
        for (AdvancedComponentPojo pojo : pojosInFrame.values()) {
            if (pojo.getParentStringId().equals("NA")) {
                AdvancedComponent<FloatType> rootComponent = AdvancedComponent.createFromPojo(pojo, componentProperties);
                roots.add(rootComponent);
            }
        }
        for (AdvancedComponent<FloatType> root : roots) {
            recursivelyBuildTree(root, pojosInFrame);
        }
        return new AdvancedComponentForest<>(roots);
//        throw new NotImplementedException();
//        return new AdvancedComponentForest(roots);
    }

    private void recursivelyBuildTree(AdvancedComponent<FloatType> component, Map<String, AdvancedComponentPojo> pojosInFrame) {
        if (component.getChildrenStringIds().isEmpty()) {
            return;
        }
        for (String id : component.getChildrenStringIds()) {
            AdvancedComponent<FloatType> child = AdvancedComponent.createFromPojo(pojosInFrame.get(id), componentProperties);
            child.setParent(component);
            component.addChild(child);
//            System.out.println("component.getNodeLevel(): " + component.getNodeLevel());
            recursivelyBuildTree(child, pojosInFrame);
        }
    }

    public static Map<Integer, Map<String, AdvancedComponentPojo>> buildPojoMap(String jsonString) {
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
