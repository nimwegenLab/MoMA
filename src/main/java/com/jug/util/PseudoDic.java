package com.jug.util;

import com.jug.config.ConfigurationManager;
import com.jug.lp.AssignmentPlausibilityTester;

/**
 * This is pseudo dependency injection container, which I use to work on getting my class dependencies and initialization
 * in order. Ideally at some point, this will be replaced with a true dependency injection frame-work.
 */
public class PseudoDic {
    private final AssignmentPlausibilityTester assignmentPlausibilityTester;

    public PseudoDic(ConfigurationManager configurationManager){
        assignmentPlausibilityTester = new AssignmentPlausibilityTester(configurationManager.SHORTEST_DOUBLING_TIME_IN_FRAMES);
    }

    public AssignmentPlausibilityTester getAssignmentPlausibilityTester() {
        return assignmentPlausibilityTester;
    }
}
