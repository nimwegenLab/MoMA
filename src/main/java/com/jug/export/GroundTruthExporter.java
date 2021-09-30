package com.jug.export;

import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.integer.IntType;
import org.apache.commons.lang.NotImplementedException;

import java.io.File;
import java.util.List;

public class GroundTruthExporter {
    public void export(File outputFolder, List<SegmentRecord> cellTrackStartingPoints) {
        SegmentRecord firstEntry = cellTrackStartingPoints.get(0);
        RandomAccessibleInterval sourceImage = ((AdvancedComponent) firstEntry.hyp.getWrappedComponent()).getSourceImage();
        System.out.println("stop");
//        createGroundTruthTiffStacks(10);
    }

//    public void createGroundTruthTiffStacks(int nrOfFrames) {
//        // TODO: Implement generation of GT stacks
//        RandomAccessibleInterval sourceImage = ((AdvancedComponent) first).getSourceImage();
//        long xDim = sourceImage.dimension(0);
//        long yDim = sourceImage.dimension(1);
//        ArrayImgFactory<IntType> imgFactory = new ArrayImgFactory<IntType>();
//        imgFactory.create()
//        throw new NotImplementedException();
//    }
}
