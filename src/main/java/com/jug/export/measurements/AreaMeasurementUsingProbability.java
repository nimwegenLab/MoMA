package com.jug.export.measurements;

import com.jug.export.ResultTable;
import com.jug.export.ResultTableColumn;
import com.jug.util.ComponentTreeUtils;
import com.jug.util.componenttree.ComponentInterface;
import com.moma.auxiliary.Plotting;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.logic.BitType;

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

        if (!allComponents.contains(component)) throw new RuntimeException("target component must be in list of all components");

        ComponentTreeUtils.sortComponentsByPosition(allComponents);

        allComponents.remove(component);

        RandomAccessibleInterval<BitType> image = Plotting.createImageWithComponentsNew(allComponents, new BitType(true));
        ImageJFunctions.show(image);
    }
}
