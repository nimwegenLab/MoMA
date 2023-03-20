package com.jug.lp;

import com.jug.config.IFluorescenceAssignmentFilterConfiguration;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

public class AssignmentFilterUsingFluorescencePerFrame implements IAssignmentFilter {
    private IFluorescenceAssignmentFilterConfiguration configuration;

    public AssignmentFilterUsingFluorescencePerFrame(IFluorescenceAssignmentFilterConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void evaluate(AbstractAssignment assignment) {
        if(assignment.isExitAssignment() | assignment.isEnterAssignment()) {
            return; /* comparing fluorescence intensities between source and target hypotheses is not possible for exit and enter assignments */
        }
        Hypothesis<AdvancedComponent<FloatType>> sourceHyp = assignment.getSourceHypothesis();
        AdvancedComponent<FloatType> sourceComponent = sourceHyp.getWrappedComponent();
        double sourceComponentIntensityMean = sourceComponent.getMaskIntensityMean(configuration.getFluorescenceAssignmentFilterChannel());
        List<Hypothesis<AdvancedComponent<FloatType>>> targetHyps = assignment.getTargetHypotheses();
        boolean targetsAreValid = true;
        for (Hypothesis<AdvancedComponent<FloatType>> targetHypothesis : targetHyps) {
            AdvancedComponent<FloatType> targetComponent = targetHypothesis.getWrappedComponent();
            double targetComponentIntensityMean = targetComponent.getMaskIntensityMean(configuration.getFluorescenceAssignmentFilterChannel());
            double intensity_ratio = targetComponentIntensityMean/sourceComponentIntensityMean;
            if (intensity_ratio < configuration.getFluorescenceAssignmentFilterIntensityRatioThresholdLower() || intensity_ratio > configuration.getFluorescenceAssignmentFilterIntensityRatioThresholdUpper()) {
                targetsAreValid = false;
            }
        }
        if (!targetsAreValid) {
            assignment.setGroundUntruth(true);
        }
    }
}
