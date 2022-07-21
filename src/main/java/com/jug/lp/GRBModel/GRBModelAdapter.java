package com.jug.lp.GRBModel;

import gurobi.*;

import java.util.HashMap;
import java.util.HashSet;

public class GRBModelAdapter implements IGRBModelAdapter {
    private gurobi.GRBModel model;

    @Override
    public GRBConstr getConstrByName(String name) throws GRBException {
        return this.model.getConstrByName(name);
    }

    @Override
    public GRBVar getVarByName(String name) throws GRBException {
        return this.model.getVarByName(name);
    }

    public GRBModelAdapter(gurobi.GRBModel model) {
        this.model = model;
    }

    public GRBModelAdapter(GRBEnv env) throws GRBException {
        this.model = new gurobi.GRBModel(env);
    }

    @Override
    public void update() throws GRBException {
        model.update();
    }

    HashMap<GRBConstr, String> constraintNames = new HashMap<>();

    @Override
    public GRBConstr addConstr(GRBLinExpr lhsExpr, char sense, double rhs, String name) throws GRBException {
        if (constraintNames.containsValue(name)) {
            throw new RuntimeException("gurobi constraint already exists: " + name);
        }
        GRBConstr res = null;
        try {
            res = model.getConstrByName(name);
        } catch (GRBException err) {
//            throw new RuntimeException("Failed while adding Gurobi constraint: " + name);
        }
        if(res == null){
            res = model.addConstr(lhsExpr, sense, rhs, name);
        }
        constraintNames.put(res, name);
        return res;
    }

    @Override
    public void remove(GRBConstr var) throws GRBException {
        model.remove(var);
        constraintNames.remove(var);
    }

    @Override
    public void read(String filename) throws GRBException {
        model.read(filename);
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

    HashSet<String> variableNames = new HashSet<>();

    @Override
    public GRBVar addVar(double lb, double ub, double obj, char type, String name) throws GRBException {
        if(variableNames.contains(name)){
            throw new RuntimeException("gurobi variable already exists: " + name);
        }
        variableNames.add(name);
        GRBVar res = null;
        try {
            res = model.getVarByName(name);
        } catch (GRBException err) {
//            System.out.println("Error reading requested variable.");
        }
        if(res == null){
            res = model.addVar(lb, ub, obj, type, name);
        }
        return res;
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
//        model.update();
//        String basePath = "/media/micha/T7/data_michael_mell/moma_test_data/000_development/feature/20220121-fix-loading-of-curated-datasets/dany_20200730__Pos3_GL16/output/";
////        String basePath = "/media/micha/T7/20210816_test_data_michael/Moma/MM_Testing/000_development/feature/20220121-fix-loading-of-curated-datasets/lis_20211026__Pos7_GL12/output/";
//        model.read(basePath + "/ilpModel.sol");
        model.update();
        model.optimize();
    }

    @Override
    public int get(GRB.IntAttr attr) throws GRBException {
        return model.get(attr);
    }
}
