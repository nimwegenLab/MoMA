package com.jug.lp.costs;

import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;

public interface IAssignmentCostCalculator {
    double calculateMappingCost(AdvancedComponent<FloatType> sourceComponent,
                                AdvancedComponent<FloatType> targetComponent);

    double calculateDivisionCost(AdvancedComponent<FloatType> sourceComponent,
                                 AdvancedComponent<FloatType> lowerTargetComponent,
                                 AdvancedComponent<FloatType> upperTargetComponent);

    double calculateExitCost(AdvancedComponent<FloatType> sourceComponent);

    double calculateLysisCost(AdvancedComponent<FloatType> sourceComponent);

    double calculateEnterCost(AdvancedComponent<FloatType> targetComponent);
}
