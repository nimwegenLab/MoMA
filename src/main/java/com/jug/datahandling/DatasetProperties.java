package com.jug.datahandling;

import com.jug.util.FloatTypeImgLoader;
import ij.IJ;
import ij.ImagePlus;

import java.io.File;

public class DatasetProperties implements IDatasetProperties {
    private int minTime = -1;
    private int maxTime = -1;
    private int minChannelIdx = 1;
    private int numChannels = 1;

    public void readDatasetProperties(File inputFolder) {
        if (inputFolder.isDirectory() && inputFolder.listFiles(FloatTypeImgLoader.tifFilter).length > 1) {
            System.out.println("reading a folder of images");
            int min_t = Integer.MAX_VALUE;
            int max_t = Integer.MIN_VALUE;
            int min_c = Integer.MAX_VALUE;
            int max_c = Integer.MIN_VALUE;
            for (final File image : inputFolder.listFiles(FloatTypeImgLoader.tifFilter)) {

                final int c = FloatTypeImgLoader.getChannelFromFilename(image.getName());
                final int t = FloatTypeImgLoader.getTimeFromFilename(image.getName());

                if (c < min_c) {
                    min_c = c;
                }
                if (c > max_c) {
                    max_c = c;
                }

                if (t < min_t) {
                    min_t = t;
                }
                if (t > max_t) {
                    max_t = t;
                }
            }
            minTime = min_t;
            maxTime = max_t + 1;
            minChannelIdx = min_c;
            numChannels = max_c - min_c + 1;
        } else {
            System.out.println("Loading images ...");
            ImagePlus imp;
            if (inputFolder.isDirectory() && inputFolder.listFiles(FloatTypeImgLoader.tifFilter).length == 1) {
                imp = IJ.openImage(inputFolder.listFiles(FloatTypeImgLoader.tifFilter)[0].getAbsolutePath());
            } else {
                imp = IJ.openImage(inputFolder.getAbsolutePath());
            }

            minTime = 1;
            maxTime = imp.getNFrames();
            minChannelIdx = 1;
            numChannels = imp.getNChannels();
        }

        System.out.println("Determined minTime: " + minTime);
        System.out.println("Determined maxTime: " + maxTime);

        System.out.println("Determined minChannelIdx: " + minChannelIdx);
        System.out.println("Determined numChannels: " + numChannels);
    }

    @Override
    public int getMinTime() {
        return minTime;
    }

    @Override
    public int getMaxTime() {
        return maxTime;
    }

    @Override
    public int getNumChannels() {
        return numChannels;
    }

    @Override
    public int getMinChannelIdx() {
        return minChannelIdx;
    }

    public boolean timestepInsideRange(int timestep) {
        return (timestep >= this.getMinTime()) &
                timestep <= this.getMaxTime();
    }
}
