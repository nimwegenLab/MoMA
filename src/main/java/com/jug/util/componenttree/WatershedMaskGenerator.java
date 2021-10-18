package com.jug.util.componenttree;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.binary.Thresholder;
import net.imglib2.algorithm.labeling.ConnectedComponentAnalysis;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;

public class WatershedMaskGenerator {
    public Img<IntType> labelingImage;
    public Img<BitType> mask;
    public Img<BitType> mergedMask;

    public Img<BitType> generateMask(Img<FloatType> image, float threshold) {
        mask = Thresholder.threshold(image, new FloatType(threshold), true, 1);
        labelingImage = createLabelingImage(mask);
        ConnectedComponentAnalysis.connectedComponents(mask, labelingImage);
        mergedMask = mergeDifferingConnectedComponents(mask, labelingImage);
        return mergedMask;
    }

    private Img<BitType> mergeDifferingConnectedComponents(Img<BitType> inputMask, Img<IntType> labelingImage) {
        int lookAhead = 3;
        Img<BitType> mergedMask = inputMask.copy();
        Cursor<IntType> labelingCursor = labelingImage.localizingCursor();
        RandomAccess<IntType> labelRandomAccess = labelingImage.randomAccess();
        RandomAccess<BitType> maskRandomAccess = mergedMask.randomAccess();

        long[] labelPosition = new long[labelingImage.numDimensions()];
        long[] labelPositionLookAhead = new long[labelingImage.numDimensions()];
        while (labelingCursor.hasNext()) {
            labelingCursor.next();
            labelingCursor.localize(labelPosition);
//            labelingCursor.localize(labelPositionLookAhead);
            int currentLabelValue = labelingCursor.get().getInteger();

            if (currentLabelValue == 0) {
                continue; /* pixel value is background, so nothing to do */
            }

            boolean performMergeForThisPixel = false; /* boolean which tells if the current pixel has a component below, which should be merged */
            for (int i = 1; i < lookAhead; i++) {
//                labelPositionLookAhead[1] += i;
                long[] nextPosition = new long[]{labelPosition[0], labelPosition[1] + i};
                System.out.println("labelPosition: " + labelPosition[0] + "," + labelPosition[1]);
                System.out.println("nextPosition: " + nextPosition[0] + "," + nextPosition[1]);
                labelRandomAccess.setPosition(nextPosition);
                int nextPixelValue = labelRandomAccess.get().get();
                if (nextPixelValue != 0 && nextPixelValue != currentLabelValue) {
                    performMergeForThisPixel = true;
                }
            }

            if (!performMergeForThisPixel) {
                continue;
            }

//            labelPositionLookAhead = labelPosition.clone();
            for (int i = 1; i < lookAhead; i++) {
//                labelPositionLookAhead[1] += i;
                long[] nextPosition = new long[]{labelPosition[0], labelPosition[1] + i};
                maskRandomAccess.setPosition(nextPosition);
                boolean previousValue = maskRandomAccess.get().get();
                System.out.println("position: " + nextPosition[0] + "," + nextPosition[1]);
                System.out.println("previousValue 1: " + previousValue);
                maskRandomAccess.get().setOne();
                boolean newValue = maskRandomAccess.get().get();
                System.out.println("previousValue 2: " + previousValue);
                System.out.println("newValue: " + newValue);
                System.out.println();
            }
        }
        return mergedMask;

//        LoopBuilder.setImages(labelingImage, inputMask).forEachPixel((dest, msk) -> {
//            if (!msk.get()) {
////                dest.set(maskedValue);
//            }
//        });

//        return inputMask;
//        throw new NotImplementedException();
//        return outputMask
    }

    /**
     * Convenience method for creating a labeling image with same dimensions as the source image.
     *
     * @param sourceImage
     * @return
     */
    private Img<IntType> createLabelingImage(RandomAccessibleInterval sourceImage) {
        long[] dims = new long[sourceImage.numDimensions()];
        sourceImage.dimensions(dims);
        Img<IntType> img = ArrayImgs.ints(dims);
        return img;
//        return new ImgLabeling<>(img);
    }
}
