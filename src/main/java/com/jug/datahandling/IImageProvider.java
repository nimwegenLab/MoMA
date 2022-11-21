package com.jug.datahandling;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

public interface IImageProvider {
    void setImgProbs(Img<FloatType> imgProbs);

    Img<FloatType> getImgProbs();

    Img<FloatType> getImgProbsAt(int timeStep);

    Img<FloatType> getImgRaw();

    /**
     * @deprecated
     * - use {@link #getChannelImg(int)} to get the image for a specific channel.
     * - use {@link #getNumberOfChannels()} to get the number of channels.
     *
     * @return the rawChannelImgs
     */
    @Deprecated
    List<Img<FloatType>> getRawChannelImgs();

    Img<FloatType> getChannelImg(int channelNumber);

    Img<FloatType> getColorChannelAtTime(int channel, int timestep);

    int getNumberOfChannels();
}
