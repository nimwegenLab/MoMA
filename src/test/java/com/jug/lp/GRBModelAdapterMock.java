package com.jug.lp;

import com.jug.lp.GRBModel.IGRBModelAdapter;
import gurobi.*;

public class GRBModelAdapterMock implements IGRBModelAdapter {
    @Override
    public GRBVar getVarByName(String name) throws GRBException {
        return null;
    }

    @Override
    public void update() throws GRBException {

    }

    @Override
    public void remove(GRBConstr var) throws GRBException {

    }

    @Override
    public GRBConstr addConstr(GRBLinExpr lhsExpr, char sense, double rhs, String name) throws GRBException {
        return null;
    }

    @Override
    public void write(String filename) throws GRBException {

    }

    @Override
    public double get(GRB.DoubleAttr attr) throws GRBException {
        return 0;
    }

    @Override
    public int get(GRB.IntParam param) throws GRBException {
        return 0;
    }

    @Override
    public void set(GRB.IntParam param, int newval) throws GRBException {

    }

    @Override
    public GRBVar addVar(double lb, double ub, double obj, char type, String name) throws GRBException {
        return null;
    }

    @Override
    public GRBEnv getEnv() throws GRBException {
        return null;
    }

    @Override
    public void setCallback(GRBCallback cb) {

    }

    @Override
    public void optimize() throws GRBException {

    }

    @Override
    public int get(GRB.IntAttr attr) throws GRBException {
        return 0;
    }
}
