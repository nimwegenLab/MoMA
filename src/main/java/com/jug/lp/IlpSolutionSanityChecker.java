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

    /**
     * Return error message listing, where continuity constraints were found.
     * @return
     */
    public String getErrorMessage() {
        return finalErrorMessage.toString();
    }
    private StringBuilder finalErrorMessage;

    /**
     * Return if continuity constraint was found.
     * @return
     */
    public boolean continuityConstraintViolationFound() {
        return continuityConstraintViolationFound;
    }
    private boolean continuityConstraintViolationFound;

    void CheckSolutionContinuityConstraintForAllTimesteps() {
        finalErrorMessage = new StringBuilder();
        continuityConstraintViolationFound = false;
        finalErrorMessage.append("--------- Start: CheckSolutionContinuityConstraintForTimestepBaseOnOptimalHypotheses ---------\n");
        for (int t = 1; t < gl.numberOfFrames(); t++) { /* we start at t=1 to have incoming defined assignments from previous time step */
            continuityConstraintViolationFound = continuityConstraintViolationFound | CheckSolutionContinuityConstraintForTimestepBaseOnOptimalHypotheses(t, finalErrorMessage);
        }
        finalErrorMessage.append("--------- End: CheckSolutionContinuityConstraintForTimestepBaseOnOptimalHypotheses ---------\n");

        finalErrorMessage.append("--------- Start: CheckContinuityConstraintForAllOptimalAssignments ---------\n");
        for (int t = 1; t < gl.numberOfFrames(); t++) { /* we start at t=1 to have incoming defined assignments from previous time step */
            continuityConstraintViolationFound = continuityConstraintViolationFound | CheckContinuityConstraintForAllOptimalAssignments(t, finalErrorMessage);
        }
        finalErrorMessage.append("--------- End: CheckContinuityConstraintForAllOptimalAssignments ---------\n");
    }

    boolean CheckContinuityConstraintForAllOptimalAssignments(int t, StringBuilder errorMessageToAppendTo) {
        boolean myOptimizationFailedFlag = false;
        Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> incomingAssignments = ilp.getOptimalAssignments(t - 1);
        Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> outgoingAssignments = ilp.getOptimalAssignments(t);
        Set<ExitAssignment> incomingExitAssignments = ilp.getEdgeSets().getAssignmentsOfType(incomingAssignments, ExitAssignment.class);
        Set<LysisAssignment> incomingLysisAssignments = ilp.getEdgeSets().getAssignmentsOfType(incomingAssignments, LysisAssignment.class);
        Set<DivisionAssignment> incomingDivisionAssignments = ilp.getEdgeSets().getAssignmentsOfType(incomingAssignments, DivisionAssignment.class);
        Set<EnterAssignment> outgoingEnterAssignments = ilp.getEdgeSets().getAssignmentsOfType(outgoingAssignments, EnterAssignment.class);
        int outgoingEnterAssignmentsCount = outgoingEnterAssignments.size();
        int incomingExitCount = incomingExitAssignments.size();
        int incomingLysisCount = incomingLysisAssignments.size();
        int incomingDivisionCount = incomingDivisionAssignments.size();
        int incomingTotalCount = incomingAssignments.size();
        int outgoingTotalCount = outgoingAssignments.size();
        int outgoingCount = (outgoingTotalCount - incomingDivisionCount - outgoingEnterAssignmentsCount);
        int incomingCount = (incomingTotalCount - incomingExitCount - incomingLysisCount);
        if (outgoingCount != incomingCount) {
            errorMessageToAppendTo.append(String.format("ERROR: Continuity constraint violation at t=%d\n", t));
            errorMessageToAppendTo.append("Assignment counts:\n");
            errorMessageToAppendTo.append(String.format("total incoming assignments: %d\n", incomingTotalCount));
            errorMessageToAppendTo.append(String.format("total outgoing assignments: %d\n", outgoingTotalCount));
            errorMessageToAppendTo.append(String.format("incoming lysis assignments: %d\n", incomingLysisCount));
            errorMessageToAppendTo.append(String.format("incoming division assignments: %d\n", incomingDivisionCount));
            errorMessageToAppendTo.append(String.format("incoming exit assignments: %d\n", incomingExitCount));
            errorMessageToAppendTo.append(String.format("outgoing enter assignments: %d\n", outgoingEnterAssignmentsCount));
            myOptimizationFailedFlag = true;
        }
        return myOptimizationFailedFlag;
    }


    /**
     * This method checks the continuity constraint: outgoingAssignmentCount == incomingAssignmentsCount for each
     * time step {@param t}
     *
     * @param t time step at which the continuity constraint is checked
     */
    boolean CheckSolutionContinuityConstraintForTimestepBaseOnOptimalHypotheses(int t, StringBuilder errorMessageToAppendTo) {
        boolean myOptimizationFailedFlag = false;
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

            if (outgoingAssignmentCount != incomingAssignmentCount) {
                throw new AssertionError(String.format("ERROR: Continuity constraint violation at t=%d\n", t));
            }
            if (outgoingAssignmentCount != incomingAssignmentCount) {
                errorMessageToAppendTo.append(String.format("ERROR: Continuity constraint violation at t=%d\n", t));
                errorMessageToAppendTo.append(String.format("timestep %d:\n", t));
                errorMessageToAppendTo.append(String.format("incoming: %d\n", incomingAssignmentCount));
                errorMessageToAppendTo.append(String.format("outgoing: %d\n", outgoingAssignmentCount));
                errorMessageToAppendTo.append("There was an error ...!\n");
                myOptimizationFailedFlag = true;
            }
            return myOptimizationFailedFlag;
        } catch (GRBException e) {
            e.printStackTrace();
            return myOptimizationFailedFlag;
        }
    }
}
