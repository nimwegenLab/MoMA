package com.jug.config;

public interface IComponentTreeGeneratorConfiguration {
    /**
     * Returns the minimal size of leaf components in [px].
     * Components candidates with size lower than this are not considered.
     *
     * @return minimal size of leaf components in [px]
     */
    int getSizeMinimumOfLeafComponent();
}
