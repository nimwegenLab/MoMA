package com.jug.lp;

import com.jug.GrowthLine;
import gurobi.GRBException;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.numeric.real.FloatType;

import java.util.ArrayList;
import java.util.List;

public class IlpSolutionSanityChecker {
    private GrowthLineTrackingILP ilp;
    private GrowthLine gl;

    public IlpSolutionSanityChecker(GrowthLineTrackingILP ilp,
                                    GrowthLine gl) {
        this.ilp = ilp;
        this.gl = gl;
    }

    void CheckSolutionContinuityConstraintForAllTimesteps() {
        System.out.println("--------- Start: CheckSolutionContinuityConstraintForAllTimesteps ---------");
        for (int t = 1; t < gl.size(); t++) {
            CheckSolutionContinuityConstraintForTimestepBaseOnOptimalHypotheses(t);
        }
        System.out.println("--------- End: CheckSolutionContinuityConstraintForAllTimesteps ---------");
    }

    /**
     * This method checks the continuity constraint: outgoingAssignmentCount == incomingAssignmentsCount for each
     * time step {@param t}
     *
     * @param t time step at which the continuity constraint is checked
     */
    void CheckSolutionContinuityConstraintForTimestepBaseOnOptimalHypotheses(int t) {
        try{
            List<Hypothesis<Component<FloatType, ?>>> currentOptimalHypotheses = ilp.getOptimalSegmentation(t);
            List<AbstractAssignment<Hypothesis<Component<FloatType, ?>>>> leftSidedAssignments = new ArrayList<>();
            List<AbstractAssignment<Hypothesis<Component<FloatType, ?>>>> rightSidedAssignments = new ArrayList<>();
            for (Hypothesis<Component<FloatType, ?>> hyp : currentOptimalHypotheses) {
                leftSidedAssignments.add(ilp.getOptimalLeftAssignment(hyp));
                rightSidedAssignments.add(ilp.getOptimalRightAssignment(hyp));
            }
            int incomingAssignmentsCount = leftSidedAssignments.size();
            int outgoingAssignmentCount = rightSidedAssignments.size();

            System.out.println(String.format("timestep %d:", t));
            System.out.println(String.format("incoming: %d", incomingAssignmentsCount));
            System.out.println(String.format("outgoing: %d", outgoingAssignmentCount));
            assert (outgoingAssignmentCount == incomingAssignmentsCount) :
                    String.format("ERROR: Continuity constraint violation at t=%d", t);
        }
        catch (GRBException e) {
            e.printStackTrace();
        }
    }
}
