package com.jug.lp;

import com.jug.mocks.MockUtils;
import com.jug.util.TestUtils;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class AssignmentFluorescenceFilterTest {
    private final TestUtils testUtils;

    public AssignmentFluorescenceFilterTest() {
        testUtils = new TestUtils();
    }

    @Test
    public void evaluate__when_all_component_intensities_are_above_threshold__does_not_call_setGroundUntruth(){
        AssignmentFluorescenceFilter sut = new AssignmentFluorescenceFilter();
        int targetChannelNumber = 4;
        double intensityThreshold = 1000.0;
        sut.setTargetChannelNumber(targetChannelNumber);
        sut.setFluorescenceThreshold(intensityThreshold);
        AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assignmentMock = MockUtils.getAssignmentMock(2);
        when(assignmentMock.getTargetComponent(0).getMaskIntensity(targetChannelNumber)).thenReturn(intensityThreshold + 1);
        when(assignmentMock.getTargetComponent(1).getMaskIntensity(targetChannelNumber)).thenReturn(intensityThreshold + 1);

        sut.evaluate(assignmentMock);

        verify(assignmentMock, never()).setGroundUntruth(true);
    }

    @Test
    public void evaluate__when_any_component_intensity_is_below_threshold__calls_setGroundUntruth(){
        AssignmentFluorescenceFilter sut = new AssignmentFluorescenceFilter();
        int targetChannelNumber = 4;
        double intensityThreshold = 1000.0;
        sut.setFluorescenceThreshold(intensityThreshold);
        AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assignmentMock = MockUtils.getAssignmentMock(2);
        when(assignmentMock.getTargetComponent(0).getMaskIntensity(targetChannelNumber)).thenReturn(intensityThreshold + 1);
        when(assignmentMock.getTargetComponent(1).getMaskIntensity(targetChannelNumber)).thenReturn(intensityThreshold - 1);

        sut.evaluate(assignmentMock);

        verify(assignmentMock).setGroundUntruth(true);
    }

    @Test
    public void evaluate__when_all_component_intensity_are_below_threshold__calls_setGroundUntruth(){
        AssignmentFluorescenceFilter sut = new AssignmentFluorescenceFilter();
        int targetChannelNumber = 4;
        double intensityThreshold = 1000.0;
        sut.setFluorescenceThreshold(intensityThreshold);
        AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assignmentMock = MockUtils.getAssignmentMock(2);
        when(assignmentMock.getTargetComponent(0).getMaskIntensity(targetChannelNumber)).thenReturn(intensityThreshold - 1);
        when(assignmentMock.getTargetComponent(1).getMaskIntensity(targetChannelNumber)).thenReturn(intensityThreshold - 1);

        sut.evaluate(assignmentMock);

        verify(assignmentMock).setGroundUntruth(true);
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
