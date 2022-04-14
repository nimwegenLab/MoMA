package com.jug;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

import static com.jug.util.JavaUtils.concatenateWithCollection;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;

public class ExplorationForImprovingCostFunctions {
//    String datasets_base_path = "/media/micha/T7/data_michael_mell/moma_test_data/";
    String datasets_base_path = "/media/micha/T7/data_michael_mell/moma_test_data/20220209_cost_function_improvements/";

    public static void main(String[] args) {
        ExplorationForImprovingCostFunctions tests = new ExplorationForImprovingCostFunctions();

        tests._lis_20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12();
//        tests._dany_20200812_8proms_ace_1_MMStack_Pos25_GL7();
    }

    public void _lis_20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12() {
        String datasetSubPath = "/lis_20211026/Pos7_GL12";
        String inputPath = datasets_base_path + datasetSubPath + "/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif";
        String outputPath = datasets_base_path + datasetSubPath + "/output/";
//        Integer tmin = 370;
//        Integer tmax = 380;
//        Integer tmin = 0;
//        Integer tmax = 10;
//        Integer tmin = 0;
//        Integer tmax = 440;
        Integer tmin = 300;
        Integer tmax = 400;
        startMoma(false, inputPath, outputPath, tmin, tmax, true, new String[]{"-ground_truth_export"});
    }


    public void _dany_20200812_8proms_ace_1_MMStack_Pos25_GL7() {
        String datasetSubPath = "/dany_20200812_8proms_ace_1_MMStack/Pos25_GL7/";
        String inputPath = datasets_base_path + datasetSubPath + "/20200812_8proms_ace_1_MMStack_Pos25_GL7.tif";
        String outputPath = datasets_base_path + datasetSubPath + "/output/";
        Integer tmin = 0;
        Integer tmax = null;
        startMoma(false, inputPath, outputPath, tmin, tmax, true, new String[]{"-ground_truth_export"});
    }
}