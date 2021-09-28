package com.jug.lp;

import com.jug.datahandling.IImageProvider;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import org.apache.commons.lang.NotImplementedException;

import java.util.List;

public class ImageProviderMock implements IImageProvider {
    private final Img<FloatType> probabilityImage;

    public ImageProviderMock(Img<FloatType> probabilityImage) {
        this.probabilityImage = probabilityImage;
    }

    @Override
    public Img<FloatType> getImgProbs() {
        return this.probabilityImage;
    }

    @Override
    public Img<FloatType> getImgRaw() {
        throw new NotImplementedException();

    }

    @Override
    public List<Img<FloatType>> getRawChannelImgs() {
        throw new NotImplementedException();
    }

    @Override
    public Img<FloatType> getColorChannelAtTime(int channel, int timestep) {
        throw new NotImplementedException();
    }
}
