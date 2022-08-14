package com.jug.exploration;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;

public class ExplorationForImprovingCostFunctions {
//    String datasets_base_path = "/media/micha/T7/data_michael_mell/moma_test_data/";
    String datasets_base_path = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development/exploration/";

    public static void main(String[] args) {
        ExplorationForImprovingCostFunctions tests = new ExplorationForImprovingCostFunctions();

        tests._20220812_test_using_cell_area_below_component_for_migration_cost___lis__20211026__Pos7_GL12();
//        tests._20220812_test_using_cell_area_below_component_for_migration_cost___lis__20220701__Pos1_GL2();
//        tests._lis_20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12();
//        tests._dany_20200812_8proms_ace_1_MMStack_Pos25_GL7();
    }

    public void _20220812_test_using_cell_area_below_component_for_migration_cost___lis__20211026__Pos7_GL12() {
        String datasetSubPath = "20220812-test-using-cell-area-below-component-for-migration-cost/lis__20211026__Pos7_GL12";
        Path inputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos7_GL12", "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
        Path outputPath = Paths.get(datasets_base_path, datasetSubPath, "output");
        if (!outputPath.toFile().exists()) {
            outputPath.toFile().mkdir();
        }
        Path mmPropertiesPath = Paths.get(datasets_base_path, datasetSubPath, "mm.properties");
        Integer tmin = 400;
        Integer tmax = 450;
        startMoma(false, inputPath.toString(), outputPath.toString(), tmin, tmax, true, new String[]{"-p",mmPropertiesPath.toString()});
    }

    public void _20220812_test_using_cell_area_below_component_for_migration_cost___lis__20220701__Pos1_GL2() {
        String datasetSubPath = "20220812-test-using-cell-area-below-component-for-migration-cost/lis__20220701__Pos1_GL2";
        Path inputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos1_GL2", "20220701_VNG1040_AB2h_4_MMStack_Pos0_Pos1_GL2.tif");
        Path outputPath = Paths.get(datasets_base_path, datasetSubPath, "output");
        if (!outputPath.toFile().exists()) {
            outputPath.toFile().mkdir();
        }
        Path mmPropertiesPath = Paths.get(datasets_base_path, datasetSubPath, "mm.properties");
        Integer tmin = null;
        Integer tmax = null;
        startMoma(false, inputPath.toString(), outputPath.toString(), tmin, tmax, true, new String[]{"-p",mmPropertiesPath.toString()});
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
        Integer tmin = 0;
        Integer tmax = 100;
        startMoma(false, inputPath, outputPath, tmin, tmax, false, new String[]{"-ground_truth_export"});
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