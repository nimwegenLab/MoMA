package com.jug.lp;

import com.jug.lp.GRBModel.IGRBModelAdapter;
import gurobi.GRB;
import gurobi.GRBConstr;
import gurobi.GRBException;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.isNull;

public class CellCountConstraint {
    private int timeStep;
    private IGRBModelAdapter model;

    private CellCountConstraint(int timeStep, IGRBModelAdapter model) {
        this.timeStep = timeStep;
        this.model = model;
    }

    public String getName(){
        return getCellCountConstraintName(timeStep);
    }

    public void remove(){
        GRBConstr constraint = getGurobiConstraint();
        if (!isNull(getGurobiConstraint())) {
            try {
                model.remove(constraint);
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
        if (!isNull(getGurobiConstraint())) {
            try {
                return (int) constraint.get(GRB.DoubleAttr.RHS);
            } catch (GRBException e) {
                throw new RuntimeException("Error: Failed to get number of cells in CellCountConstraint: " + CellCountConstraint.getCellCountConstraintName(timeStep), e);
            }
        }
        return -1;
    }

    private GRBConstr getGurobiConstraint() {
        GRBConstr cellCountConstraint;
        try {
            cellCountConstraint = model.getConstrByName(CellCountConstraint.getCellCountConstraintName(timeStep));
            if (!isNull(cellCountConstraint)) {
                return cellCountConstraint;
            } else {
                return null;
            }
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
