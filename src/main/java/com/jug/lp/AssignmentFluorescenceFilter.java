package com.jug.lp;

import com.jug.config.ConfigurationManager;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

public class AssignmentFluorescenceFilter {
    private ConfigurationManager configurationManager;

    public AssignmentFluorescenceFilter(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public void evaluate(AbstractAssignment assignment) {
        if (!configurationManager.getFilterAssignmentsUsingFluorescenceFeatureFlag()) {
            return;
        }
        List<Hypothesis<AdvancedComponent<FloatType>>> targetHyps = assignment.getTargetHypotheses();
        boolean targetsAreValid = false;
        for(Hypothesis<AdvancedComponent<FloatType>> hyp : targetHyps){
            AdvancedComponent<FloatType> component = hyp.getWrappedComponent();
//            component.get
        }
    }
}
