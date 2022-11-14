package com.jug.export;

import com.jug.export.measurements.SegmentMeasurementData;
import com.jug.export.measurements.SpineLengthMeasurement;
import com.jug.util.TestUtils;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.ContourCalculator;
import com.jug.util.componenttree.MedialLineCalculator;
import com.jug.util.componenttree.SpineCalculator;
import com.jug.util.imglib2.Imglib2Utils;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpineLengthMeasurementTest {
    private final TestUtils testUtils;
    private final ImageJ ij;
    private final SpineLengthMeasurement sut;

    public SpineLengthMeasurementTest() {
        ij = new ImageJ();
        testUtils = new TestUtils(ij);
        Imglib2Utils imglib2Utils = new Imglib2Utils(ij.op());
        MedialLineCalculator medialLineCalculator = new MedialLineCalculator(ij.op(), imglib2Utils);
        SpineCalculator spineCalculator = new SpineCalculator(7, 7, 3.5);
        ContourCalculator contourCalculator = new ContourCalculator(ij.op());
        sut = new SpineLengthMeasurement(medialLineCalculator, spineCalculator, contourCalculator);
    }

    @Test
    public void test__measure() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6/frame90_repeated__cropped__20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6__model_9e5727e4ed18802f4ab04c7494ef8992d798f4d64d5fd75e285b9a3d83b13ac9.tif";

        ResultTable resultTable = new ResultTable(",");
        sut.setOutputTable(resultTable);

        for (int componentIndex = 0; componentIndex < 5; componentIndex++) {
            ValuePair<AdvancedComponent<FloatType>, RandomAccessibleInterval<BitType>> componentAndImage = testUtils.getComponentWithImage(imageFile,
                    componentIndex,
                    new BitType(true));
            AdvancedComponent<FloatType> component = componentAndImage.getA();
            SegmentMeasurementData data = new SegmentMeasurementData(component, new ArrayList<>(), testUtils.getImageProvider(), testUtils.getFrameIndex());
            sut.measure(data);
        }
        ResultTableColumn<Double> column = resultTable.columnList.get(1);

        double[] expected = new double[]{
                41.13496287766395D,
                47.08039933241312D,
                137.37517972316357D,
                45.412324125174486D,
                79.65491798744475D,
        };
        for (int componentIndex = 0; componentIndex < 5; componentIndex++) {
            System.out.println("componentIndex: " + componentIndex);
            assertEquals(expected[componentIndex], column.getValue(componentIndex), 0.0001);
        }
    }
}
