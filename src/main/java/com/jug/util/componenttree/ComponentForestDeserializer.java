package com.jug.util.componenttree;

import com.google.gson.Gson;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import org.apache.commons.lang.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public class ComponentForestDeserializer implements IComponentForestGenerator {
    private String jsonString;

    public ComponentForestDeserializer(String jsonString) {
        this.jsonString = jsonString;
    }

    @Override
    public ComponentForest<AdvancedComponent<FloatType>> buildComponentForest(Img<FloatType> raiFkt, int frameIndex, float componentSplittingThreshold) {
        throw new NotImplementedException();
    }
    public void deserializeFromJson(String jsonString) {
        ComponentSerializationContainer componentContainer = new Gson().fromJson(jsonString, ComponentSerializationContainer.class);
        List<AdvancedComponentPojo> pojos = componentContainer.getAdvancedComponentPojos();
        throw new NotImplementedException();
    }

    public List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> rebuildComponentTrees() {
        ArrayList<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> componentForests = new ArrayList<>();
        throw new NotImplementedException();
    }
}
