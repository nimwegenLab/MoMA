package com.jug.lp;

import com.jug.config.ITrackingConfiguration;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AssignmentPlausibilityTesterTest {
    @Test
    public void sizeDifferenceIsPlausible__for_growth_smaller_than_growth_rate__returns_true() {
        double previousSize = 1.;
        double newSize = 1.09;
        double maximumRelativeSizeChangeBetweenFrames = 1.1;
        AssignmentPlausibilityTester sut = new AssignmentPlausibilityTester(new TrackingConfigMock(maximumRelativeSizeChangeBetweenFrames));
        assertTrue(sut.sizeDifferenceIsPlausible(previousSize, newSize));
    }

    @Test
    public void sizeDifferenceIsPlausible__for_growth_equal_to_growth_rate__returns_true() {
        double initialSize = 1.;
        double newSize = 1.1;
        double maximumRelativeSizeChangeBetweenFrames = 1.1;
        AssignmentPlausibilityTester sut = new AssignmentPlausibilityTester(new TrackingConfigMock(maximumRelativeSizeChangeBetweenFrames));
        assertTrue(sut.sizeDifferenceIsPlausible(initialSize, newSize));
    }

    @Test
    public void sizeDifferenceIsPlausible__for_growth_larger_than_growth_rate__returns_false() {
        double initialSize = 1.;
        double newSize = 1.11;
        double maximumRelativeSizeChangeBetweenFrames = 1.1;
        AssignmentPlausibilityTester sut = new AssignmentPlausibilityTester(new TrackingConfigMock(maximumRelativeSizeChangeBetweenFrames));
        assertFalse(sut.sizeDifferenceIsPlausible(initialSize, newSize));
    }

    class TrackingConfigMock implements ITrackingConfiguration {
        private double maximumGrowthRate;

        public TrackingConfigMock(double maximumGrowthRate) {

            this.maximumGrowthRate = maximumGrowthRate;
        }

        @Override
        public double getMaximumGrowthRate() {
            return maximumGrowthRate;
        }
    }
}