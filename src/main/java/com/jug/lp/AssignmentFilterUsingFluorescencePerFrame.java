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
        int frame = assignment.getTargetComponent(0).getFrameNumber();
        double intensityMean = imageProperties.getBackgroundIntensityMeanAtFrame(targetChannelNumber, frame);
        double intensityStd = imageProperties.getBackgroundIntensityStdAtFrame(targetChannelNumber, frame);
        double threshold = intensityMean + numberOfSigmas * intensityStd;
        List<Hypothesis<AdvancedComponent<FloatType>>> targetHyps = assignment.getTargetHypotheses();
        boolean targetsAreValid = true;
        for (Hypothesis<AdvancedComponent<FloatType>> hyp : targetHyps) {
            AdvancedComponent<FloatType> component = hyp.getWrappedComponent();
            double maskIntensityMean = component.getMaskIntensityMean(targetChannelNumber);
            if (maskIntensityMean < threshold) {
                targetsAreValid = false;
            }
        }
        if (!targetsAreValid) {
            assignment.setGroundUntruth(true);
        }
    }
}
