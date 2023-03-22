package com.jug.lp.costs;

import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

public class DummyMigrationCostCalculator implements ICostCalculator {
    @Override
    public double calculateCost(AdvancedComponent<FloatType> sourceComponent, List<AdvancedComponent<FloatType>> targetComponents) {
        return 0;
    }
}
