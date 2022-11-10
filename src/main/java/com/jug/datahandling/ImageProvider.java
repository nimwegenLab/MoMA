package com.jug.datahandling;

import com.jug.util.FloatTypeImgLoader;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class ImageProvider implements IImageProvider {
    private ArrayList<Img<FloatType>> rawChannelImgs;
    private Img<FloatType> imgRaw;
    private Img<FloatType> imgProbs;

    public void loadTiffsFromFileOrFolder(String path, int minTime, int maxTime, int minChannelIdx, int numChannels) throws FileNotFoundException {
        rawChannelImgs = FloatTypeImgLoader.loadTiffsFromFileOrFolder(path, minTime, maxTime, minChannelIdx, numChannels + minChannelIdx - 1);
        imgRaw = rawChannelImgs.get( 0 );
    }

    /**
     * @return the imgRaw
     */
    @Override
    public Img<FloatType> getImgRaw() {
        return imgRaw;
    }

    /**
     * @return the rawChannelImgs
     */
    @Override
    public List<Img<FloatType>> getRawChannelImgs() {
        return rawChannelImgs;
    }

    /**
     * @return the imgProbs
     */
    @Override
    public Img<FloatType> getImgProbs() {
        return imgProbs;
    }

    /**
     * @return the imgProbs
     */
    @Override
    public synchronized void setImgProbs(Img<FloatType> imgProbs) {
        this.imgProbs = imgProbs;
    }

    @Override
    public synchronized Img<FloatType> getImgProbsAt(int timeStep) {
        Img<FloatType> img = this.getImgProbs();
        return ImgView.wrap(Views.hyperSlice(img, 2, timeStep)).copy(); /* return copy so that we do not accidentally modify the source image */
    }

    @Override
    public synchronized Img<FloatType> getColorChannelAtTime(int channelNumber, int timestep) {
        ArgumentValidation.channelNumberIsValid(this, channelNumber);
        return ImgView.wrap(Views.hyperSlice(this.getRawChannelImgs().get(channelNumber), 2, timestep));
    }
}
