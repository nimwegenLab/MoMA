package com.jug.export.measurements;

import com.jug.export.ResultTable;
import com.jug.export.ResultTableColumn;
import com.jug.util.ComponentTreeUtils;
import com.jug.util.componenttree.ComponentInterface;
import com.moma.auxiliary.Plotting;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.logic.BitType;

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

        List<ComponentInterface> neighbors = ComponentTreeUtils.getNeighborComponents(component, allComponents);

        RandomAccessibleInterval<BitType> image = Plotting.createImageWithComponentsNew(neighbors, new BitType(true));
        ImageJFunctions.show(image);

        List<ComponentInterface> componentList = new ArrayList<>();
        componentList.add(component);
        image = Plotting.createImageWithComponentsNew(componentList, new BitType(true));
        ImageJFunctions.show(image);
    }
}
