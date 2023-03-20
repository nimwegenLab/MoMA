package com.jug.lp;

import com.jug.config.ConfigurationManager;
import com.jug.util.ComponentTreeUtils;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

public class AssignmentPlausibilityTesterLegacyPositionMismatch implements IAssignmentPlausibilityTester {
    private final ConfigurationManager configurationManager;

    public AssignmentPlausibilityTesterLegacyPositionMismatch(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    @Override
    public boolean assignmentIsPlausible(AdvancedComponent<FloatType> sourceComponent,
                                         List<AdvancedComponent<FloatType>> targetComponents) {
        AdvancedComponent<FloatType> lowerTargetComponent = targetComponents.get(0);
        return !ComponentTreeUtils.isBelowByMoreThen(sourceComponent, lowerTargetComponent, configurationManager.getMaxCellDrop());
    }
}
