package com.jug.export.measurements;

import com.jug.export.ResultTable;
import com.jug.export.ResultTableColumn;
import com.jug.util.ComponentTreeUtils;
import com.jug.util.componenttree.ComponentInterface;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.MaskInterval;
import net.imglib2.roi.Regions;
import net.imglib2.type.numeric.real.FloatType;
import org.jetbrains.annotations.NotNull;

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
        List<ComponentInterface> allOptimalComponents = data.getAllOptimalComponents();
        RandomAccessibleInterval<FloatType> probabilityMap = data.getImageProvider().getImgProbsAt(data.getFrameIndex());

        List<ComponentInterface> neighbors = ComponentTreeUtils.getNeighborComponents(component, allOptimalComponents);

        double area = calculateArea(component, probabilityMap, neighbors);
        areaCol.addValue(area);
    }

    /**
     * Calculates the area of the component using the probability map. Depending on the number of neighboring components
     * it will take into account the overlapping pixels of neighboring components by calling the appropriate method.
     *
     * @param component
     * @param probabilityMap
     * @param neighbors
     * @return
     */
    private double calculateArea(ComponentInterface component, RandomAccessibleInterval<FloatType> probabilityMap, List<ComponentInterface> neighbors) {
        if (neighbors.isEmpty()) {
            return calculateAreaWithoutNeighboringComponents(component, probabilityMap);
        } else {
            return calculateAreaConsideringNeighboringComponents(component, probabilityMap, neighbors);
        }
    }

    /**
     * Calculates the area of the component using the probability map.
     * The area is calculated by counting the pixels in the "core" area of the component and the summing the probability
     * values of the pixels in the border mask as provided {@link com.jug.util.componenttree.AdvancedComponent#getBorderMask}.
     *
     * @param component
     * @param probabilityMap
     * @return
     */
    @NotNull
    private static Double calculateAreaWithoutNeighboringComponents(ComponentInterface component, RandomAccessibleInterval<FloatType> probabilityMap) {
        Double totalArea = 0D;

        totalArea += Regions.countTrue(component.getCoreMaskImg()); /* add sum of the number of pixels in the "core" area of the mask */

        MaskInterval componentBorderMask = component.getBorderMask();
        IterableInterval<FloatType> borderPixels = Regions.sample(componentBorderMask, probabilityMap);
        Cursor<FloatType> c2 = borderPixels.cursor();
        while (c2.hasNext()) {
            c2.next();
            double val = c2.get().getRealDouble();
            totalArea += val; /* add sum of the values of border pixels in probabilityMap that do not overlap with other components */
        }
        return totalArea;
    }

    /**
     * Calculates the area of the component using the probability map.
     * The area is calculated by counting the pixels in the "core" area of the component and the summing the probability
     * values of the pixels in the border mask as provided {@link com.jug.util.componenttree.AdvancedComponent#getBorderMask}.
     * Pixel values of the border pixels that overlap with neighboring components are counted only one-half.
     *
     * @param component
     * @param probabilityMap
     * @param neighbors
     * @return
     */
    @NotNull
    private static Double calculateAreaConsideringNeighboringComponents(ComponentInterface component, RandomAccessibleInterval<FloatType> probabilityMap, List<ComponentInterface> neighbors) {
        MaskInterval componentBorderMask = component.getBorderMask();

        MaskInterval neighborBorderMaskUnion = neighbors.get(0).getBorderMask();
        for(ComponentInterface neighbor : neighbors){
            neighborBorderMaskUnion = neighborBorderMaskUnion.or(neighbor.getBorderMask()); /* combine neighbor masks */
        }

        MaskInterval intersectingBorderPixelMask = neighborBorderMaskUnion.and(componentBorderMask);
        componentBorderMask = componentBorderMask.minus(intersectingBorderPixelMask); /* remove intersecting pixels */

        Double totalArea = 0D;

        totalArea += Regions.countTrue(component.getCoreMaskImg()); /* add sum of the number of pixels in the "core" area of the mask */

        IterableInterval<FloatType> borderPixels = Regions.sample(componentBorderMask, probabilityMap);
        Cursor<FloatType> c2 = borderPixels.cursor();
        while(c2.hasNext()){
            c2.next();
            double val = c2.get().getRealDouble();
            totalArea += val; /* add sum of the values of border pixels in probabilityMap that do not overlap with other components */
        }

        IterableInterval<FloatType> intersectingBorderPixels = Regions.sample(intersectingBorderPixelMask, probabilityMap);
        Cursor<FloatType> c3 = intersectingBorderPixels.cursor();
        while(c3.hasNext()){
            c3.next();
            double val = c3.get().getRealDouble() / 2; /* sum the number of values of the border pixels in probabilityMap that overlap with other components; we count intersecting border pixels only half, because they belong to two neighboring components */
            totalArea += val;
        }
        return totalArea;
    }
}
