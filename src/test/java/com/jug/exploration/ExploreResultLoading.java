package com.jug.exploration;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;

public class ExploreResultLoading {
    String datasets_base_path = "/media/micha/T7/data_michael_mell/moma_test_data/000_development/feature/20220121-fix-loading-of-curated-datasets/";

    public static void main(String[] args) {
        ExploreResultLoading tests = new ExploreResultLoading();
        tests._dany_20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16__test_reloading();
//        tests._dany_20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16();
//        tests._20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12();
//        tests._20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12__test_reloading();
    }

    public void _dany_20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16__test_reloading() {
        String inputPath = datasets_base_path + "/dany_20200730__Pos3_GL16/20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16.tif";
        String outputPath = datasets_base_path + "/dany_20200730__Pos3_GL16/output";
        String settings_file_path = datasets_base_path + "/dany_20200730__Pos3_GL16/output/mm.properties";
        String reload_folder_path = datasets_base_path + "/dany_20200730__Pos3_GL16/output";
        Integer tmin = null;
        Integer tmax = null;
//        inputPath = null;
//        outputPath = null;
//        startMoma(true, inputPath, outputPath, tmin, tmax, false, new String[]{"-ground_truth_export", "-p", settings_file_path});
//        startMoma(false, inputPath, outputPath, tmin, tmax, true, new String[]{"-ground_truth_export", "-p", settings_file_path});
        startMoma(false, null, null, null, null, false, new String[]{"-ground_truth_export", "-reload", reload_folder_path});
    }


    public void _dany_20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16() {
        String inputPath = datasets_base_path + "/dany_20200730__Pos3_GL16/20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16.tif";
        String outputPath = datasets_base_path + "/dany_20200730__Pos3_GL16/output/";
        Integer tmin = 1;
        Integer tmax = 10;
        startMoma(false, inputPath, outputPath, tmin, tmax, true, new String[]{"-ground_truth_export"});
    }


    public void _20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12() {
        String inputPath = datasets_base_path + "/lis_20211026__Pos7_GL12/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif";
        String outputPath = datasets_base_path + "/lis_20211026__Pos7_GL12/output/";
        Integer tmin = 1;
        Integer tmax = 480;
        startMoma(false, inputPath, outputPath, tmin, tmax, false, new String[]{"-ground_truth_export"});
//        startMoma(false, null, null, null, null, false, new String[]{"-ground_truth_export", "-reload", reload_folder_path});
    }

    public void _20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12__test_reloading() {
        String inputPath = datasets_base_path + "/lis_20211026__Pos7_GL12/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif";
        String outputPath = datasets_base_path + "/lis_20211026__Pos7_GL12/output/";
        String reload_folder_path = datasets_base_path + "/lis_20211026__Pos7_GL12/output";
        Integer tmin = null;
        Integer tmax = null;
//        startMoma(false, inputPath, outputPath, tmin, tmax, true, new String[]{"-ground_truth_export"});
        startMoma(false, null, null, null, null, false, new String[]{"-ground_truth_export", "-reload", reload_folder_path});
    }
}