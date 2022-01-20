package com.jug.export;

import com.jug.MoMA;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.ComponentInterface;
import com.jug.util.imglib2.Imglib2Utils;
import com.jug.util.imglib2.OverlayUtils;
import com.jug.util.math.Vector2D;
import com.jug.util.math.Vector2DPolyline;
import com.moma.auxiliary.Plotting;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.process.LUT;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * This class store the resulting cell masks to a TIFF stack with axes [XYZT]. The slices in Z are as follows:
 * Z=0: cell masks at T in the intput image.
 * Z=1: cell masks of the corresponding parents at T (which is a copy of the slice Z=0, T=T-1, for all T except T=0).
 */
public class CellMaskExporter implements ResultExporterInterface {
    private final Imglib2Utils imglib2Utils;
    private OverlayUtils overlayUtils;
    private Supplier<String> defaultFilenameDecorationSupplier;
    Img<IntType> imgResult;
    private HashMap<String, Color> featureColors;

    public CellMaskExporter(Imglib2Utils imglib2Utils, OverlayUtils overlayUtils, Supplier<String> defaultFilenameDecorationSupplier) {
        this.imglib2Utils = imglib2Utils;
        this.overlayUtils = overlayUtils;
        this.defaultFilenameDecorationSupplier = defaultFilenameDecorationSupplier;
        featureColors = new HashMap<>();
        featureColors.put("contour", Color.BLUE);
        featureColors.put("spine", Color.RED);
        featureColors.put("medialline", Color.YELLOW);
        featureColors.put("orientedbbox", Color.GREEN);
    }

    public void export(File outputFolder, List<SegmentRecord> cellTrackStartingPoints) {
        SegmentRecord firstEntry = cellTrackStartingPoints.get(0);
        int nrOfFrames = getNumberOfFrames(cellTrackStartingPoints);
        imgResult = createGroundTruthTiffStacks(nrOfFrames, firstEntry.hyp.getWrappedComponent());
        writeSegmentsToResultImage(cellTrackStartingPoints);
//        copySliceOfParentComponents();
        String defaultFileNameDecoration = defaultFilenameDecorationSupplier.get();
        saveResultImageToFile(new File(outputFolder, "ExportedCellMasks__" + defaultFileNameDecoration + ".tif"));
    }

    private void copySliceOfParentComponents() {
        int timeMax = (int) imgResult.dimension(4);
        for (int t = 1; t < timeMax; t++) {
            IntervalView<IntType> sourceImage = imglib2Utils.getImageSlice(imgResult, 0, 0, t - 1);
            IntervalView<IntType> targetImage = imglib2Utils.getImageSlice(imgResult, 0, 1, t);
            imglib2Utils.copyImage(sourceImage, targetImage);
        }
    }

    private FinalInterval getRoiForSaving(Img<?> img) {
        long start_index_horz = 0;
        long end_index_horz = img.max(0);
        return new FinalInterval(
                new long[]{start_index_horz, MoMA.dic.getConfigurationManager().CELL_DETECTION_ROI_OFFSET_TOP, 0, 0, 0},
                new long[]{end_index_horz, img.max(1), img.max(2), img.max(3), img.max(4)}
        );
    }

    private void saveResultImageToFile(File outputFile) {
        ImagePlus tmp_image = ImageJFunctions.wrap(imgResult, "imgResults");
        tmp_image.setOverlay(overlay);
//        tmp_image.setLut(new LUT());
//        tmp_image.setCha
//        IJ.run(tmp_image, "Grays", "");
        IJ.saveAsTiff(tmp_image, outputFile.getAbsolutePath());
    }

    private Img<IntType> createGroundTruthTiffStacks(int nrOfFrames, AdvancedComponent<FloatType> component) {
        RandomAccessibleInterval sourceImage = component.getSourceImage();
        long xDim = sourceImage.dimension(0);
        long yDim = sourceImage.dimension(1);
        long nrofChannels = 2;
        long nrOfSlices = 1;
        ArrayImgFactory<IntType> imgFactory = new ArrayImgFactory<>(new IntType());
        Img<IntType> img = imgFactory.create(xDim, yDim, nrofChannels, nrOfSlices, nrOfFrames);
        return img; /* channel order in an ImagePlus (which we use for storing), is: XYCZT; so those are the axes we generate here */
//        return imgFactory.create(xDim, yDim, 1, 2, nrOfFrames); /* channel order in an ImagePlus (which we use for storing), is: XYCZT; so those are the axes we generate here */
    }

    private void writeSegmentsToResultImage(List<SegmentRecord> cellTrackStartingPoints) {
        for (SegmentRecord segment : cellTrackStartingPoints) {
            int segmentCounter = 0;
            while (segment.exists) {
                IntervalView<IntType> z0slice = imglib2Utils.getImageSlice(imgResult, 0, 0, segment.timestep);
                Plotting.drawSegmentToImage(segment.hyp.getWrappedComponent(), new IntType(segment.getId()), z0slice);
                int sourceSegmentId;
                if (segmentCounter == 0) { /* I use the cell counter to check, if this is the first segment in the track of the cell. If so we use the cell-ID of the parent-cell because it was a . Else use the ID of the previous instance of this cell, because it was a mapping-assignment in this case. This hack is needed, because I do not have a reference to the source-component from a given target-component. */
                    sourceSegmentId = segment.getParentId();
                } else {
                    sourceSegmentId = segment.getId();
                }
                IntervalView<IntType> z1slice = imglib2Utils.getImageSlice(imgResult, 1, 0, segment.timestep);
                Plotting.drawSegmentToImage(segment.hyp.getWrappedComponent(), new IntType(sourceSegmentId), z1slice);
                addComponentFeaturesToOverlay(segment);
                segment = segment.nextSegmentInTime();
                segmentCounter++;
            }
        }
    }

//    private HashMap<Integer,Overlay> overlays = new HashMap<>();
    private Overlay overlay = new Overlay();
    private void addComponentFeaturesToOverlay(SegmentRecord segment) {
        int timestep = segment.timestep;
//        Overlay currentOverlay;
//        if(!overlays.keySet().contains(timestep)){
//            currentOverlay = overlays.put(timestep, new Overlay());
//        } else{
//            currentOverlay = overlays.get(timestep);
//        }
        ComponentInterface component = segment.hyp.getWrappedComponent();
        Set<String> featureNames = component.getComponentFeatureNames();
        for (String featureName : featureNames){
            Vector2DPolyline feature = component.getComponentFeature(featureName);
//            feature.shiftMutate(new Vector2D(0.5, 0.5));
            String roiName = featureName + "__timestep_" + timestep + "__segId_" + segment.id;
//            System.out.println("roiName: " + roiName);
            if(feature.isEmpty()){
//                System.out.println("isEmpty");
                continue;
            }
            Roi roi;
            if(feature.getType() == Vector2DPolyline.PolyshapeType.POLYGON){
                roi = overlayUtils.convertToRoi(feature.getPolygon2D());
            } else{
                roi = overlayUtils.convertToRoi(feature.getPolyline());
            }
            roi.setStrokeColor(featureColors.get(featureName));
            roi.setName(roiName);
            roi.setPosition(0, 0, timestep + 1);  /* indexing in ImageJ is 1-based, so I need to add +1 here to the 0-based time steps. */
            overlay.add(roi);
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
