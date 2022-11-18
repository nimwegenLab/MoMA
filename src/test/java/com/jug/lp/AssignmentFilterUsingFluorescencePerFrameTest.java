package com.jug.lp;

import com.jug.mocks.MockUtils;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

class AssignmentFilterUsingFluorescencePerFrameTest {
    private AssignmentFilterUsingFluorescencePerFrame sut;
    private double defaultNumberOfSigmas = 1.0;
    private int defaultTargetChannel = 1;
    private ImageProperties imageProperties;

    @BeforeEach
    void setUp() {
        imageProperties = mock(ImageProperties.class);
        sut = new AssignmentFilterUsingFluorescencePerFrame(imageProperties, defaultTargetChannel, defaultNumberOfSigmas);
    }

    @Test
    public void evaluate__when_all_component_intensities_are_above_threshold__does_not_call_setGroundUntruth(){
        int targetChannelNumber = 4;
        int targetFrame = 0;
        double intensityThreshold = 1000.0;
        sut.setTargetChannelNumber(targetChannelNumber);
        when(imageProperties.getBackgroundIntensityMeanAtFrame(targetChannelNumber, targetFrame)).thenReturn(intensityThreshold);
        when(imageProperties.getBackgroundIntensityStdAtFrame(targetChannelNumber, targetFrame)).thenReturn(0.0);
        AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assignmentMock = MockUtils.getAssignmentMock(2);
        when(assignmentMock.getTargetComponent(0).getMeanMaskIntensity(targetChannelNumber)).thenReturn(intensityThreshold + 1);
        when(assignmentMock.getTargetComponent(1).getMeanMaskIntensity(targetChannelNumber)).thenReturn(intensityThreshold + 1);
        when(assignmentMock.getTargetComponent(0).getFrameNumber()).thenReturn(targetFrame);
        when(assignmentMock.getTargetComponent(1).getFrameNumber()).thenReturn(targetFrame);

        sut.evaluate(assignmentMock);

        verify(assignmentMock, never()).setGroundUntruth(true);
    }

    @Test
    public void evaluate__when_any_component_intensity_is_below_threshold__calls_setGroundUntruth(){
        int targetChannelNumber = 4;
        int targetFrame = 0;
        double intensityThreshold = 1000.0;
        sut.setTargetChannelNumber(targetChannelNumber);
        when(imageProperties.getBackgroundIntensityMeanAtFrame(targetChannelNumber, targetFrame)).thenReturn(intensityThreshold);
        when(imageProperties.getBackgroundIntensityStdAtFrame(targetChannelNumber, targetFrame)).thenReturn(0.0);
        AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assignmentMock = MockUtils.getAssignmentMock(2);
        when(assignmentMock.getTargetComponent(0).getMeanMaskIntensity(targetChannelNumber)).thenReturn(intensityThreshold + 1);
        when(assignmentMock.getTargetComponent(1).getMeanMaskIntensity(targetChannelNumber)).thenReturn(intensityThreshold - 1);
        when(assignmentMock.getTargetComponent(0).getFrameNumber()).thenReturn(targetFrame);
        when(assignmentMock.getTargetComponent(1).getFrameNumber()).thenReturn(targetFrame);

        sut.evaluate(assignmentMock);

        verify(assignmentMock).setGroundUntruth(true);
    }

    @Test
    public void evaluate__when_all_component_intensity_are_below_threshold__calls_setGroundUntruth(){
        int targetChannelNumber = 4;
        int targetFrame = 0;
        double intensityThreshold = 1000.0;

        sut.setTargetChannelNumber(targetChannelNumber);
        when(imageProperties.getBackgroundIntensityMeanAtFrame(targetChannelNumber, targetFrame)).thenReturn(intensityThreshold);
        when(imageProperties.getBackgroundIntensityStdAtFrame(targetChannelNumber, targetFrame)).thenReturn(0.0);

        AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assignmentMock = MockUtils.getAssignmentMock(2);
        when(assignmentMock.getTargetComponent(0).getMeanMaskIntensity(targetChannelNumber)).thenReturn(intensityThreshold - 1);
        when(assignmentMock.getTargetComponent(1).getMeanMaskIntensity(targetChannelNumber)).thenReturn(intensityThreshold - 1);
        when(assignmentMock.getTargetComponent(0).getFrameNumber()).thenReturn(targetFrame);
        when(assignmentMock.getTargetComponent(1).getFrameNumber()).thenReturn(targetFrame);

        sut.evaluate(assignmentMock);

        verify(assignmentMock).setGroundUntruth(true);
    }

    @Test
    public void evaluate__after_using_setTargetChannelNumber__calls_getMeanMaskIntensity_on_components_with_correct_channel(){
        int expectedTargetChannelNumber = 4;
        sut.setTargetChannelNumber(expectedTargetChannelNumber);

        AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assignmentMock = MockUtils.getAssignmentMock(2);

        sut.evaluate(assignmentMock);

        verify(assignmentMock.getTargetComponent(0)).getMeanMaskIntensity(expectedTargetChannelNumber);
        verify(assignmentMock.getTargetComponent(1)).getMeanMaskIntensity(expectedTargetChannelNumber);
    }

    @Test
    public void getTargetChannelNumber__after_using_setTargetChannelNumber__returns_set_value() {
        sut.setTargetChannelNumber(1);
        Assertions.assertEquals(1, sut.getTargetChannelNumber());
    }

    @Test
    public void getTargetChannelNumber__after_instantiation__returns_zero() {
        Assertions.assertEquals(defaultTargetChannel, sut.getTargetChannelNumber());
    }

    @Test
    public void getNumberOfSigmas__after_instantiation__returns_zero() {
        Assertions.assertEquals(defaultNumberOfSigmas, sut.getNumberOfSigmas(), 1e-6);
    }

    @Test
    public void getNumberOfSigmas__after_using_setFluorescenceThreshold__returns_set_value() {
        double expected = 1.234;
        sut.setNumberOfSigmas(expected);
        Assertions.assertEquals(expected, sut.getNumberOfSigmas(), 1e-6);
    }
}