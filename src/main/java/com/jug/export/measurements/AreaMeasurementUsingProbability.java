package com.jug.export.measurements;

import com.jug.export.ResultTable;
import com.jug.export.ResultTableColumn;
import com.jug.util.ComponentTreeUtils;
import com.jug.util.componenttree.ComponentInterface;
import com.moma.auxiliary.Plotting;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.MaskInterval;
import net.imglib2.roi.Masks;
import net.imglib2.roi.Regions;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;

import java.util.ArrayList;
import java.util.List;

public class AreaMeasurementUsingProbability implements SegmentMeasurementInterface {
    private ResultTableColumn<Double> areaCol;

    @Override
    public void setOutputTable(ResultTable outputTable) {
        areaCol = outputTable.addColumn(new ResultTableColumn<>("area_using_probability_map__px"));
    }

    @Override
    public void measure(SegmentMeasurementDataInterface data) {
        ComponentInterface component = data.getComponentToMeasure();
        List<ComponentInterface> allComponents = data.getAllOptimalComponents();
        RandomAccessibleInterval<FloatType> probabilityMap = data.getImageProvider().getImgProbsAt(data.getFrameIndex());

        List<ComponentInterface> neighbors = ComponentTreeUtils.getNeighborComponents(component, allComponents);

        double area = calculateArea(component, probabilityMap, neighbors);
        areaCol.addValue(area);
    }

    private double calculateArea(ComponentInterface component, RandomAccessibleInterval<FloatType> probabilityMap, List<ComponentInterface> neighbors) {
        MaskInterval dilatedMask = component.getDilatedMask();
        MaskInterval componentCoreMask = component.getErodedMask();
        MaskInterval componentBorderMask = dilatedMask.minus(componentCoreMask);

        MaskInterval neighborMaskUnion = neighbors.get(0).getDilatedMask();
        for(ComponentInterface neighbor : neighbors){
            neighborMaskUnion = neighborMaskUnion.or(neighbor.getDilatedMask()); /* combine neighbor masks */
        }

        MaskInterval intersectingBorderPixelMask = neighborMaskUnion.and(componentBorderMask);
        componentBorderMask = componentBorderMask.minus(neighborMaskUnion); /* remove intersecting pixels */

//        new ij.ImageJ();
//
//        ImageJFunctions.show(Masks.toRandomAccessibleInterval(neighborMaskUnion));
//        ImageJFunctions.show(Masks.toRandomAccessibleInterval(componentBorderMask), "componentBorderMask");
//
//        ImageJFunctions.show(Masks.toRandomAccessibleInterval(intersectingBorderPixelMask), "intersectingBorderPixelMask");

        Double totalArea = 0D;
//        Regions.iterable(componentCoreMask)
//        Regions.countTrue(componentCoreMask)
        IterableInterval<FloatType> corePixels = Regions.sample(componentCoreMask, probabilityMap);
        Cursor<FloatType> c1 = corePixels.cursor();
        while(c1.hasNext()){
            c1.next();
            totalArea++;
        }

//        ImageJFunctions.show(probabilityMap);

        IterableInterval<FloatType> borderPixels = Regions.sample(componentBorderMask, probabilityMap);
//        ImageJFunctions.show(probabilityMap, "probabilityMap");
        Cursor<FloatType> c2 = borderPixels.cursor();
        while(c2.hasNext()){
            c2.next();
            double val = c2.get().getRealDouble();
            totalArea += val;
        }

        IterableInterval<FloatType> intersectingBorderPixels = Regions.sample(intersectingBorderPixelMask, probabilityMap);
        Cursor<FloatType> c3 = intersectingBorderPixels.cursor();
        while(c3.hasNext()){
            c3.next();
            double val = c3.get().getRealDouble() / 2; /* we count intersecting border pixels only half, because they belong to two neighboring components */
            totalArea += val;
        }

//        ImageJFunctions.show(Masks.toRandomAccessibleInterval(dilatedMask));
//        ImageJFunctions.show(Masks.toRandomAccessibleInterval(erodedMask));
//

//        RandomAccessibleInterval<BitType> image = Plotting.createImageWithComponentsNew(neighbors, new BitType(true));
//        ImageJFunctions.show(image);
//
//        List<ComponentInterface> componentList = new ArrayList<>();
//        componentList.add(component);
//        RandomAccessibleInterval<BitType> image = Plotting.createImageWithComponentsNew(componentList, new BitType(true));
//        ImageJFunctions.show(image, "component");
        return totalArea;
    }
}
