package com.jug.lp;

import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

public class AssignmentFilterUsingFluorescencePerFrame implements IAssignmentFilter {
    private final ImageProperties imageProperties;
    private double numberOfSigmas;

    int targetChannelNumber;

    public AssignmentFilterUsingFluorescencePerFrame(ImageProperties imageProperties, int channelNumber, double numberOfSigmas) {
        this.imageProperties = imageProperties;
        this.targetChannelNumber = channelNumber;
        this.numberOfSigmas = numberOfSigmas;
    }

    public void setNumberOfSigmas(double numberOfSigmas) {
        this.numberOfSigmas = numberOfSigmas;
    }

    public double getNumberOfSigmas() {
        return numberOfSigmas;
    }

    public int getTargetChannelNumber() {
        return targetChannelNumber;
    }

    public void setTargetChannelNumber(int targetChannelNumber) {
        this.targetChannelNumber = targetChannelNumber;
    }

    @Override
    public void evaluate(AbstractAssignment assignment) {
        int frame = assignment.getTargetTimeStep();
//        double intensityMean = imageProperties.getBackgroundIntensityMeanAtFrame(targetChannelNumber, frame);
//        double intensityStd = imageProperties.getBackgroundIntensityStdAtFrame(targetChannelNumber, frame);
//        double threshold = intensityMean + numberOfSigmas * intensityStd;
        Hypothesis<AdvancedComponent<FloatType>> sourceHyp = assignment.getSourceHypothesis();
        AdvancedComponent<FloatType> sourceComponent = sourceHyp.getWrappedComponent();
        double sourceComponentIntensityMean = sourceComponent.getMaskIntensityMean(targetChannelNumber);
        List<Hypothesis<AdvancedComponent<FloatType>>> targetHyps = assignment.getTargetHypotheses();
        boolean targetsAreValid = true;
        double ratio_threshold = numberOfSigmas * 0.068;
        for (Hypothesis<AdvancedComponent<FloatType>> hyp : targetHyps) {
            AdvancedComponent<FloatType> component = hyp.getWrappedComponent();
            double targetComponentIntensityMean = component.getMaskIntensityMean(targetChannelNumber);
            double intensity_ratio = targetComponentIntensityMean/sourceComponentIntensityMean - 1;
            if (intensity_ratio < -ratio_threshold || intensity_ratio > ratio_threshold) {
//            if (!(-ratio_threshold < intensity_ratio && intensity_ratio < ratio_threshold)) {
                targetsAreValid = false;
            }
        }
        if (!targetsAreValid) {
            assignment.setGroundUntruth(true);
        }
    }
}
