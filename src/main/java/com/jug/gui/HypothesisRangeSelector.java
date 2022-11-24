package com.jug.gui;

import com.jug.lp.Hypothesis;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;
import org.apache.commons.lang.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public class HypothesisRangeSelector {
    private Hypothesis<?> startHypothesis;
    private Hypothesis<?> endHypothesis;

    public HypothesisRangeSelector() {
    }

    public void setStartHypothesis(Hypothesis<?> hypothesis) {
        this.startHypothesis = hypothesis;
//        calculateSelectedHypotheses();
    }

    public void setEndHypothesis(Hypothesis<?> hypothesis) {
        this.endHypothesis = hypothesis;
        calculateSelectedHypotheses();
        highlightSelectedHypotheses();
    }

    private void highlightSelectedHypotheses() {
        for (Hypothesis<?> hyp : selectedHypotheses) {
            hyp.selected();
        }
    }

    List<Hypothesis<?>> selectedHypotheses = new ArrayList<>();

    private void clearSelectedHypotheses() {
        for (Hypothesis<?> hyp : selectedHypotheses) {
            hyp.deselected();
        }
        selectedHypotheses.clear();
    }

    private void calculateSelectedHypotheses() {
        clearSelectedHypotheses();
        selectedHypotheses.add(endHypothesis);
        Hypothesis<?> currentHyp = endHypothesis;
        while (currentHyp != null) {
            currentHyp = currentHyp.getSourceHypothesis();
            selectedHypotheses.add(currentHyp);
            if (currentHyp == startHypothesis) {
                break;
            }
        }
        if (!selectedHypotheses.contains(startHypothesis)) {
            clearSelectedHypotheses();
            throw new NotImplementedException("implement displaying a dialog that the starting-hypothesis was not found.");
        }
    }
}
