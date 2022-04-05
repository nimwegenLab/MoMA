package com.jug.export;

import com.jug.export.measurements.AreaMeasurementUsingProbability;
import com.jug.export.measurements.SegmentMeasurementData;
import com.jug.util.TestUtils;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.ComponentInterface;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class AreaMeasurementUsingProbabilityTests {
    private final ImageJ ij;
    private final TestUtils testUtils;
    private final AreaMeasurementUsingProbability sut;

    public static void main(String[] args) throws IOException {
        new AreaMeasurementUsingProbabilityTests().measurement_returns_expected_area();
    }

    public AreaMeasurementUsingProbabilityTests() {
        sut = new AreaMeasurementUsingProbability();
        ij = new ImageJ();
        testUtils = new TestUtils(ij);
    }

    @Test
    public void measurement_returns_expected_area() throws IOException {
        int componentIndex = 2;
        List<ComponentInterface> components = getListOfComponents();

        ComponentInterface componentToMeasure = components.get(componentIndex);
        sut.measure(new SegmentMeasurementData(componentToMeasure, components));
    }

    @Test
    public void measure__throw_exception_if_component_componentToMeasure_not_in_list_of_all_components() throws IOException {
        List<ComponentInterface> components = getListOfComponents();
        ComponentInterface componentToMeasure = components.get(4);
        components.remove(componentToMeasure);
        Exception exception = assertThrows(RuntimeException.class, () -> sut.measure(new SegmentMeasurementData(componentToMeasure, components)));

        String expectedMessage = "target component must be in list of all components";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @NotNull
    private List<ComponentInterface> getListOfComponents() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6/frame90_repeated__cropped__20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6__model_9e5727e4ed18802f4ab04c7494ef8992d798f4d64d5fd75e285b9a3d83b13ac9.tif";
        ResultTable resultTable = new ResultTable(",");
        sut.setOutputTable(resultTable);

        List<ComponentInterface> components = new ArrayList<>();
        for (int componentIndex = 0; componentIndex < 5; componentIndex++) {
            ValuePair<AdvancedComponent<FloatType>, RandomAccessibleInterval<BitType>> componentAndImage = testUtils.getComponentWithImage(imageFile,
                    componentIndex,
                    new BitType(true));
            components.add(componentAndImage.getA());
        }
        return components;
    }
}
