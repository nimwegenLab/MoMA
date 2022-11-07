package com.jug.lp;

import com.jug.datahandling.IImageProvider;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.apache.commons.lang.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

public class ImageProviderMock implements IImageProvider {
    private Img<FloatType> probabilityImage;
    private Img<FloatType> imageStack;

    public ImageProviderMock(Img<FloatType> probabilityImage) {
        this.probabilityImage = probabilityImage;
        this.imageStack = null;
    }

    public ImageProviderMock(Img<FloatType> probabilityImage, Img<FloatType> imageStack) {
        this.probabilityImage = probabilityImage;
        this.imageStack = imageStack;
    }

    @Override
    public void setImgProbs(Img<FloatType> imgProbs) {

    }

    @Override
    public Img<FloatType> getImgProbs() {
        return this.probabilityImage;
    }

    @Override
    public Img<FloatType> getImgProbsAt(int timeStep) {
        Img<FloatType> img = this.getImgProbs();
        return ImgView.wrap(Views.hyperSlice(img, 2, timeStep));
    }

    @Override
    public Img<FloatType> getImgRaw() {
        if(isNull(this.imageStack)){
            throw new RuntimeException("Image data is not available.");
        }
        return imageStack;
    }

    @Override
    public List<Img<FloatType>> getRawChannelImgs() {
        if(isNull(this.imageStack)){
            throw new RuntimeException("Image data is not available.");
        }
        List<Img<FloatType>> imgChannelStack = new ArrayList<>();
        long numberOfChannels = imageStack.dimension(2);
        for(long c=0; c<numberOfChannels; c++){
            imgChannelStack.add(ImgView.wrap(Views.hyperSlice(imageStack, 2, c)));
        }
        return imgChannelStack;
    }

    @Override
    public Img<FloatType> getColorChannelAtTime(int channel, int timestep) {
        if(isNull(this.imageStack)){
            throw new RuntimeException("Image data is not available.");
        }
        return ImgView.wrap(Views.hyperSlice(this.getRawChannelImgs().get(channel), 2, timestep));
    }
}
