package com.jug.export;

import com.jug.MoMA;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.imglib2.Imglib2Utils;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;

import java.io.File;
import java.util.List;

/**
 * This class store the resulting cell masks to a TIFF stack with axes [XYZT]. The slices in Z are as follows:
 * Z=0: cell masks at T in the intput image.
 * Z=1: cell masks of the corresponding parents at T (which is a copy of the slice Z=0, T=T-1, for all T except T=0).
 */
public class CellMaskExporter {
    private final Imglib2Utils imglib2Utils;
    Img<IntType> imgResult;

    public CellMaskExporter(Imglib2Utils imglib2Utils) {
        this.imglib2Utils = imglib2Utils;
    }

    private static <T extends Type<T>> void drawSegmentToImage(Iterable<Localizable> component,
                                                               T value,
                                                               RandomAccessibleInterval<T> targetImage) {
        RandomAccess<T> out = targetImage.randomAccess();
        for (Localizable location : component) {
            out.setPosition(location);
            out.get().set(value);
        }
    }

    public void export(File outputFolder, List<SegmentRecord> cellTrackStartingPoints) {
        SegmentRecord firstEntry = cellTrackStartingPoints.get(0);
        int nrOfFrames = getNumberOfFrames(cellTrackStartingPoints);
        imgResult = createGroundTruthTiffStacks(nrOfFrames, firstEntry.hyp.getWrappedComponent());
        writeSegmentsToResultImage(cellTrackStartingPoints);
        copySliceOfParentComponents();
        saveResultImageToFile(new File(outputFolder, "ExportedCellMasks_" + MoMA.getDefaultFilenameDecoration() + ".tif"));
    }

    private void copySliceOfParentComponents() {
        int timeMax = (int) imgResult.dimension(4);
        for (int t = 1; t < timeMax; t++) {
            IntervalView<IntType> sourceImage = imglib2Utils.getImageSlice(imgResult, 0, 0, t - 1);
            IntervalView<IntType> targetImage = imglib2Utils.getImageSlice(imgResult, 0, 1, t);
            imglib2Utils.copyImage(sourceImage, targetImage);
        }
    }

    private void saveResultImageToFile(File outputFile) {
        imglib2Utils.saveImage(imgResult, outputFile.getAbsolutePath());
    }

    private Img<IntType> createGroundTruthTiffStacks(int nrOfFrames, AdvancedComponent<FloatType> component) {
        RandomAccessibleInterval sourceImage = component.getSourceImage();
        long xDim = sourceImage.dimension(0);
        long yDim = sourceImage.dimension(1);
        ArrayImgFactory<IntType> imgFactory = new ArrayImgFactory<>(new IntType());
        return imgFactory.create(xDim, yDim, 1, 2, nrOfFrames); /* channel order in an ImagePlus (which we use for storing), is: XYCZT; so those are the axes we generate here */
    }

    private void writeSegmentsToResultImage(List<SegmentRecord> cellTrackStartingPoints) {
        for (SegmentRecord segment : cellTrackStartingPoints) {
            while (segment.exists) {
                IntervalView<IntType> slice = imglib2Utils.getImageSlice(imgResult, 0, 0, segment.timestep);
                drawSegmentToImage(segment.hyp.getWrappedComponent(), new IntType(segment.id), slice);
                segment = segment.nextSegmentInTime();
            }
        }
    }

    /**
     * Recover first and last frames of the segments, since I do not trust that their timesteps, will be in sync and
     * have the same value as tmin and tmax.
     *
     * @param cellTrackStartingPoints
     * @return
     */
    private int getNumberOfFrames(List<SegmentRecord> cellTrackStartingPoints) {
        int firstFrame = Integer.MAX_VALUE;
        int lastFrame = -1;
        for (SegmentRecord segment : cellTrackStartingPoints) {
            while (segment.exists) {
                int timestep = segment.timestep;
                if (timestep < firstFrame) {
                    firstFrame = timestep;
                }
                if (timestep > lastFrame) {
                    lastFrame = timestep;
                }
                segment = segment.nextSegmentInTime();
            }
        }
        return lastFrame + 1 - firstFrame;
    }
}
