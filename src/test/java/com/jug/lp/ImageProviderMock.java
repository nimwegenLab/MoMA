package com.jug.lp;

import com.jug.datahandling.IImageProvider;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

public class ImageProviderMock implements IImageProvider {
    @Override
    public Img<FloatType> getImgProbs() {
        return null;
    }

    @Override
    public Img<FloatType> getImgRaw() {
        return null;
    }

    @Override
    public List<Img<FloatType>> getRawChannelImgs() {
        return null;
    }
}
