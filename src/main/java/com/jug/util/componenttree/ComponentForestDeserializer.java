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
    private String jsonString;

    public ComponentForestDeserializer(String jsonString) {
        this.jsonString = jsonString;
    }


    private Map<Integer, List<AdvancedComponentPojo>> pojoMap;

    @Override
    public ComponentForest<AdvancedComponent<FloatType>> buildComponentForest(Img<FloatType> raiFkt, int frameIndex, float componentSplittingThreshold) {
        if(isNull(pojoMap)){
            pojoMap = buildPojoMap();
        }
        throw new NotImplementedException();
    }

    private Map<Integer, List<AdvancedComponentPojo>> buildPojoMap() {
        List<AdvancedComponentPojo> pojoList = deserializeFromJson(jsonString);
        pojoMap = new HashMap<>();
        for (AdvancedComponentPojo pojo : pojoList) {
            int frame = pojo.getFrameNumber();
            List<AdvancedComponentPojo> currentList = pojoMap.get(frame);
            if (isNull(currentList)) {
                currentList = new ArrayList<>();
                pojoMap.put(frame, currentList);
            }
            currentList.add(pojo);
        }
        throw new NotImplementedException();
    }

    public List<AdvancedComponentPojo> deserializeFromJson(String jsonString) {
        ComponentSerializationContainer componentContainer = new Gson().fromJson(jsonString, ComponentSerializationContainer.class);
        return componentContainer.getAdvancedComponentPojos();
    }

    private List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> rebuildComponentTrees() {
        ArrayList<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> componentForests = new ArrayList<>();
        throw new NotImplementedException();
    }
}
