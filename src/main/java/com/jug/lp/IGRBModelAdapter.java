package com.jug.lp;

import gurobi.*;

public interface IGRBModelAdapter {
    void update() throws GRBException;

    void remove(GRBConstr var) throws GRBException;

    GRBConstr addConstr(GRBLinExpr lhsExpr, char sense, double rhs, String name) throws GRBException;

    void write(String filename) throws GRBException;

    double get(GRB.DoubleAttr attr) throws GRBException;

    int get(GRB.IntParam param) throws GRBException;

    void set(GRB.IntParam param, int newval) throws GRBException;

    GRBVar addVar(double lb, double ub, double obj, char type, String name) throws GRBException;

    GRBEnv getEnv() throws GRBException;

    void setCallback(GRBCallback cb);

    void optimize() throws GRBException;

    int get(GRB.IntAttr attr) throws GRBException;
}
