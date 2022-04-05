package com.jug.export.measurements;

import com.jug.export.ResultTable;
import com.jug.export.ResultTableColumn;
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
        List<ComponentInterface> otherComponents = data.getOptimalComponents();
        otherComponents.remove(component);

        

        RandomAccessibleInterval<BitType> image = Plotting.createImageWithComponentsNew(otherComponents, new BitType(true));
        ImageJFunctions.show(image);
    }
}
