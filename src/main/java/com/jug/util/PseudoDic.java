package com.jug.util;

import com.jug.lp.AssignmentPlausibilityTester;

/**
 * This is pseudo dependency injection container, which I use to work on getting my class dependencies and initialization
 * in order. Ideally at some point, this will be replaced with a true dependency injection frame-work.
 */
public class PseudoDic {
    private final AssignmentPlausibilityTester assignmentPlausibilityTester;

    public PseudoDic(){
        assignmentPlausibilityTester = new AssignmentPlausibilityTester();
    }

    public AssignmentPlausibilityTester getAssignmentPlausibilityTester() {
        return assignmentPlausibilityTester;
    }
}
