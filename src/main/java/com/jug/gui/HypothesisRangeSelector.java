package com.jug.gui;

import com.jug.Growthlane;
import com.jug.lp.Hypothesis;
import com.jug.lp.MappingAssignment;
import gurobi.GRBException;

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
        if (isNull(hypothesis)) {
            throw new RuntimeException("hypothesis is null.");
        }
        if (!isNull(startHypothesis) && !hypothesis.equals(startHypothesis)) {
            startHypothesis.deselect();
        }
        startHypothesis = hypothesis;
        startHypothesis.select();
        if (isNull(startHypothesis) || isNull(endHypothesis)) {
            return;
        }
        updatedSelectedHypotheses();
    }

    public void setEndHypothesis(Hypothesis<?> hypothesis) {
        if (isNull(hypothesis)) {
            throw new RuntimeException("hypothesis is null.");
        }
        if (!isNull(endHypothesis) && !hypothesis.equals(endHypothesis)) {
            endHypothesis.deselect();
        }
        endHypothesis = hypothesis;
        endHypothesis.select();
        if (isNull(startHypothesis) || isNull(endHypothesis)) {
            return;
        }
        updatedSelectedHypotheses();
    }

    private void updatedSelectedHypotheses() {
        deselectHypotheses();
        startHypothesis.select();
        endHypothesis.select();
        calculateSelectedHypotheses();
        selectedHypotheses();
    }

    private void deselectHypotheses() {
        for (Hypothesis<?> hyp : selectedHypotheses) {
            hyp.deselect();
        }
    }

    private void selectedHypotheses() {
        for (Hypothesis<?> hyp : selectedHypotheses) {
            hyp.select();
        }
    }

    List<Hypothesis<?>> selectedHypotheses = new ArrayList<>();

    public void clearSelectedHypotheses() {
        for (Hypothesis<?> hyp : selectedHypotheses) {
            hyp.deselect();
        }
        selectedHypotheses.clear();
        startHypothesis.deselect();
        startHypothesis = null;
        endHypothesis.deselect();
        endHypothesis = null;
    }

    private void calculateSelectedHypotheses() {
        selectedHypotheses.clear();
        selectedHypotheses.add(endHypothesis);
        Hypothesis<?> currentHyp = endHypothesis;
        while (currentHyp != null) {
            currentHyp = currentHyp.getSourceHypothesis();
            if (isNull(currentHyp)) {
                break;
            }
            selectedHypotheses.add(currentHyp);
            if (currentHyp == startHypothesis) {
                break;
            }
        }
        if (!selectedHypotheses.contains(startHypothesis)) {
            selectedHypotheses.clear();
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
        List<MappingAssignment> assignments = new ArrayList<>();
        for (int i = 0; i < selectedHypotheses.size() - 1; i++) {
            MappingAssignment assignment = selectedHypotheses.get(i).getRightAssignmentWithTarget(MappingAssignment.class, selectedHypotheses.get(i + 1));
            if (isNull(assignment)) {
                assignments.clear();
                return;
            }
        }
        assignments.stream().forEach(assignment -> assignment.setGroundUntruth(true));
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
