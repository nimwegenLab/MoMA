package com.jug.lp;

import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

public class AssignmentFluorescenceFilter implements IAssignmentFilter {
    private double threshold = 0.0;
    int targetChannelNumber = 0;

    public void setFluorescenceThreshold(double threshold) {
        this.threshold = threshold;
    }

    public double getFluorescenceThreshold(){
        return threshold;
    }

    @Override
    public void evaluate(AbstractAssignment assignment) {
        List<Hypothesis<AdvancedComponent<FloatType>>> targetHyps = assignment.getTargetHypotheses();
        boolean targetsAreValid = true;
        for(Hypothesis<AdvancedComponent<FloatType>> hyp : targetHyps){
            AdvancedComponent<FloatType> component = hyp.getWrappedComponent();
            double maskIntensityMean = component.getMeanMaskIntensity(targetChannelNumber);
            if(maskIntensityMean < threshold){
                targetsAreValid = false;
            }
        }
        if(!targetsAreValid){
            assignment.setGroundUntruth(true);
        }
    }

    public int getTargetChannelNumber() {
        return targetChannelNumber;
    }

    public void setTargetChannelNumber(int targetChannelNumber) {
        this.targetChannelNumber = targetChannelNumber;
    }
}
