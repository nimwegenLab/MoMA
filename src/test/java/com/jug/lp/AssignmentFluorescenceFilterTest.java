package com.jug.lp;

import com.jug.datahandling.IImageProvider;
import com.jug.mocks.MockUtils;
import com.jug.util.TestUtils;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class AssignmentFluorescenceFilterTest {
    private final TestUtils testUtils;

    public AssignmentFluorescenceFilterTest() {
        testUtils = new TestUtils();
    }

    public static void main(String[] args) throws IOException {
        AssignmentFluorescenceFilterTest tests = new AssignmentFluorescenceFilterTest();
        tests.test1();
    }

    @Test
    public void test1() throws IOException {
        Path testDataFolder = testUtils.getAbsolutTestFilePath("src/test/resources/00_probability_maps/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12/frames_445-460__20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12");
        IImageProvider imageProvider = testUtils.getImageProviderFromDataFolder(testDataFolder);
        List<Img<FloatType>> imgs = imageProvider.getRawChannelImgs();
        testUtils.show(imgs.get(0));
        System.out.println("stop");

        AssignmentFluorescenceFilter sut = new AssignmentFluorescenceFilter();
    }

    @Test
    public void evaluate__for_target_component_with_low_intensity__filters_the_assignment(){
        AssignmentFluorescenceFilter sut = new AssignmentFluorescenceFilter();
        int targetChannelNumber = 4;

        AbstractAssignment assignmentMock = mock(AbstractAssignment.class);

        Hypothesis hypothesis = mock(Hypothesis.class);
        AdvancedComponent componentMock = mock(AdvancedComponent.class);

        when(componentMock.getMaskIntensity(1)).thenReturn(1000.0);
        when(hypothesis.getWrappedComponent()).thenReturn(componentMock);

        List<Hypothesis> list = new ArrayList<>();
        list.add(hypothesis);
        when(assignmentMock.getTargetHypotheses()).thenReturn(list);

        sut.evaluate(assignmentMock);

        verify(componentMock).getMaskIntensity(targetChannelNumber);
    }

    @Test
    public void evaluate__after_using_setTargetChannelNumber__calls_getMaskIntensity_on_components_with_correct_channel(){
        AssignmentFluorescenceFilter sut = new AssignmentFluorescenceFilter();

        int expectedTargetChannelNumber = 4;
        sut.setTargetChannelNumber(expectedTargetChannelNumber);

        AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assignmentMock = MockUtils.getAssignmentMock(2);

        sut.evaluate(assignmentMock);

        verify(assignmentMock.getTargetComponent(0)).getMaskIntensity(expectedTargetChannelNumber);
        verify(assignmentMock.getTargetComponent(1)).getMaskIntensity(expectedTargetChannelNumber);
    }

    @Test
    public void getTargetChannelNumber__after_using_setTargetChannelNumber__returns_set_value() {
        AssignmentFluorescenceFilter sut = new AssignmentFluorescenceFilter();
        sut.setTargetChannelNumber(1);
        Assert.assertEquals(1, sut.getTargetChannelNumber());
    }

    @Test
    public void getTargetChannelNumber__after_instantiation__returns_zero() {
        AssignmentFluorescenceFilter sut = new AssignmentFluorescenceFilter();
        Assert.assertEquals(0, sut.getTargetChannelNumber());
    }

    @Test
    public void getFluorescenceThreshold__after_instantiation__returns_zero() {
        AssignmentFluorescenceFilter sut = new AssignmentFluorescenceFilter();
        Assert.assertEquals(0.0, sut.getFluorescenceThreshold(), 1e-6);
    }

    @Test
    public void getFluorescenceThreshold__after_using_setFluorescenceThreshold__returns_set_value() {
        AssignmentFluorescenceFilter sut = new AssignmentFluorescenceFilter();
        double expected = 1.234;
        sut.setFluorescenceThreshold(expected);
        Assert.assertEquals(expected, sut.getFluorescenceThreshold(), 1e-6);
    }
}
