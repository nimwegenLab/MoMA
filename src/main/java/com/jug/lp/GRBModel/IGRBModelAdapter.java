package com.jug.lp.GRBModel;

import gurobi.*;

public interface IGRBModelAdapter {
    GRBConstr getConstrByName(String name) throws GRBException;

    GRBVar getVarByName(String name) throws GRBException;

    void update() throws GRBException;

    void remove(GRBConstr var) throws GRBException;

    GRBConstr addConstr(GRBLinExpr lhsExpr, char sense, double rhs, String name) throws GRBException;

//    GRBConstr getConstrByName(String name);

    void write(String filename) throws GRBException;

    void read(String filename) throws GRBException;

    double get(GRB.DoubleAttr attr) throws GRBException;

    int get(GRB.IntParam param) throws GRBException;

    void set(GRB.IntParam param, int newval) throws GRBException;

    GRBVar addVar(double lb, double ub, double obj, char type, String name) throws GRBException; /* here I could make a method addOrGetVar, which first tries to get the variable and if not existent, adds the variable  */

    GRBEnv getEnv() throws GRBException;

    void setCallback(GRBCallback cb);

    void optimize() throws GRBException;

    int get(GRB.IntAttr attr) throws GRBException;
}
