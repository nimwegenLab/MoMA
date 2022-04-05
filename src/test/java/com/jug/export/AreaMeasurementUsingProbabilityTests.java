package com.jug.export;

import com.jug.export.measurements.AreaMeasurementUsingProbability;
import com.jug.export.measurements.SegmentMeasurementData;
import com.jug.util.TestUtils;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.ComponentInterface;
import com.moma.auxiliary.Plotting;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AreaMeasurementUsingProbabilityTests {
    private final ImageJ ij;
    private final TestUtils testUtils;

    public static void main(String[] args) throws IOException {
        new AreaMeasurementUsingProbabilityTests().test_area_measurement_1();
    }

    public AreaMeasurementUsingProbabilityTests() {
        ij = new ImageJ();
        testUtils = new TestUtils(ij);
    }

    @Test
    public void test_area_measurement_1() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6/frame90_repeated__cropped__20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6__model_9e5727e4ed18802f4ab04c7494ef8992d798f4d64d5fd75e285b9a3d83b13ac9.tif";
        AreaMeasurementUsingProbability sut = new AreaMeasurementUsingProbability();
        ResultTable resultTable = new ResultTable(",");
        sut.setOutputTable(resultTable);

        List<ComponentInterface> components = new ArrayList<>();
        for (int componentIndex = 0; componentIndex < 5; componentIndex++) {
            ValuePair<AdvancedComponent<FloatType>, RandomAccessibleInterval<BitType>> componentAndImage = testUtils.getComponentWithImage(imageFile,
                    componentIndex,
                    new BitType(true));
            components.add(componentAndImage.getA());
        }

        RandomAccessibleInterval<BitType> image = Plotting.createImageWithComponentsNew(components, new BitType(true));
        ImageJFunctions.show(image);

        sut.measure(new SegmentMeasurementData(components.get(0), components));
        System.out.println();
    }
}
