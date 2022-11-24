package com.jug.gui;

import com.jug.Growthlane;
import com.jug.lp.Hypothesis;
import com.jug.lp.MappingAssignment;
import org.apache.commons.lang.NotImplementedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HypothesisRangeSelector {
    private Hypothesis<?> startHypothesis;
    private Hypothesis<?> endHypothesis;
    private Growthlane growthlane;

    public HypothesisRangeSelector(Growthlane growthlane) {
        this.growthlane = growthlane;
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
        Collections.reverse(selectedHypotheses); /* reverse order so that the assignment with lowest time-steps is first and with highest time-step is last element */
    }

    public void forceIgnoreSelectedHypotheses() {
        selectedHypotheses.stream().forEach(hyp -> hyp.setIsForceIgnored(true));
        updateMomaState();
    }

    public void forceMappingAssigmentBetweenSelectedHypotheses() {
        for (int i = 0; i < selectedHypotheses.size() - 1; i++) {
            MappingAssignment assignment = selectedHypotheses.get(i).getRightAssignmentWithTarget(MappingAssignment.class, selectedHypotheses.get(i + 1));
            assignment.setGroundTruth(true);
        }
        updateMomaState();
    }

    private void updateMomaState() {
        clearSelectedHypotheses();
        growthlane.getIlp().run();
    }
}
