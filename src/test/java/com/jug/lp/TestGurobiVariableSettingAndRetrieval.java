package com.jug.lp;

//import com.jug.lp.GRBModel.GRBModelAdapter;

import gurobi.*;
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
//        System.out.println("stop");
//        String.format("a_%d^LYSIS--%d", t, hyp.getId())
    }

    @Test
    public void testSaveAndLoadVariable_1() throws IOException, GRBException {
        Path pathToTempDir = Files.createTempDirectory("java-");

        /* create and save model */
        System.out.println("Temporary Directory: " + pathToTempDir.toAbsolutePath());
        String modelFilePath = pathToTempDir.toAbsolutePath() + "/model.lp";
        GRBEnv env = new GRBEnv(pathToTempDir.toAbsolutePath() + "/ilp_orig.log");
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
        GRBEnv envLoaded = new GRBEnv(pathToTempDir.toAbsolutePath() + "/ilp_loaded.log");
//        env.start();
        GRBModel modelLoaded = new GRBModel(envLoaded, modelFilePath);
//        GRBVar[] vars = model.getVars();
//        System.out.println("vars.length: " + vars.length);
//        GRBVar res = vars[0];
//        GRBVar var1_retrieved = modelLoaded.getVarByName(var1name);
//        var1_retrieved.get("name")
//        Assert.assertTrue(var1.sameAs(var1_retrieved));

        GRBVar var1_retrieved = modelLoaded.getVarByName(var1name);
        String var1name_retrieved = var1_retrieved.get(GRB.StringAttr.VarName);
        Assert.assertTrue(var1name.contentEquals(var1name_retrieved)); /* assert names are equal */
        Assert.assertFalse(var1.sameAs(var1_retrieved)); /* assert we are not testing the same variable */

        GRBVar var2_retrieved = modelLoaded.getVarByName(var2name);
        String var2name_retrieved = var2_retrieved.get(GRB.StringAttr.VarName);
        Assert.assertTrue(var2name.contentEquals(var2name_retrieved)); /* assert names are equal */
        Assert.assertFalse(var2.sameAs(var2_retrieved)); /* assert we are not testing the same variable */

        GRBVar[] vars = modelLoaded.getVars();
        for (int ind = 0; ind < vars.length; ind++) {
            GRBVar var = vars[ind];
            System.out.println("VarName: " + var.get(GRB.StringAttr.VarName));
        }

//        Assert.assertFalse(var1.sameAs(var2_retrieved));
//        System.out.println("stop");
//        String.format("a_%d^LYSIS--%d", t, hyp.getId())

    }

    @Test
    public void loadAndPrintIlpFromMoma() throws IOException, GRBException {
        Path pathToTempDir = Files.createTempDirectory("java-");

//        /* create and save model */
//        System.out.println("Temporary Directory: " + pathToTempDir.toAbsolutePath());
//        String modelFilePath = pathToTempDir.toAbsolutePath() + "/model.lp";
//        GRBEnv env = new GRBEnv(pathToTempDir.toAbsolutePath() + "/ilp_orig.log");
////        env.start();
//        GRBModel model = new GRBModel(env);
//        String name = "some_variable_name";
//        String var1name = name + "1";
//        String var2name = name + "2";
//        GRBVar var1 = model.addVar(0.0, 1.0, 1.0, GRB.BINARY, var1name);
//        GRBVar var2 = model.addVar(0.0, 1.0, 1.0, GRB.BINARY, var2name);
//        model.update();
//        model.write(modelFilePath);

//        String modelFilePath = "/media/micha/T7/20210816_test_data_michael/Moma/MM_Testing/000_moma_benchmarking/other_test_data/dany_20200730__Pos3_GL16/output/ilpModel.lp";
        String modelFilePath = "/media/micha/T7/20210816_test_data_michael/Moma/MM_Testing/000_moma_benchmarking/other_test_data/dany_20200730__Pos3_GL16/output/ilpModel.mps";

        /* load model and retrieve variables */
        GRBEnv envLoaded = new GRBEnv(pathToTempDir.toAbsolutePath() + "/ilp_loaded.log");
//        env.start();
        GRBModel modelLoaded = new GRBModel(envLoaded, modelFilePath);
//        GRBVar[] vars = model.getVars();
//        System.out.println("vars.length: " + vars.length);
//        GRBVar res = vars[0];
//        GRBVar var1_retrieved = modelLoaded.getVarByName(var1name);
//        var1_retrieved.get("name")
//        Assert.assertTrue(var1.sameAs(var1_retrieved));

//        GRBVar var1_retrieved = modelLoaded.getVarByName(var1name);
//        String var1name_retrieved = var1_retrieved.get(GRB.StringAttr.VarName);
//        Assert.assertTrue(var1name.contentEquals(var1name_retrieved)); /* assert names are equal */
//        Assert.assertFalse(var1.sameAs(var1_retrieved)); /* assert we are not testing the same variable */
//
//        GRBVar var2_retrieved = modelLoaded.getVarByName(var2name);
//        String var2name_retrieved = var2_retrieved.get(GRB.StringAttr.VarName);
//        Assert.assertTrue(var2name.contentEquals(var2name_retrieved)); /* assert names are equal */
//        Assert.assertFalse(var2.sameAs(var2_retrieved)); /* assert we are not testing the same variable */

        GRBVar[] vars = modelLoaded.getVars();
        for (int ind = 0; ind < vars.length; ind++) {
            GRBVar var = vars[ind];
            System.out.println("VarName: " + var.get(GRB.StringAttr.VarName));
        }

//        Assert.assertFalse(var1.sameAs(var2_retrieved));
//        System.out.println("stop");
//        String.format("a_%d^LYSIS--%d", t, hyp.getId())

    }
}
