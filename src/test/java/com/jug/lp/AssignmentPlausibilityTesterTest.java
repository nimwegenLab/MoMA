package com.jug.lp;

import org.junit.Test;
import static org.junit.Assert.*;

public class AssignmentPlausibilityTesterTest {

    @Test
    public void sizeDifferenceIsPlausible__for_smaller_than_valid_target_size__returns_true() {
        int sourceSize = 10;
        int totalTargetSize = 19;
        int doublingTime = 1;
        AssignmentPlausibilityTester sut = new AssignmentPlausibilityTester();
        sut.setShortestDoublingTimeInFrames(doublingTime);
        assertTrue(sut.sizeDifferenceIsPlausible(sourceSize, totalTargetSize));
    }

    @Test
    public void sizeDifferenceIsPlausible__for_equal_to_valid_target_size__returns_true() {
        int sourceSize = 10;
        int totalTargetSize = 20;
        int doublingTime = 1;
        AssignmentPlausibilityTester sut = new AssignmentPlausibilityTester();
        sut.setShortestDoublingTimeInFrames(doublingTime);
        assertTrue(sut.sizeDifferenceIsPlausible(sourceSize, totalTargetSize));
    }

    @Test
    public void sizeDifferenceIsPlausible__for_valid_target_size__returns_false() {
        int sourceSize = 10;
        int totalTargetSize = 21;
        int doublingTime = 1;
        AssignmentPlausibilityTester sut = new AssignmentPlausibilityTester();
        sut.setShortestDoublingTimeInFrames(doublingTime);
        assertFalse(sut.sizeDifferenceIsPlausible(sourceSize, totalTargetSize));
    }

    @Test
    public void getShortestDoublingTimeInFrames__returns_expected_value() {
        double expected = 0.0346;
        int doublingTime = 20;
        AssignmentPlausibilityTester sut = new AssignmentPlausibilityTester();
        sut.setShortestDoublingTimeInFrames(doublingTime);
        assertEquals(expected, sut.getGrowthRateInFrames(), 1e-3);
    }

    @Test
    public void setShortestDoublingTimeInFrames__sets_correct_growth_rate() {
        double expected = 0.0346;
        int doublingTime = 20;
        AssignmentPlausibilityTester sut = new AssignmentPlausibilityTester();
        sut.setShortestDoublingTimeInFrames(doublingTime);
        assertEquals(expected, sut.getGrowthRateInFrames(), 1e-3);
    }
}