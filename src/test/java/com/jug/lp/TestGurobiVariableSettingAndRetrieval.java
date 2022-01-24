package com.jug.lp;

//import com.jug.lp.GRBModel.GRBModelAdapter;
import gurobi.*;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBVar;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestGurobiVariableSettingAndRetrieval {
    @Test
    public void testSaveAndLoadOfVariableByName() throws GRBException {
        GRBEnv env = new GRBEnv("MotherMachineILPs.log");
//        env.start();
        GRBModel model = new GRBModel(env);
        String name = "some_variable_name";
        String var1name = name + "1";
        String var2name = name + "2";
        GRBVar var1 = model.addVar(0.0, 1.0, 1.0, GRB.BINARY, var1name);
        GRBVar var2 = model.addVar(0.0, 1.0, 1.0, GRB.BINARY, var2name);
        model.update();
//        GRBVar[] vars = model.getVars();
//        System.out.println("vars.length: " + vars.length);
//        GRBVar res = vars[0];
        GRBVar var1_retrieved = model.getVarByName(var1name);
        Assert.assertTrue(var1.sameAs(var1_retrieved));

        GRBVar var2_retrieved = model.getVarByName(var2name);
        Assert.assertTrue(var2.sameAs(var2_retrieved));

        Assert.assertFalse(var1.sameAs(var2_retrieved));
        System.out.println("stop");
//        String.format("a_%d^LYSIS--%d", t, hyp.getId())
    }

    @Test
    public void testSaveAndLoadVariable_1() throws IOException, GRBException {
        Path path = Files.createTempDirectory("java-");

        /* create and save model */
        String modelFilePath = path.toAbsolutePath() + "/model.lp";
        GRBEnv env = new GRBEnv(path.toAbsolutePath() + "/ilp_orig.log");
//        env.start();
        GRBModel model = new GRBModel(env);
        String name = "some_variable_name";
        String var1name = name + "1";
        String var2name = name + "2";
        GRBVar var1 = model.addVar(0.0, 1.0, 1.0, GRB.BINARY, var1name);
        GRBVar var2 = model.addVar(0.0, 1.0, 1.0, GRB.BINARY, var2name);
        model.update();
        model.write(modelFilePath);

        /* load model and retrieve variables */
        GRBEnv envLoaded = new GRBEnv(path.toAbsolutePath() + "/ilp_loaded.log");
//        env.start();
        GRBModel modelLoaded = new GRBModel(envLoaded, modelFilePath);
//        GRBVar[] vars = model.getVars();
//        System.out.println("vars.length: " + vars.length);
//        GRBVar res = vars[0];
        GRBVar var1_retrieved = modelLoaded.getVarByName(var1name);
//        var1_retrieved.get("name")
//        Assert.assertTrue(var1.sameAs(var1_retrieved));

        GRBVar var2_retrieved = modelLoaded.getVarByName(var2name);
//        Assert.assertTrue(var2.sameAs(var2_retrieved));

//        Assert.assertFalse(var1.sameAs(var2_retrieved));
        System.out.println("stop");
//        String.format("a_%d^LYSIS--%d", t, hyp.getId())

    }
}
