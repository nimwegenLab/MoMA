package com.jug.util.componenttree;

import com.google.gson.Gson;
import com.jug.datahandling.Version;
import net.imglib2.type.numeric.real.FloatType;
import org.apache.commons.lang.NotImplementedException;

import java.io.File;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class ComponentForestSerializer {
    public String serializeToDisk(List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> componentForests) {
        System.out.println("FINISHED.");
        List<AdvancedComponent<FloatType>> components = getAllComponents(componentForests);
        List<AdvancedComponentPojo> advancedComponentPojos = getSerializableRepresentations(components);
        String jsonString = new Gson().toJson(advancedComponentPojos);
        throw new NotImplementedException();
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
