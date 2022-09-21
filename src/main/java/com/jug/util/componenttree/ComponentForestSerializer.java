package com.jug.util.componenttree;

import com.google.gson.Gson;
import net.imglib2.type.numeric.real.FloatType;
import org.apache.commons.lang.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public class ComponentForestSerializer {
    public String serializeToJson(List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> componentForests) {
        List<AdvancedComponent<FloatType>> components = getAllComponents(componentForests);
        List<AdvancedComponentPojo> advancedComponentPojos = getSerializableRepresentations(components);
        ComponentSerializationContainer serializationContainer = new ComponentSerializationContainer(advancedComponentPojos);
        String jsonString = new Gson().toJson(serializationContainer);
        return jsonString;
    }

    private List<AdvancedComponent<FloatType>> getAllComponents(List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> componentForests) {
        ArrayList<AdvancedComponent<FloatType>> allComponents = new ArrayList<>();
        for (AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> forest : componentForests) {
            allComponents.addAll(forest.getAllComponents());
        }
        return allComponents;
    }

    private List<AdvancedComponentPojo> getSerializableRepresentations(List<AdvancedComponent<FloatType>> components){
        List<AdvancedComponentPojo> advancedComponentPojos = new ArrayList<>();
        for(AdvancedComponent<FloatType> component : components){
            advancedComponentPojos.add(component.getSerializableRepresentation());
        }
        return advancedComponentPojos;
    }
}
