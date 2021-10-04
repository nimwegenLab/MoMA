package com.jug.lp.GRBModel;

import gurobi.GRBEnv;
import gurobi.GRBException;

public class GRBModelFactory {
    private static GRBModelAdapter model;
    private static GRBEnv env;

    public static GRBModelAdapter getModel(){
        if (model != null){
            return model;
        }
        model = getModelInstance();
        return model;
    }

    private static GRBModelAdapter getModelInstance() {
        if (env == null) {
            try {
                env = new GRBEnv("MotherMachineILPs.log");
            } catch (final GRBException e) {
                System.out.println("GrowthlaneTrackingILP::env could not be initialized!");
                e.printStackTrace();
            }
        }

        try {
            model = new GRBModelAdapter(env);
        } catch (final GRBException e) {
            System.out.println("GrowthlaneTrackingILP::model could not be initialized!");
            e.printStackTrace();
        }

        return model;
    }
}
