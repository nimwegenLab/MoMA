package com.jug;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import static com.jug.exploration.ExplorationTestHelpers.startMoma;

import static com.jug.util.JavaUtils.concatenateWithCollection;

public class ResultLoadingExploratoration {
    //    String datasets_base_path = "/home/micha/Documents/01_work/git/MoMA/test_datasets";
//    String datasets_base_path = "/media/micha/T7/20210816_test_data_michael/home__micha__Documents__01_work/MoMA/test_datasets";
    String datasets_base_path = "/media/micha/T7/data_michael_mell/moma_test_data/000_development/feature/20220121-fix-loading-of-curated-datasets/";

//-i
///home/micha/Documents/01_work/git/MoMA/test_datasets/Dany_synthetic_rich_media/cropped_420200403_5_rpmB_1_rrnB_synthrich7_1_MMStack_Pos13_preproc_GL16.tif
//##########################################################
//        -i
///home/micha/Documents/01_work/git/MoMA/test_datasets/Dany_synthetic_rich_media/420200403_5_rpmB_1_rrnB_synthrich7_1_MMStack_Pos13_preproc_GL16.tif

    public static void main(String[] args) {
        ResultLoadingExploratoration tests = new ResultLoadingExploratoration();
        tests._dany_20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16__test_mm_properties_loading();
//        tests._dany_20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16();
//        tests._20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12();
    }


    public void _dany_20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16__test_mm_properties_loading() {
        String inputPath = datasets_base_path + "/dany_20200730__Pos3_GL16/20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16.tif";
        String outputPath = datasets_base_path + "/dany_20200730__Pos3_GL16/output";
        String settings_file_path = datasets_base_path + "/dany_20200730__Pos3_GL16/source_for_mm_properties_loading__DONT_DELETE/mm.properties";
        Integer tmin = 0;
        Integer tmax = 10;
        startMoma(false, inputPath, outputPath, tmin, tmax, true, new String[]{"-ground_truth_export", "-p", settings_file_path});
    }


    public void _dany_20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16() {
        String inputPath = datasets_base_path + "/dany_20200730__Pos3_GL16/20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16.tif";
        String outputPath = datasets_base_path + "/dany_20200730__Pos3_GL16/output/";
        Integer tmin = 0;
        Integer tmax = 10;
        startMoma(false, inputPath, outputPath, tmin, tmax, true, new String[]{"-ground_truth_export"});
    }


    public void _20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12() {
        String inputPath = datasets_base_path + "/lis_20211026__Pos7_GL12/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif";
        String outputPath = datasets_base_path + "/lis_20211026__Pos7_GL12/output/";
        Integer tmin = 0;
        Integer tmax = 480;
        startMoma(false, inputPath, outputPath, tmin, tmax, true, new String[]{"-ground_truth_export"});
    }
}