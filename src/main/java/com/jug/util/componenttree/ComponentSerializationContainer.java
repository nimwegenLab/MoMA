package com.jug.util.componenttree;

import java.util.List;

public class ComponentSerializationContainer {
    private final String version;
    private List<AdvancedComponentPojo> components;

    public ComponentSerializationContainer(List<AdvancedComponentPojo> components) {
        version = "0.1.0";
        this.components = components;
    }

    public List<AdvancedComponentPojo> getAdvancedComponentPojos() {
        return components;
    }
}
