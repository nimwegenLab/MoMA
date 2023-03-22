package com.jug.lp;

import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

public interface IAssignmentPlausibilityTester {
    boolean assignmentIsPlausible(AdvancedComponent<FloatType> sourceComponent,
                                  List<AdvancedComponent<FloatType>> targetComponents);
}
