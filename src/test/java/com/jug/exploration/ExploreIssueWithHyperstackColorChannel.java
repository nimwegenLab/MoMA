package com.jug.exploration;

import ij.ImagePlus;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.IntType;

public class ExploreIssueWithHyperstackColorChannel {
    public static void main(String... args){
        long xDim = 64;
        long yDim = 256;
        long nrofChannels = 2;
        long nrOfSlices = 1;
        long nrOfFrames = 10;
        ArrayImgFactory<IntType> imgFactory = new ArrayImgFactory<>(new IntType());
        Img<IntType> img = imgFactory.create(xDim, yDim, nrofChannels, nrOfSlices, nrOfFrames); /* channel order in an ImagePlus (which we use for storing), is: XYCZT; so those are the axes we generate here */
        long[] img_dims = new long[5];
        img.dimensions(img_dims);
        System.out.println("img_dims: " + img_dims[0] + " " + img_dims[1] + " " + img_dims[2] + " " + img_dims[3] + " " + img_dims[4]);
        ImagePlus wrapped_img = ImageJFunctions.wrap(img, "imgResults");
        System.out.println("wrapped_img_dims: " + wrapped_img.getWidth() + " " + wrapped_img.getHeight() + " " + wrapped_img.getNChannels() + " " + wrapped_img.getNSlices() + " " + wrapped_img.getNFrames());
    }
}
