package com.jug.lp;

import org.junit.Test;
import static org.junit.Assert.*;

public class AssignmentPlausibilityTesterTest {
    double tol = 1e-5;

    @Test
    public void constructor__sets_maximumRelativeSizeChangeBetweenFrames_correctly(){
        double expected = 1.1;
        AssignmentPlausibilityTester sut = new AssignmentPlausibilityTester(expected);
        assertEquals(expected, sut.getMaximumRelativeSizeChangeBetweenFrames(), tol);
    }

    @Test
    public void maximumRelativeSizeChangeBetweenFrames__getter_and_setter_behave_correctly(){
        double previousSize = 1.0;
        double expected = 1.2;
        AssignmentPlausibilityTester sut = new AssignmentPlausibilityTester(previousSize);
        sut.setMaximumRelativeSizeChangeBetweenFrames(expected);
        assertEquals(expected, sut.getMaximumRelativeSizeChangeBetweenFrames(), tol);
    }

    @Test
    public void sizeDifferenceIsPlausible__for_growth_smaller_than_growth_rate__returns_true() {
        double previousSize = 1.;
        double newSize = 1.09;
        double maximumRelativeSizeChangeBetweenFrames = 1.1;
        AssignmentPlausibilityTester sut = new AssignmentPlausibilityTester(maximumRelativeSizeChangeBetweenFrames);
        assertTrue(sut.sizeDifferenceIsPlausible(previousSize, newSize));
    }

    @Test
    public void sizeDifferenceIsPlausible__for_growth_equal_to_growth_rate__returns_true() {
        double initialSize = 1.;
        double newSize = 1.1;
        double maximumRelativeSizeChangeBetweenFrames = 1.1;
        AssignmentPlausibilityTester sut = new AssignmentPlausibilityTester(maximumRelativeSizeChangeBetweenFrames);
        assertTrue(sut.sizeDifferenceIsPlausible(initialSize, newSize));
    }

    @Test
    public void sizeDifferenceIsPlausible__for_growth_larger_than_growth_rate__returns_false() {
        double initialSize = 1.;
        double newSize = 1.11;
        double maximumRelativeSizeChangeBetweenFrames = 1.1;
        AssignmentPlausibilityTester sut = new AssignmentPlausibilityTester(maximumRelativeSizeChangeBetweenFrames);
        assertFalse(sut.sizeDifferenceIsPlausible(initialSize, newSize));
    }
}