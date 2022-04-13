package com.jug.datahandling;

public interface DatasetPropertiesInterface {
    /**
     * @return the first time-point loaded
     */
    int getMinTime();

    /**
     * @return the last loaded time-point
     */
    int getMaxTime();

    int getNumChannels();

    int getMinChannelIdx();
}
