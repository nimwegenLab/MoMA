package com.jug.lp;

import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

public class AssignmentFilterUsingFluorescencePerFrame implements IAssignmentFilter {
    private final ImageProperties imageProperties;
    private double intensityRatioThresholdLower;
    private double intensityRatioThresholdUpper;

    int targetChannelNumber;

    public AssignmentFilterUsingFluorescencePerFrame(ImageProperties imageProperties, int channelNumber, double intensityRatioThresholdLower, double intensityRatioThresholdUpper) {
        this.imageProperties = imageProperties;
        this.targetChannelNumber = channelNumber;
        this.intensityRatioThresholdLower = intensityRatioThresholdLower;
        this.intensityRatioThresholdUpper = intensityRatioThresholdUpper;
    }

    public void setIntensityRatioThresholdUpper(double intensityRatioThresholdUpper) {
        this.intensityRatioThresholdUpper = intensityRatioThresholdUpper;
    }

    public double getIntensityRatioThresholdUpper() {
        return intensityRatioThresholdUpper;
    }

    public int getTargetChannelNumber() {
        return targetChannelNumber;
    }

    public void setTargetChannelNumber(int targetChannelNumber) {
        this.targetChannelNumber = targetChannelNumber;
    }

    @Override
    public void evaluate(AbstractAssignment assignment) {
        Hypothesis<AdvancedComponent<FloatType>> sourceHyp = assignment.getSourceHypothesis();
        AdvancedComponent<FloatType> sourceComponent = sourceHyp.getWrappedComponent();
        double sourceComponentIntensityMean = sourceComponent.getMaskIntensityMean(targetChannelNumber);
        List<Hypothesis<AdvancedComponent<FloatType>>> targetHyps = assignment.getTargetHypotheses();
        boolean targetsAreValid = true;
        for (Hypothesis<AdvancedComponent<FloatType>> targetHypothesis : targetHyps) {
            AdvancedComponent<FloatType> targetComponent = targetHypothesis.getWrappedComponent();
            double targetComponentIntensityMean = targetComponent.getMaskIntensityMean(targetChannelNumber);
            double intensity_ratio = targetComponentIntensityMean/sourceComponentIntensityMean - 1;
            if (intensity_ratio < intensityRatioThresholdLower || intensity_ratio > intensityRatioThresholdUpper) {
                targetsAreValid = false;
            }
        }
        if (!targetsAreValid) {
            assignment.setGroundUntruth(true);
        }
    }
}
