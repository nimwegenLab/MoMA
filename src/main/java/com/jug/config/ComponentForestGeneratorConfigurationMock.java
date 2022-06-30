package com.jug.config;

public class ComponentForestGeneratorConfigurationMock implements IComponentForestGeneratorConfiguration {
    private int sizeMinimumOfLeafComponent;
    private int sizeMinimumOfParentComponent;

    public ComponentForestGeneratorConfigurationMock(int sizeMinimumOfLeafComponent, int sizeMinimumOfParentComponent) {

        this.sizeMinimumOfLeafComponent = sizeMinimumOfLeafComponent;
        this.sizeMinimumOfParentComponent = sizeMinimumOfParentComponent;
    }


    public int getSizeMinimumOfLeafComponent() {
        return sizeMinimumOfLeafComponent;
    }

    public int getSizeMinimumOfParentComponent() {
        return sizeMinimumOfParentComponent;
    }
}
