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
        for (int t = 1; t < gl.size(); t++) {
            CheckSolutionContinuityConstraintForTimestep(t);
        }
    }

    /**
     * This method check whether: `#allPreviousOutgoingAssignments-#allPreviousExitAssignment == #currentIncomingAssignments`
     *
     * @param t
     */
    void CheckSolutionContinuityConstraintForTimestep(int t) {
        try{
            List<Hypothesis<Component<FloatType, ?>>> currentOptimalHypotheses = ilp.getOptimalSegmentation(t);
            List<AbstractAssignment<Hypothesis<Component<FloatType, ?>>>> currentLeftSidedAssignments = new ArrayList<>();
            for (Hypothesis<Component<FloatType, ?>> hyp : currentOptimalHypotheses) {
                currentLeftSidedAssignments.add(ilp.getOptimalLeftAssignment(hyp));
            }

            List<Hypothesis<Component<FloatType, ?>>> previousOptimalHypotheses = ilp.getOptimalSegmentation(t-1);
            List<AbstractAssignment<Hypothesis<Component<FloatType, ?>>>> previousRightHandedAssignments = new ArrayList<>();
            for (Hypothesis<Component<FloatType, ?>> hyp : previousOptimalHypotheses) {
                previousRightHandedAssignments.add(ilp.getOptimalRightAssignment(hyp));
            }

            int previousOutgoingAssignmentCount = previousRightHandedAssignments.size();
            int previousExitAssignmentCount = new HypothesisNeighborhoods().getAssignmentsOfType(previousRightHandedAssignments, ExitAssignment.class).size();
            int currentIncomingAssignmentCount = currentLeftSidedAssignments.size();

            assert (currentIncomingAssignmentCount == (previousOutgoingAssignmentCount - previousExitAssignmentCount));
        }
        catch (GRBException e) {
            e.printStackTrace();
        }
    }

//    int AssignmentTypeCount(List<AbstractAssignment<?>> assignments, Class<T> assignmentType){
//        for(AbstractAssignment<?> ass : assignments){
//            if()
//        }
//    }
}
