package com.jug.export;

import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import org.apache.commons.lang.NotImplementedException;

import java.io.File;
import java.util.List;

public class GroundTruthExporter {
    Img<IntType> imgResult;

    public void export(File outputFolder, List<SegmentRecord> cellTrackStartingPoints) {
        SegmentRecord firstEntry = cellTrackStartingPoints.get(0);
//        int nrOfFrames = getNumberOfFrames(cellTrackStartingPoints);
        int nrOfFrames = 10;
        createGroundTruthTiffStacks(nrOfFrames, firstEntry.hyp.getWrappedComponent());
        throw new NotImplementedException();
    }

    private void createGroundTruthTiffStacks(int nrOfFrames, AdvancedComponent<FloatType> component) {
        // TODO: Implement generation of GT stacks
        RandomAccessibleInterval sourceImage = component.getSourceImage();
        long xDim = sourceImage.dimension(0);
        long yDim = sourceImage.dimension(1);
        ArrayImgFactory<IntType> imgFactory = new ArrayImgFactory<>(new IntType());
//        Img img = ImgView.wrap(imgFactory.create(xDim, yDim, 2, nrOfFrames));
        imgResult = imgFactory.create(xDim, yDim, 2, nrOfFrames);
//        RandomAccessibleInterval<IntType> img2 = imgFactory.create(xDim, yDim, 2, nrOfFrames);
        throw new NotImplementedException();
    }

//    private int getNumberOfFrames(List<SegmentRecord> cellTrackStartingPoints) {
//        int firstFrame = Integer.MAX_VALUE;
//        int lastFrame = -1;
//        for (SegmentRecord segment : cellTrackStartingPoints) {
//            while(segment.nextSegmentInTime()){
//
//            }
//            int timestep = segment.timestep;
//
//        }
//        throw new NotImplementedException();
//    }
}
