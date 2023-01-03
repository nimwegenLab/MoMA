package com.jug.gui;

import com.jug.Growthlane;
import com.jug.lp.AbstractAssignment;
import com.jug.lp.Hypothesis;
import com.jug.lp.MappingAssignment;
import com.jug.util.componenttree.AdvancedComponent;
import gurobi.GRBException;
import net.imglib2.type.numeric.real.FloatType;

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
        clearStartHypothesis();
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
        clearEndHypothesis();
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
        clearStartHypothesis();
        clearEndHypothesis();
        selectedHypotheses.clear();
    }

    private void clearEndHypothesis() {
        if (isNull(endHypothesis)) {
            return;
        }
        endHypothesis.deselect();
        endHypothesis = null;
    }

    private void clearStartHypothesis() {
        if (isNull(startHypothesis)) {
            return;
        }
        startHypothesis.deselect();
        startHypothesis = null;
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
            if (isNull(assignment)) { /* assignment is NULL, when user-selected start-/end-components are not connected by assignments; in this case abort action */
                assignments.clear();
                return;
            }
            assignments.add(assignment);
        }
        assignments.stream().forEach(assignment -> assignment.setGroundTruth(true));
        updateMomaState();
    }

    public void forceCurrentlyActiveAssigmentBetweenSelectedHypotheses() {
        List<AbstractAssignment> assignments = new ArrayList<>();
        for (int i = 0; i < selectedHypotheses.size() - 1; i++) {
            AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assignment = selectedHypotheses.get(i).getActiveOutgoingAssignment();
            if (isNull(assignment)) { /* assignment is NULL, when user-selected start-/end-components are not connected by assignments; in this case abort action */
                assignments.clear();
                return;
            }
            assignments.add(assignment);
        }
        assignments.stream().forEach(assignment -> assignment.setGroundTruth(true));
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
        List<AbstractAssignment> selectedAssignments = getSelectedMappingAssignments();
        selectedAssignments.stream().forEach(mappingAssignment -> mappingAssignment.setGroundTruth(false));
        updateGurobiModel();
        selectedAssignments.stream().forEach(mappingAssignment -> mappingAssignment.setGroundUntruth(false));
        updateGurobiModel();
        updateMomaState();
    }

    private List<AbstractAssignment> getSelectedMappingAssignments() {
        List<AbstractAssignment> res = new ArrayList<>();
        for (int i = 0; i < selectedHypotheses.size() - 1; i++) {
            AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assignment = selectedHypotheses.get(i).getActiveOutgoingAssignment();
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
