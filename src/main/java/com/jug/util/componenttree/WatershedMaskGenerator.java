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
    private float thresholdForComponentMerging;

    float threshold;

    public void setThresholdForComponentMerging(float thresholdForComponentMerging) {
        this.thresholdForComponentMerging = thresholdForComponentMerging;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    public WatershedMaskGenerator(float thresholdForComponentMerging, float threshold) {
        this.thresholdForComponentMerging = thresholdForComponentMerging;
        this.threshold = threshold;
    }

    public synchronized Img<BitType> generateMask(Img<FloatType> image) {
        Img<BitType> maskForComponentGeneration = Thresholder.threshold(image, new FloatType(threshold), true, 1);
        Img<BitType> maskForComponentMerging = Thresholder.threshold(image, new FloatType(thresholdForComponentMerging), true, 1);
        Img<IntType> labelingImage = createLabelingImage(maskForComponentGeneration);
        ConnectedComponentAnalysis.connectedComponents(maskForComponentGeneration, labelingImage);
        mergeDifferingConnectedComponentsInMask(maskForComponentGeneration, maskForComponentMerging, labelingImage);
        return maskForComponentGeneration;
    }

    /**
     * This method merges two connected components in componentMask _along pixel columns_ in the following way:
     * The algorithm consist of two steps:
     * 1.Check if a pixel has a pixel with different label value below it (along the pixel column) and get the range
     * along the pixel column (the range is the pixels below the current pixel position and pixelColumnEndPosition).
     * 2.Use the identified column range to set all pixels below these pixels to foreground.
     *
     * In more detail the merging algorithm works as follows:
     * - We iterate over each pixel in the image; its value is currentLabelValue:
     *      - if currentLabelValue is background we abort, because we only want to merge foreground components.
     *      - if currentLabelValue is foreground, we iterate over the pixel column below that pixel; the position inside
     *      the column is given by nextColumnPosition:
     *          - if nextColumnPosition in maskForComponentMerging is 0, we abort, because we do not want to merge columns which (partially) lie outside maskForComponentMerging
     *          - else if: nextColumnPosition in labelingImage is equal to currentLabelValue, then we abort, because we are inside the current component
     *          - else if: nextColumnPosition in labelingImage has a non-zero label value that is different from currentLabelValue, we encountered a pixel from a differing component. We store the value of verticalPixelOffset (to use for the actual merging) and use performMergeForThisPixel to signal the need for merging. Then break the loop.
     *      - Merging is done for the current pixel, if performMergeForThisPixel==true, so that all value between the current pixel position in currentPixelPosition and currentPixelPosition[1]+verticalPixelOffset are set to 1,
     * @param componentMask
     * @param maskForComponentMerging
     * @param labelingImage
     */
    private void mergeDifferingConnectedComponentsInMask(Img<BitType> componentMask, Img<BitType> maskForComponentMerging, Img<IntType> labelingImage) {
        ExtendedRandomAccessibleInterval<IntType, Img<IntType>> labelingImageExtended = Views.extendZero(labelingImage); /* extend to avoid running out of bounds */
        ExtendedRandomAccessibleInterval<BitType, Img<BitType>> componentMaskExtended = Views.extendZero(componentMask); /* extend to avoid running out of bounds */
        ExtendedRandomAccessibleInterval<BitType, Img<BitType>> maskForComponentMergingExtended = Views.extendZero(maskForComponentMerging); /* extend to avoid running out of bounds */
        Cursor<IntType> labelingCursor = labelingImage.localizingCursor();
        RandomAccess<IntType> labelRandomAccess = labelingImageExtended.randomAccess();
        RandomAccess<BitType> componentMaskRandomAccess = componentMaskExtended.randomAccess();
        RandomAccess<BitType> mergingMaskRandomAccess = maskForComponentMergingExtended.randomAccess();

        long[] currentPixelPosition = new long[labelingImage.numDimensions()];
        while (labelingCursor.hasNext()) {
            labelingCursor.next();
            labelingCursor.localize(currentPixelPosition);
            int currentLabelValue = labelingCursor.get().getInteger();

            if (currentLabelValue == 0) {
                continue; /* pixel value is background, so nothing to do */
            }

            boolean performMergeForThisPixel = false; /* boolean which tells if the current pixel has a component below, which should be merged */
            int pixelColumnEndPosition = 0;
            for (int verticalPixelOffset = 1; ; verticalPixelOffset++) { /* we increase verticalPixelOffset until we encounter a background pixel in maskForComponentMerging or until we encounter pixel with a different (non-zero) label from the current component in componentMaskExtended; this means we have encountered a new component */
                long[] nextColumnPosition = new long[]{currentPixelPosition[0], currentPixelPosition[1] + verticalPixelOffset};

                mergingMaskRandomAccess.setPosition(nextColumnPosition);
                boolean mergingMaskPixelValue = mergingMaskRandomAccess.get().get();
                labelRandomAccess.setPosition(nextColumnPosition);
                int nextPixelValue = labelRandomAccess.get().get();

                if (!mergingMaskPixelValue) {
                    break;  /* pixel column lies outside of parent component in the merging mask; abort because we do not want to merge in this case */
                }
                else if (nextPixelValue == currentLabelValue) {
                    break; /* we encountered a pixel with the same value to the current one; this means we are inside the current component and can therefore abort */
                }
                else if (nextPixelValue != 0 && nextPixelValue != currentLabelValue) {
                    performMergeForThisPixel = true;
                    pixelColumnEndPosition = verticalPixelOffset;
                    break; /* merge needed; break and continue with merging the pixel column */
                }
            }

            if (!performMergeForThisPixel) {
                continue;
            }

            for (int i = 1; i <= pixelColumnEndPosition; i++) {
                long[] nextPosition = new long[]{currentPixelPosition[0], currentPixelPosition[1] + i};
                componentMaskRandomAccess.setPosition(nextPosition);
                componentMaskRandomAccess.get().setOne();
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
