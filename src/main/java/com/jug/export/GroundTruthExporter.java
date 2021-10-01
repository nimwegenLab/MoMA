package com.jug.export;

import com.jug.MoMA;
import com.jug.util.componenttree.AdvancedComponent;
import ij.IJ;
import ij.ImagePlus;
import io.scif.img.ImgSaver;
import org.scijava.Context;
import org.scijava.io.DefaultIOService;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.io.IOPlugin;
import org.scijava.io.location.FileLocation;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GroundTruthExporter {
    Img<IntType> imgResult;
    private Context context;

    public GroundTruthExporter(Context context) {
        this.context = context;
    }

    public void export(File outputFolder, List<SegmentRecord> cellTrackStartingPoints) {
        SegmentRecord firstEntry = cellTrackStartingPoints.get(0);
        int nrOfFrames = getNumberOfFrames(cellTrackStartingPoints);
        imgResult = createGroundTruthTiffStacks(nrOfFrames, firstEntry.hyp.getWrappedComponent());
        writeSegmentsToResultImage(cellTrackStartingPoints);
        saveResultImageToFile(new File(outputFolder, "ExportedCellMasks_" + MoMA.getDefaultFilenameDecoration() + ".tif"));
    }

    private void saveResultImageToFile(File outputFile) {
        /* ATTEMPT 1 TO SAVE IMG TO DISK */
//        ImagePlus tmp_image = ImageJFunctions.wrap(Views.permute(imgResult, 2, 3 ), "imgResults");
////        ImagePlus tmp_image = ImageJFunctions.wrap(imgResult, "imgResults");
//        IJ.saveAsTiff(tmp_image, outputFile.getAbsolutePath());

        /* ATTEMPT 2 TO SAVE IMG TO DISK */
        ImgSaver saver = new ImgSaver(context);
        FileLocation imgName = new FileLocation(outputFile);
        try {
            saver.saveImg(imgName, imgResult);
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }

        /* ATTEMPT 3 */
//        IOPlugin<Img<IntType>> saver = new DefaultIOService().getSaver(imgResult, outputFile.getAbsolutePath());
//        try {
//            saver.save(imgResult, outputFile.getAbsolutePath());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private Img<IntType> createGroundTruthTiffStacks(int nrOfFrames, AdvancedComponent<FloatType> component) {
        RandomAccessibleInterval sourceImage = component.getSourceImage();
        long xDim = sourceImage.dimension(0);
        long yDim = sourceImage.dimension(1);
        ArrayImgFactory<IntType> imgFactory = new ArrayImgFactory<>(new IntType());
        return imgFactory.create(xDim, yDim, 2, nrOfFrames);
    }

    private void writeSegmentsToResultImage(List<SegmentRecord> cellTrackStartingPoints) {
        for (SegmentRecord segment : cellTrackStartingPoints) {
            do {
                IntervalView<IntType> channelSlice = Views.hyperSlice(imgResult, 3, 0);
                IntervalView<IntType> slice = Views.hyperSlice(channelSlice, 2, segment.timestep);
                drawSegmentToImage(segment.hyp.getWrappedComponent(), new IntType(segment.id), slice);
                segment = segment.nextSegmentInTime();
            }
            while (segment.exists);
        }
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
            do {
                int timestep = segment.timestep;
                if (timestep < firstFrame) {
                    firstFrame = timestep;
                }
                if (timestep > lastFrame) {
                    lastFrame = timestep;
                }
                segment = segment.nextSegmentInTime();
            }
            while (segment.exists);
        }
        return lastFrame - firstFrame;
    }
}
