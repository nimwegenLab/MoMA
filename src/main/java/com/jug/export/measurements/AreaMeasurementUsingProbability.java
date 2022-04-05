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
        areaCol = outputTable.addColumn(new ResultTableColumn<Double>("spine_length_calculation_successful__boolean"));
    }

    @Override
    public void measure(SegmentMeasurementDataInterface data) {
        ComponentInterface component = data.getComponentToMeasure();
        List<ComponentInterface> allComponents = data.getAllOptimalComponents();
        RandomAccessibleInterval<FloatType> probabilityMap = data.getImageProvider().getImgProbs();

        List<ComponentInterface> neighbors = ComponentTreeUtils.getNeighborComponents(component, allComponents);

        MaskInterval dilatedMask = component.getDilatedMask();
        MaskInterval componentCoreMask = component.getErodedMask();
        MaskInterval componentBorderMask = dilatedMask.minus(componentCoreMask);

        MaskInterval neighborMaskUnion = neighbors.get(0).getDilatedMask();
        for(ComponentInterface neighbor : neighbors){
            neighborMaskUnion = neighborMaskUnion.or(neighbor.getDilatedMask()); /* combine neighbor masks */
        }

        MaskInterval intersectingBorderPixels = neighborMaskUnion.and(componentBorderMask);
        componentBorderMask = componentBorderMask.minus(neighborMaskUnion); /* remove intersecting pixels */

        new ij.ImageJ();

//        ImageJFunctions.show(Masks.toRandomAccessibleInterval(neighborMaskUnion));
        ImageJFunctions.show(Masks.toRandomAccessibleInterval(componentBorderMask), "componentBorderMask");

        ImageJFunctions.show(Masks.toRandomAccessibleInterval(intersectingBorderPixels), "intersectingBorderPixels");

        Double totalArea = 0D;

        ImageJFunctions.show(probabilityMap);

        RandomAccessibleInterval sourceImage = component.getSourceImage();
        IterableInterval<FloatType> borderPixels = Regions.sample(componentBorderMask, sourceImage);
        ImageJFunctions.show(probabilityMap, "probabilityMap");
        Cursor<FloatType> c = borderPixels.cursor();
        while(c.hasNext()){
            c.next();
            double val = c.get().getRealDouble();
            totalArea += val;
        }

//        ImageJFunctions.show(Masks.toRandomAccessibleInterval(dilatedMask));
//        ImageJFunctions.show(Masks.toRandomAccessibleInterval(erodedMask));
//

//        RandomAccessibleInterval<BitType> image = Plotting.createImageWithComponentsNew(neighbors, new BitType(true));
//        ImageJFunctions.show(image);
//
        List<ComponentInterface> componentList = new ArrayList<>();
        componentList.add(component);
        RandomAccessibleInterval<BitType> image = Plotting.createImageWithComponentsNew(componentList, new BitType(true));
        ImageJFunctions.show(image, "component");
    }
}
