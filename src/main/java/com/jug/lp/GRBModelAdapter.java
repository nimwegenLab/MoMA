package com.jug.lp;

import gurobi.*;

public class GRBModelAdapter implements IGRBModelAdapter {
    private gurobi.GRBModel model;
    public GRBModelAdapter(GRBEnv env) throws GRBException {
        this.model = new gurobi.GRBModel(env);
    }

    @Override
    public void update() throws GRBException {
        model.update();
    }

    @Override
    public void remove(GRBConstr var) throws GRBException {
        model.remove(var);
    }

    @Override
    public GRBConstr addConstr(GRBLinExpr lhsExpr, char sense, double rhs, String name) throws GRBException {
        return model.addConstr(lhsExpr, sense, rhs, name);
    }

    @Override
    public void write(String filename) throws GRBException {
        model.write(filename);
    }

    @Override
    public double get(GRB.DoubleAttr attr) throws GRBException {
        return model.get(attr);
    }

    @Override
    public int get(GRB.IntParam param) throws GRBException {
        return model.get(param);
    }

    @Override
    public void set(GRB.IntParam param, int newval) throws GRBException {
        model.set(param, newval);
    }

    @Override
    public GRBVar addVar(double lb, double ub, double obj, char type, String name) throws GRBException {
        return model.addVar(lb, ub, obj, type, name);
    }

    @Override
    public GRBEnv getEnv() throws GRBException {
        return model.getEnv();
    }

    @Override
    public void setCallback(GRBCallback cb) {
        model.setCallback(cb);
    }

    @Override
    public void optimize() throws GRBException {
        model.optimize();
    }

    @Override
    public int get(GRB.IntAttr attr) throws GRBException {
        return model.get(attr);
    }
}
