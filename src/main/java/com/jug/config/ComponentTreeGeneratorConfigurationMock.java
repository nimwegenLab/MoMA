package com.jug.config;

public class ComponentTreeGeneratorConfigurationMock implements IComponentTreeGeneratorConfiguration {
    private int sizeMinimumOfLeafComponent;
    private int sizeMinimumOfParentComponent;

    public ComponentTreeGeneratorConfigurationMock(int sizeMinimumOfLeafComponent, int sizeMinimumOfParentComponent) {

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
