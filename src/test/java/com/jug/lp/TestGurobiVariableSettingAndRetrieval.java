package com.jug.lp;

import gurobi.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestGurobiVariableSettingAndRetrieval {
    @Test
    public void test__getting_constraint_by_name__throws_exception_when_not_updating_model() throws GRBException {
        GRBEnv env = new GRBEnv("MotherMachineILPs.log");
        GRBModel model = new GRBModel(env);
        String name = "some_constraint_name";

        GRBLinExpr expr = new GRBLinExpr();
        model.addConstr(expr, GRB.EQUAL, 1, name);

        Exception exception = Assert.assertThrows(gurobi.GRBException.class, ()->model.getConstrByName(name));
        Assert.assertEquals("No constraint names available to index", exception.getMessage());
    }

    @Test
    public void test__getting_variable_name__throws_exception_when_not_updating_model() throws GRBException {
        GRBEnv env = new GRBEnv("MotherMachineILPs.log");
        GRBModel model = new GRBModel(env);
        String name = "some_variable_name";
        String varName = name + "1";

        GRBVar var1 = model.addVar(0.0, 1.0, 1.0, GRB.BINARY, varName);

        Exception exception = Assert.assertThrows(gurobi.GRBException.class, ()->var1.get(GRB.StringAttr.VarName));
        Assert.assertEquals("Error at GRBVar.get", exception.getMessage());
    }

    @Test
    public void test__getting_variable_name__works_when_not_updating_model() throws GRBException {
        GRBEnv env = new GRBEnv("MotherMachineILPs.log");
        GRBModel model = new GRBModel(env);
        String name = "some_variable_name";
        String varName = name + "1";
        GRBVar var1 = model.addVar(0.0, 1.0, 1.0, GRB.BINARY, varName);

        model.update();

        String varNameRecovered = var1.get(GRB.StringAttr.VarName);
        Assert.assertEquals(varName, varNameRecovered);
    }

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

        String modelFilePath = new File("").getAbsolutePath() + "/src/test/resources/gurobi_api_test_data/test_data_1/ilpModel.mps";

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

    @Test
    public void loadModelAndSolutionFromMoma() throws IOException, GRBException {
        Path pathToTempDir = Files.createTempDirectory("java-");
        String modelFilePath = new File("").getAbsolutePath() + "/src/test/resources/gurobi_api_test_data/test_data_1/";
        GRBEnv envLoaded = new GRBEnv(pathToTempDir.toAbsolutePath() + "/ilp_loaded.log");
        GRBModel modelLoaded = new GRBModel(envLoaded, modelFilePath + "/ilpModel.mps");
        modelLoaded.update();
        modelLoaded.read(modelFilePath + "/ilpModel.sol");
        modelLoaded.update();
        int optimstatus = modelLoaded.get(GRB.IntAttr.Status); /* see https://www.gurobi.com/documentation/9.1/refman/status.html#attr:Status AND https://www.gurobi.com/documentation/9.1/refman/optimization_status_codes.html#sec:StatusCodes */
        GRBVar[] vars = modelLoaded.getVars();
//        modelLoaded.sync();
//        double[] res = modelLoaded.get(GRB.DoubleAttr.X, vars);
//        String[] res = modelLoaded.get(GRB.StringAttr.VarName, vars);
        for (int ind = 0; ind < vars.length; ind++) {
            GRBVar var = vars[ind];
            System.out.println("VarName: " + var.get(GRB.StringAttr.VarName));
            System.out.println("GRB.DoubleAttr.X: " + var.get(GRB.DoubleAttr.X));
        }
    }
}
