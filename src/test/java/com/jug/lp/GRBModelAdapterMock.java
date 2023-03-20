package com.jug.lp;

import com.jug.lp.GRBModel.IGRBModelAdapter;
import gurobi.*;

import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GRBModelAdapterMock implements IGRBModelAdapter {
    @Override
    public GRBConstr[] getConstrs() {
        return new GRBConstr[0];
    }

    @Override
    public GRBVar[] getVars() {
        return new GRBVar[0];
    }

    @Override
    public GRBConstr getConstrByName(String name) throws GRBException {
        return null;
    }

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
    public void read(String filename) throws GRBException {

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
        GRBVar varMock = mock(GRBVar.class);
        when(varMock.toString()).thenReturn(name);
        when(varMock.get(GRB.StringAttr.VarName)).thenReturn(name);
        return varMock;
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

    @Override
    public Set<GRBVar> getVariablesContaining(String string) {
        return null;
    }

    @Override
    public Set<GRBConstr> getConstraintsContaining(String string) {
        return null;
    }
}
