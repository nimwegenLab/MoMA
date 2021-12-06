package com.jug.lp;

import com.jug.Growthlane;
import com.jug.util.componenttree.AdvancedComponent;
import gurobi.GRBException;
import net.imglib2.type.numeric.real.FloatType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class IlpSolutionSanityChecker {
    private final GrowthlaneTrackingILP ilp;
    private final Growthlane gl;

    public IlpSolutionSanityChecker(GrowthlaneTrackingILP ilp,
                                    Growthlane gl) {
        this.ilp = ilp;
        this.gl = gl;
    }

    private StringBuilder finalErrorMessage;
    private boolean optimizationFailedFlag;

    void CheckSolutionContinuityConstraintForAllTimesteps() {
        finalErrorMessage = new StringBuilder();
        optimizationFailedFlag = false;
        finalErrorMessage.append("--------- Start: CheckSolutionContinuityConstraintForTimestepBaseOnOptimalHypotheses ---------\n");
        for (int t = 1; t < gl.size(); t++) { /* we start at t=1 to have incoming defined assignments from previous time step */
            CheckSolutionContinuityConstraintForTimestepBaseOnOptimalHypotheses(t, finalErrorMessage);
        }
        finalErrorMessage.append("--------- End: CheckSolutionContinuityConstraintForTimestepBaseOnOptimalHypotheses ---------\n");

        finalErrorMessage.append("--------- Start: CheckContinuityConstraintForAllOptimalAssignments ---------\n");
        for (int t = 1; t < gl.size(); t++) { /* we start at t=1 to have incoming defined assignments from previous time step */
            CheckContinuityConstraintForAllOptimalAssignments(t, finalErrorMessage);
        }
        finalErrorMessage.append("--------- End: CheckContinuityConstraintForAllOptimalAssignments ---------\n");
    }

    void CheckContinuityConstraintForAllOptimalAssignments(int t, StringBuilder errorMessageToAppendTo) {
        Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> incomingAssignments = ilp.getOptimalAssignments(t - 1);
        Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> outgoingAssignments = ilp.getOptimalAssignments(t);
        Set<ExitAssignment> incomingExitAssignments = ilp.getEdgeSets().getAssignmentsOfType(incomingAssignments, ExitAssignment.class);
        Set<LysisAssignment> incomingLysisAssignments = ilp.getEdgeSets().getAssignmentsOfType(incomingAssignments, LysisAssignment.class);
        Set<DivisionAssignment> incomingDivisionAssignments = ilp.getEdgeSets().getAssignmentsOfType(incomingAssignments, DivisionAssignment.class);
        int incomingExitCount = incomingExitAssignments.size();
        int incomingLysisCount = incomingLysisAssignments.size();
        int incomingDivisionCount = incomingDivisionAssignments.size();
        int incomingTotalCount = incomingAssignments.size();
        int outgoingTotalCount = outgoingAssignments.size();
        if (outgoingTotalCount - incomingDivisionCount != (incomingTotalCount - incomingExitCount - incomingLysisCount)) {
            errorMessageToAppendTo.append(String.format("ERROR: Continuity constraint violation at t=%d\n", t));
            errorMessageToAppendTo.append(String.format("incoming total: %d\n", incomingTotalCount));
            errorMessageToAppendTo.append(String.format("outgoing total: %d\n", outgoingTotalCount));
            errorMessageToAppendTo.append(String.format("incoming exit: %d\n", incomingExitCount));
            errorMessageToAppendTo.append(String.format("incoming lysis: %d\n", incomingLysisCount));
            errorMessageToAppendTo.append(String.format("incoming division: %d\n", incomingDivisionCount));
        }
    }


    /**
     * This method checks the continuity constraint: outgoingAssignmentCount == incomingAssignmentsCount for each
     * time step {@param t}
     *
     * @param t time step at which the continuity constraint is checked
     */
    void CheckSolutionContinuityConstraintForTimestepBaseOnOptimalHypotheses(int t, StringBuilder errorMessageToAppendTo) {
        try {
            List<Hypothesis<AdvancedComponent<FloatType>>> currentOptimalHypotheses = ilp.getOptimalSegmentation(t);
            List<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> incomingAssignments = new ArrayList<>();
            List<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> outgoingAssignments = new ArrayList<>();
            for (Hypothesis<AdvancedComponent<FloatType>> hyp : currentOptimalHypotheses) {
                incomingAssignments.add(ilp.getOptimalLeftAssignment(hyp));
                outgoingAssignments.add(ilp.getOptimalRightAssignment(hyp));
            }
            int incomingAssignmentCount = incomingAssignments.size();
            int outgoingAssignmentCount = outgoingAssignments.size();

            assert (outgoingAssignmentCount != incomingAssignmentCount) :
                    String.format("ERROR: Continuity constraint violation at t=%d\n", t);
            if (outgoingAssignmentCount != incomingAssignmentCount) {
                errorMessageToAppendTo.append(String.format("ERROR: Continuity constraint violation at t=%d\n", t));
                errorMessageToAppendTo.append(String.format("timestep %d:\n", t));
                errorMessageToAppendTo.append(String.format("incoming: %d\n", incomingAssignmentCount));
                errorMessageToAppendTo.append(String.format("outgoing: %d\n", outgoingAssignmentCount));
                errorMessageToAppendTo.append("There was an error ...!\n");
            }
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

    public String getFinalErrorMessage() {
        return finalErrorMessage.toString();
    }
}
