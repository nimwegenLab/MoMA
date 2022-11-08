package com.jug.lp;

import com.jug.config.ConfigurationManager;
import com.jug.config.IConfiguration;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;
import org.apache.commons.lang.NotImplementedException;

import java.util.List;

public class AssignmentFluorescenceFilter {
    private IConfiguration configurationManager;
    private double threshold = 0.0;
    int targetChannelNumber = 0;

    public void setFluorescenceThreshold(double threshold) {
        this.threshold = threshold;
    }

    public double getFluorescenceThreshold(){
        return threshold;
    }

    public void evaluate(AbstractAssignment assignment) {
        List<Hypothesis<AdvancedComponent<FloatType>>> targetHyps = assignment.getTargetHypotheses();
        boolean targetsAreValid = false;
        for(Hypothesis<AdvancedComponent<FloatType>> hyp : targetHyps){
            AdvancedComponent<FloatType> component = hyp.getWrappedComponent();
            if(component.getMaskIntensity(targetChannelNumber) > threshold){
                targetsAreValid = true;
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
