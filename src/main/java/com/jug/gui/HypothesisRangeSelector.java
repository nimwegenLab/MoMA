package com.jug.gui;

import com.jug.lp.Hypothesis;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;

public class HypothesisRangeSelector {
    private Hypothesis<AdvancedComponent<FloatType>> startHypothesis;
    private Hypothesis<AdvancedComponent<FloatType>> endHypothesis;

    public HypothesisRangeSelector() {
    }

    public void setStartHypothesis(Hypothesis<AdvancedComponent<FloatType>> hypothesis) {
        this.startHypothesis = hypothesis;
    }

    public void setEndHypothesis(Hypothesis<AdvancedComponent<FloatType>> hypothesis) {
        this.endHypothesis = hypothesis;
    }
}
