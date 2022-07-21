package com.jug.lp;

import com.jug.lp.GRBModel.IGRBModelAdapter;
import com.jug.util.componenttree.AdvancedComponent;
import gurobi.GRB;
import gurobi.GRBConstr;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import net.imglib2.type.numeric.real.FloatType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

import static java.util.Objects.isNull;

public class CellCountConstraint {
    private int timeStep;
    private IGRBModelAdapter model;

    private CellCountConstraint(int timeStep, IGRBModelAdapter model) {
        this.timeStep = timeStep;
        this.model = model;
    }

    public static void addCellCountConstraint(int timeStep, int numberOfCells, IGRBModelAdapter model, AssignmentsAndHypotheses<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>, Hypothesis<AdvancedComponent<FloatType>>> nodes, HypothesisNeighborhoods<Hypothesis<AdvancedComponent<FloatType>>, AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> edgeSets) {
        final GRBLinExpr expr = new GRBLinExpr();

        final List<Hypothesis<AdvancedComponent<FloatType>>> hyps = nodes.getHypothesesAt(timeStep);
        for (final Hypothesis<AdvancedComponent<FloatType>> hyp : hyps) {
            final Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> rightNeighbors = edgeSets.getRightNeighborhood(hyp);
            for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assmnt : rightNeighbors) {
                expr.addTerm(1.0, assmnt.getGRBVar());
            }
        }

        try {
            model.addConstr(expr, GRB.EQUAL, numberOfCells, CellCountConstraint.getCellCountConstraintName(timeStep));
            model.update();
        } catch (GRBException e) {
            throw new RuntimeException("Error: Failed to add CellCountConstraint: " + getCellCountConstraintName(timeStep), e);
        }
    }

    public String getName(){
        return getCellCountConstraintName(timeStep);
    }

    public void remove(){
        GRBConstr constraint = getGurobiConstraint();
        if (!isNull(constraint)) {
            try {
                model.remove(constraint);
                model.update();
            } catch (GRBException e) {
                throw new RuntimeException("Failed to remove CellNumberConstraint: " + getCellCountConstraintName(getTimeStep()), e);
            }
        }
    }

    public int getTimeStep() {
        return timeStep;
    }

    public int getNumberOfCells() {
        GRBConstr constraint = getGurobiConstraint();
        if (!isNull(constraint)) {
            try {
                return (int) constraint.get(GRB.DoubleAttr.RHS);
            } catch (GRBException e) {
                throw new RuntimeException("Error: Failed to get number of cells in CellCountConstraint: " + CellCountConstraint.getCellCountConstraintName(timeStep), e);
            }
        }
        return -1;
    }

    private GRBConstr getGurobiConstraint() {
        try {
            return model.getConstrByName(CellCountConstraint.getCellCountConstraintName(timeStep));
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
    }

    public static CellCountConstraint getCellCountConstraint(int timeStep, IGRBModelAdapter model){
        return new CellCountConstraint(timeStep, model);
    }

    @NotNull
    public static String getCellCountConstraintName(int t) {
        return "CellCountConstraintAtT_" + t;
    }
}
