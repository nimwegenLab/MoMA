package com.jug.gui;

import com.jug.Growthlane;
import com.jug.lp.Hypothesis;
import com.jug.lp.MappingAssignment;
import gurobi.GRBException;
import org.apache.commons.lang.NotImplementedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.isNull;

public class HypothesisRangeSelector {
    private Hypothesis<?> startHypothesis;
    private Hypothesis<?> endHypothesis;
    private final Growthlane growthlane;

    public HypothesisRangeSelector(Growthlane growthlane) {
        this.growthlane = growthlane;
    }

    public void setStartHypothesis(Hypothesis<?> hypothesis) {
        this.startHypothesis = hypothesis;
        if (isNull(startHypothesis) || isNull(endHypothesis)) {
            return;
        }
        calculateSelectedHypotheses();
    }

    public void setEndHypothesis(Hypothesis<?> hypothesis) {
        this.endHypothesis = hypothesis;
        if (isNull(startHypothesis) || isNull(endHypothesis)) {
            return;
        }
        updatedSelectedHypotheses();
    }

    private void updatedSelectedHypotheses() {
        calculateSelectedHypotheses();
        highlightSelectedHypotheses();
    }

    private void highlightSelectedHypotheses() {
        for (Hypothesis<?> hyp : selectedHypotheses) {
            hyp.selected();
        }
    }

    List<Hypothesis<?>> selectedHypotheses = new ArrayList<>();

    public void clearSelectedHypotheses() {
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
        try {
            growthlane.getIlp().model.update();
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
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
        growthlane.getIlp().run();
    }

    public void clearUserConstraints() {
        selectedHypotheses.stream().forEach(hypothesis -> hypothesis.setIsForced(false));
        updateGurobiModel();
        selectedHypotheses.stream().forEach(hypothesis -> hypothesis.setIsForceIgnored(false));
        updateGurobiModel();
        List<MappingAssignment> mappingAssignments = getSelectedMappingAssignments();
        mappingAssignments.stream().forEach(mappingAssignment -> mappingAssignment.setGroundTruth(false));
        updateGurobiModel();
        mappingAssignments.stream().forEach(mappingAssignment -> mappingAssignment.setGroundUntruth(false));
        updateGurobiModel();
        updateMomaState();
    }

    private List<MappingAssignment> getSelectedMappingAssignments() {
        List<MappingAssignment> res = new ArrayList<>();
        for (int i = 0; i < selectedHypotheses.size() - 1; i++) {
            MappingAssignment assignment = selectedHypotheses.get(i).getRightAssignmentWithTarget(MappingAssignment.class, selectedHypotheses.get(i + 1));
            if (isNull(assignment)) {
                continue;
            }
            res.add(assignment);
        }
        return res;
    }

    private void updateGurobiModel() {
        try {
            growthlane.getIlp().model.update();
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
    }
}
