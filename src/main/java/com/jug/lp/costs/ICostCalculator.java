package com.jug.lp.costs;

import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

public interface ICostCalculator {
    /**
     * Calculate the migration cost for a given source-components and one or more target-components.
     * @{link targetComponents} must be sorted so that the first component in the list is the lowest component the last
     * component is the highest in the growthlane.
     *
     * @param sourceComponent
     * @param targetComponents
     * @return
     */
    double calculateCost(AdvancedComponent<FloatType> sourceComponent,
                         List<AdvancedComponent<FloatType>> targetComponents);
}