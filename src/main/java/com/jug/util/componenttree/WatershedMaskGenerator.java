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
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;

/**
 * This class generates the mask that is used to mask the image before running the component generation.
 */
public class WatershedMaskGenerator {
    /**
     * The maximum allowed number of pixels between two distinct components in order for the two components to still be
     * merged connected.
     */
    int maximumDistanceBetweenComponents = 0;

    public WatershedMaskGenerator(int maximumDistanceBetweenComponents) {
        this.maximumDistanceBetweenComponents = maximumDistanceBetweenComponents;
    }

    public Img<BitType> generateMask(Img<FloatType> image, float threshold) {
        Img<BitType> mask = Thresholder.threshold(image, new FloatType(threshold), true, 1);
        Img<IntType> labelingImage = createLabelingImage(mask);
        ConnectedComponentAnalysis.connectedComponents(mask, labelingImage);
        Img<BitType> mergedMask = mask.copy();
        mergeDifferingConnectedComponentsInMask(mergedMask, labelingImage);
        return mergedMask;
    }

    private void mergeDifferingConnectedComponentsInMask(Img<BitType> mergedMask, Img<IntType> labelingImage) {
        int lookAheadDistance = maximumDistanceBetweenComponents + 2; /* +2 because the lookAheadDistance has to include one pixel for each component; whereas maximumDistanceBetweenComponents is the number of pixels between the two components */
        ExtendedRandomAccessibleInterval<IntType, Img<IntType>> labelingImageExtended = Views.extendZero(labelingImage); /* extend to avoid running out of bounds */
        ExtendedRandomAccessibleInterval<BitType, Img<BitType>> mergedMaskExtended = Views.extendZero(mergedMask); /* extend to avoid running out of bounds */
        Cursor<IntType> labelingCursor = labelingImage.localizingCursor();
        RandomAccess<IntType> labelRandomAccess = labelingImageExtended.randomAccess();
        RandomAccess<BitType> maskRandomAccess = mergedMaskExtended.randomAccess();

        long[] labelPosition = new long[labelingImage.numDimensions()];
        while (labelingCursor.hasNext()) {
            labelingCursor.next();
            labelingCursor.localize(labelPosition);
            int currentLabelValue = labelingCursor.get().getInteger();

            if (currentLabelValue == 0) {
                continue; /* pixel value is background, so nothing to do */
            }

            boolean performMergeForThisPixel = false; /* boolean which tells if the current pixel has a component below, which should be merged */
            for (int i = 1; i < lookAheadDistance; i++) {
                long[] nextPosition = new long[]{labelPosition[0], labelPosition[1] + i};
                labelRandomAccess.setPosition(nextPosition);
                int nextPixelValue = labelRandomAccess.get().get();
                if (nextPixelValue != 0 && nextPixelValue != currentLabelValue) {
                    performMergeForThisPixel = true;
                }
            }

            if (!performMergeForThisPixel) {
                continue;
            }

            for (int i = 1; i < lookAheadDistance; i++) {
                long[] nextPosition = new long[]{labelPosition[0], labelPosition[1] + i};
                maskRandomAccess.setPosition(nextPosition);
                maskRandomAccess.get().setOne();
            }
        }
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
