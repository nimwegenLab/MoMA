package com.jug.datahandling;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

public interface IImageProvider {
    Img<FloatType> getImgProbs();

    Img<FloatType> getImgProbsAt(int timeStep);

    Img<FloatType> getImgRaw();

    List<Img<FloatType>> getRawChannelImgs();

    Img<FloatType> getColorChannelAtTime(int channel, int timestep);
}
