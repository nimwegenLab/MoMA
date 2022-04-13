package com.jug.datahandling;

public interface IDatasetProperties {
    /**
     * Returns the minimum time step of the range of images (this should always be zero!).
     * @return the first time-point of the dataset (this should always be zero!)
     */
    int getMinTime();

    /**
     * Returns the maximum number of time steps in the dataset.
     * @return the number of time steps in the dataset
     */
    int getMaxTime();

    /**
     * Returns the number of color channels in the dataset.
     * @return the number of color channels in the dataset
     */
    int getNumChannels();

    /**
     * Returns the index of the first color channel.
     * @return the index of the first color channel
     */
    int getMinChannelIdx();
}
