package com.jug;

import org.junit.Test;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

import static com.jug.util.JavaUtils.concatenateWithCollection;

public class MoMATest {
    //    String datasets_base_path = "/home/micha/Documents/01_work/git/MoMA/test_datasets";
//    String datasets_base_path = "/media/micha/T7/20210816_test_data_michael/home__micha__Documents__01_work/MoMA/test_datasets";
    String datasets_base_path = "/media/micha/T7/20210816_test_data_michael/Moma/MM_Testing/";

//-i
///home/micha/Documents/01_work/git/MoMA/test_datasets/Dany_synthetic_rich_media/cropped_420200403_5_rpmB_1_rrnB_synthrich7_1_MMStack_Pos13_preproc_GL16.tif
//##########################################################
//        -i
///home/micha/Documents/01_work/git/MoMA/test_datasets/Dany_synthetic_rich_media/420200403_5_rpmB_1_rrnB_synthrich7_1_MMStack_Pos13_preproc_GL16.tif

    public static void main(String[] args) {
        MoMATest tests = new MoMATest();
//        tests._dany_20200812_8proms_ace_1_MMStack_Pos25_GL22();
        tests._dany_20200812_8proms_ace_1_MMStack_Pos25_GL5();

//        tests._theo_20210923_glu_batch_1_MMStack_Pos0_GL38();
//        tests._dany_20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16();
//        tests._20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12();
//        tests._cell_fragments__lis_20201119_Pos6_GL6();
//        tests._cell_fragments__thomas_20200922__Pos16_GL19(); /* test-case for new cost calculation */
//        tests._cell_fragments__lis_20210521__Pos0_Gl12(); /* test-case for new cost calculation */
//        tests._cell_fragments__lis_20210521__Pos0_Gl10(); /* test-case for new cost calculation */
//        tests._filamenting_cell__lis_20201119__Pos5_GL17(); /* test-case for new cost calculation */
//        tests._debug_mapping_assignment_issue_when_using_only_plausible_assignments();
//        tests._missing_mapping_assignment__lis_20201119__Pos14_GL30();
//        tests._exception_when_loading_gl__thomas_20200910__Pos3_GL33();
//        tests._ilp_infeasible_crashes_moma__theo_20210517__Pos18_GL35();
//        tests._export_failure__theo_20210517__Pos8_GL11();
//        tests._lysing_cell__thomas_20200922__Pos0_GL30(); /* nice example for lysing cells */
//        tests._lysing_cell__thomas_20200922__Pos16_GL17(); /* nice example for lysing cells */
//        tests._lis_20210303__Pos0_GL14();
//        tests._theo__20210126_glc_spcm_1__Pos1_GL15();
//        tests._theo__20210126_glc_spcm_1__Pos1_GL14();
//        tests._20190515_hi1_med1_med2_rpmB_glu_gly_7_chr_rpmB_curated__dataset__Pos25_GL1();
//        tests._20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL02(); // problematic dataset
//        tests._20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL06(); // problematic dataset
//        tests._20191112_glc_spcm_1_MMStack_Pos7_preproc_GL15();
//        tests._20200528_glcIPTG_spcm_1_MMStack_Pos15_preproc_GL01(); // problematic dataset
//        tests._20180711_glyc_lactuloseTMG20uM_1_MMStack_Pos1_preproc_GL07();
//        tests._20180711_glyc_lactuloseTMG20uM_1_MMStack_Pos3_preproc_GL16();
//        tests._lugange_001();
//        tests._20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos0_preproc_GL04();
//        tests._20200417_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_richdefinedmedium_8_MMStack_Pos14_preproc_GL14(); // dataset with inhomogeneous PhC (rich-media cells)
//        tests._cropped_420200403_5_rpmB_1_rrnB_synthrich7_1_MMStack_Pos13_preproc_GL16();
//        tests._20190614_glu_lowLac_1_MMStack_Pos0_preproc_GL03();
//        tests._20190614_glu_lowLac_1_MMStack_Pos0_preproc_GL02();  // dataset with jumping cell
//        tests._new_10frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3(); // Bad segmentation with model_20200605-134801_36cfc364_cb8fe485.zip
//        tests._50frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3(); // Bad segmentation with model_20200605-134801_36cfc364_cb8fe485.zip
//        tests._450frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3(); // Bad segmentation with model_20200605-134801_36cfc364_cb8fe485.zip
//        tests._20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3(); // Bad segmentation with model_20200605-134801_36cfc364_cb8fe485.zip
//        tests._cropped_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3(); // Bad segmentation with model_20200605-134801_36cfc364_cb8fe485.zip
//        tests._20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01__frames_400_450();
//        tests.headless_20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01__frames_400_450();
//        tests._20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01();
//        tests._20191105_glc_spcm_1_MMStack_Pos7_preproc_GL15();
//        tests._cropped_20191105_glc_spcm_1_MMStack_Pos7_preproc_GL15();
    }

//    /media/micha/T7/20210816_test_data_michael/Moma/MM_Testing/000_moma_benchmarking/CIP/lis_20201119/Pos6_GL6

    
    public void _theo_20210923_glu_batch_1_MMStack_Pos0_GL38() {
        String inputPath = datasets_base_path + "/000_moma_benchmarking/other_test_data/theo_20210923__Pos0_GL38/20210923_glu_batch_1_MMStack_Pos0_GL38.tif";
        String outputPath = datasets_base_path + "/000_moma_benchmarking/other_test_data/theo_20210923__Pos0_GL38/output/";
        Integer tmin = 0;
        Integer tmax = 100;
//        Integer tmax = null;
        startMoma(true, inputPath, outputPath, tmin, tmax, true, new String[]{"-ground_truth_export"});
    }

    public void _dany_20200812_8proms_ace_1_MMStack_Pos25_GL22() {
        String inputPath = datasets_base_path + "/001_moma_debugging/20200812_8proms_ace_1/Pos25_GL22/20200812_8proms_ace_1_MMStack_Pos25_GL22.tif";
        String outputPath = datasets_base_path + "/001_moma_debugging/20200812_8proms_ace_1/Pos25_GL22/output/";
        Integer tmin = 0;
        Integer tmax = null;
        startMoma(true, inputPath, outputPath, tmin, tmax, true, new String[]{"-ground_truth_export"});
    }


    public void _dany_20200812_8proms_ace_1_MMStack_Pos25_GL5() {
        String inputPath = datasets_base_path + "/001_moma_debugging/20200812_8proms_ace_1/Pos25_GL5/20200812_8proms_ace_1_MMStack_Pos25_GL5.tif";
        String outputPath = datasets_base_path + "/001_moma_debugging/20200812_8proms_ace_1/Pos25_GL5/output/";
        Integer tmin = 0;
        Integer tmax = null;
        startMoma(true, inputPath, outputPath, tmin, tmax, true, new String[]{"-ground_truth_export"});
    }


    public void _dany_20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16() {
        String inputPath = datasets_base_path + "/000_moma_benchmarking/other_test_data/dany_20200730__Pos3_GL16/20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16.tif";
        String outputPath = datasets_base_path + "/000_moma_benchmarking/other_test_data/dany_20200730__Pos3_GL16/output/";
        Integer tmin = 0;
        Integer tmax = 10;
        startMoma(true, inputPath, outputPath, tmin, tmax, true, new String[]{"-ground_truth_export"});
    }


    public void _20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12() {
        String inputPath = datasets_base_path + "/000_moma_benchmarking/CEF/lis_20211026/Pos7_GL12/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif";
        String outputPath = datasets_base_path + "/000_moma_benchmarking/CEF/lis_20211026/Pos7_GL12/output/";
        Integer tmin = 0;
        Integer tmax = 480;
        startMoma(false, inputPath, outputPath, tmin, tmax, true, new String[]{"-ground_truth_export"});
    }

    
    public void _cell_fragments__lis_20201119_Pos6_GL6() {
        String inputPath = datasets_base_path + "/000_moma_benchmarking/CIP/lis_20201119/Pos6_GL6/cropped__20201119_VNG1040_AB2h_2h_1_MMStack_Pos6_GL6.tif";
        String outputPath = datasets_base_path + "000_moma_benchmarking/CIP/lis_20201119/Pos6_GL6/output/";
        Integer tmin = 0;
        Integer tmax = 400;
        startMoma(false, inputPath, outputPath, tmin, tmax, true, new String[]{"-ground_truth_export"});
    }

    
    public void _cell_fragments__thomas_20200922__Pos16_GL19() {
        String inputPath = datasets_base_path + "/000_moma_benchmarking/CEF/thomas_20200922/Pos16_GL19/cropped__20200922_M9glc_VNG1040-hi2_AB_1_MMStack_Pos16_GL19.tiff";
        String outputPath = datasets_base_path + "000_moma_benchmarking/CEF/thomas_20200922/Pos16_GL19/output/";
        Integer tmin = 0;
        Integer tmax = 480;
        startMoma(false, inputPath, outputPath, tmin, tmax, true);
    }

    
    public void _cell_fragments__lis_20210521__Pos0_Gl12() {
        String inputPath = datasets_base_path + "/000_moma_benchmarking/CIP/lis_20210521/Pos0_GL12/20210521_VNG1040_AB2h_2h_1_MMStack_Pos0_GL12.tif";
        String outputPath = datasets_base_path + "/000_moma_benchmarking/CIP/lis_20210521/Pos0_GL10/output/";
        Integer tmin = 0;
        Integer tmax = 480;
        startMoma(false, inputPath, outputPath, tmin, tmax, true);
    }

    
    public void _cell_fragments__lis_20210521__Pos0_Gl10() {
        String inputPath = datasets_base_path + "/000_moma_benchmarking/CIP/lis_20210521/Pos0_GL10/20210521_VNG1040_AB2h_2h_1_MMStack_Pos0_GL10.tif";
        String outputPath = datasets_base_path + "/000_moma_benchmarking/CIP/lis_20210521/Pos0_GL10/output/";
        Integer tmin = 0;
        Integer tmax = 5;
//        Integer tmin = 0;
//        Integer tmax = 480;
        startMoma(false, inputPath, outputPath, tmin, tmax, false, new String[]{"-ground_truth_export"});
    }

    
    public void _filamenting_cell__lis_20201119__Pos5_GL17() {
        String inputPath = datasets_base_path + "/000_moma_benchmarking/problem_cases/filamenting_cells/CIP/lis_20201119/Pos5_GL17/cropped__20201119_VNG1040_AB2h_2h_1_MMStack_Pos5_GL17.tif";
        String outputPath = datasets_base_path + "/000_moma_benchmarking/problem_cases/filamenting_cells/CIP/lis_20201119/Pos5_GL17/output/";
//        Integer tmin = 120;
//        Integer tmax = 140;
//        Integer tmin = 160;
//        Integer tmax = 303;
        Integer tmin = 0;
        Integer tmax = 480;
        startMoma(false, inputPath, outputPath, tmin, tmax, true);
    }

    
    public void _debug_mapping_assignment_issue_when_using_only_plausible_assignments() {
        String datasetPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/001_bugfixing/20210812__issue_with_generating_plausible_assignments";
        String inputPath = datasetPath + "/cropped__20200922_M9glc_VNG1040-hi2_AB_1_MMStack_Pos0_GL30__frame_126__6_repeats_of_same_frame__20210812.tif";
        String outputPath = datasetPath + "/output/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _missing_mapping_assignment__lis_20201119__Pos14_GL30() {
        String datasetPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/000_moma_benchmarking/problem_cases/lysing_cell_inside_gl/CEF/lis_20201119/Pos14_GL30";
        String inputPath = datasetPath + "/cropped__20201119_VNG1040_AB2h_2h_1_MMStack_Pos14_GL30.tif";
        String outputPath = datasetPath + "/output/";
        startMoma(false, inputPath, outputPath, 261, 264, true);
    }

    
    public void _exception_when_loading_gl__thomas_20200910__Pos3_GL33() {
        String datasetPath = datasets_base_path + "/001_bugfixing/20210726__exception_on_loading_gl/thomas_20200910/Pos3_GL33";
        String inputPath = datasetPath + "/20200910_M9glc_VNG1040-hi2_AB_2_MMStack_Pos3_GL33.tif";
        String outputPath = datasetPath + "/output/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _ilp_infeasible_crashes_moma__theo_20210517__Pos18_GL35() {
        String datasetPath = datasets_base_path + "/001_bugfixing/20210722__ilp_infeasible_crashes_moma/thomas_20200922/Pos18_GL35/";
//        String inputPath = datasetPath + "20210517_X_spm_1_MMStack_Pos8_GL11.tif";
        String inputPath = datasetPath + "/cropped__20200922_M9glc_VNG1040-hi2_AB_1_MMStack_Pos28_GL35.tiff";
        String outputPath = datasetPath + "/output/";
        startMoma(false, inputPath, outputPath, 112, 116, true);
    }

    
    public void _export_failure__theo_20210517__Pos8_GL11() {
        String datasetPath = datasets_base_path + "/001_bugfixing/20210722__exception_during_export/theo_20210517/Pos8_GL11/";
//        String inputPath = datasetPath + "20210517_X_spm_1_MMStack_Pos8_GL11.tif";
        String inputPath = datasetPath + "failing_frame_isolated__20210517_X_spm_1_MMStack_Pos8_GL11.tif";
        String outputPath = datasetPath + "/output/";
        startMoma(true, inputPath, outputPath, null, null, true);
    }

    
    public void _lysing_cell__thomas_20200922__Pos0_GL30() {
        String inputPath = datasets_base_path + "/000_moma_benchmarking/problem_cases/lysing_cell_inside_gl/thomas_20200922/Pos0_GL30/cropped__20200922_M9glc_VNG1040-hi2_AB_1_MMStack_Pos0_GL30.tiff";
        String outputPath = datasets_base_path + "/000_moma_benchmarking/problem_cases/lysing_cell_inside_gl/thomas_20200922/Pos0_GL30/output/";
        Integer tmin = 120;
        Integer tmax = 140;
//        Integer tmin = 0;
//        Integer tmax = 480;
        startMoma(false, inputPath, outputPath, tmin, tmax, true);
//        startMoma(false, inputPath, outputPath, null, null, true);
    }

    // GOOD FOR DEMO PURPOSES
    
    public void _lysing_cell__thomas_20200922__Pos16_GL17() {
        String inputPath = datasets_base_path + "/000_moma_benchmarking/problem_cases/lysing_cell_inside_gl/thomas_20200922/Pos16_GL17/cropped__20200922_M9glc_VNG1040-hi2_AB_1_MMStack_Pos16_GL17.tiff";
        String outputPath = datasets_base_path + "/000_moma_benchmarking/problem_cases/lysing_cell_inside_gl/thomas_20200922/Pos16_GL17/output/";
        Integer tmin = 0;
        Integer tmax = 20;
//        startMoma(false, inputPath, outputPath, tmin, tmax, true, new String[]{"-ground_truth_export"});
        startMoma(false, inputPath, outputPath, tmin, tmax, true);
//        startMoma(false, inputPath, outputPath, tmin, tmax, true);
//        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _lis_20210303__Pos0_GL14() {
//        String inputPath = datasets_base_path + "/lis_20210303/Pos0_GL14/20210303_VNG40_12h_2hAB_10h_1_MMStack_Pos0_GL14.tiff";
        String inputPath = datasets_base_path + "/lis_20210303/Pos0_GL14/20210303_VNG40_12h_2hAB_10h_1_MMStack_Pos0_GL14__cropped.tiff";
        String outputPath = datasets_base_path + "/lis_20210303/Pos0_GL14/output/";
        Integer tmin = 100;
        Integer tmax = 200;
//        startMoma(false, inputPath, outputPath, null, null, true);
        startMoma(false, inputPath, outputPath, tmin, tmax, true);
    }

    
    public void _theo__20210126_glc_spcm_1__Pos1_GL15() {
        String inputPath = datasets_base_path + "/20200626__datasets_for_work_on_ticket_021-improve-image-normalization/00_datasets_from_theo/01_bad_datasets/theo__20210126_glc_spcm_1/Pos1_GL15/20210126_glc_spcm_1_MMStack_Pos1_GL15.tiff";
        String outputPath = datasets_base_path + "/20200626__datasets_for_work_on_ticket_021-improve-image-normalization/00_datasets_from_theo/01_bad_datasets/theo__20210126_glc_spcm_1/Pos1_GL15/output/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _theo__20210126_glc_spcm_1__Pos1_GL14() {
        String inputPath = datasets_base_path + "/20200626__datasets_for_work_on_ticket_021-improve-image-normalization/00_datasets_from_theo/01_bad_datasets/theo__20210126_glc_spcm_1/Pos1_GL14/20210126_glc_spcm_1_MMStack_Pos1_GL14.tiff";
        String outputPath = datasets_base_path + "/20200626__datasets_for_work_on_ticket_021-improve-image-normalization/00_datasets_from_theo/01_bad_datasets/theo__20210126_glc_spcm_1/Pos1_GL14/output/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL06() {
        // NOTES: * cropped image performs slightly better, but not good
//        String inputPath = datasets_base_path + "/20200626__datasets_for_work_on_ticket_021-improve-image-normalization/01_dany_datasets/20200516_bottom_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa/20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL06/20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL06.tif";
        String inputPath = datasets_base_path + "/20200626__datasets_for_work_on_ticket_021-improve-image-normalization/01_dany_datasets/20200516_bottom_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa/20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL06/cropped_20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL06.tif";
//        String inputPath = datasets_base_path + "/20200626__datasets_for_work_on_ticket_021-improve-image-normalization/01_dany_datasets/20200516_bottom_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa/20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL06/20200629__frame_sets_for_comparison/frame_346_bad__cropped_20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL06.tif";
//        String inputPath = datasets_base_path + "/20200626__datasets_for_work_on_ticket_021-improve-image-normalization/01_dany_datasets/20200516_bottom_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa/20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL06/20200629__frame_sets_for_comparison/frame_346_bad__PhC__16bit__cropped_20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL06.tif";
//        String inputPath = datasets_base_path + "/20200626__datasets_for_work_on_ticket_021-improve-image-normalization/01_dany_datasets/20200516_bottom_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa/20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL06/20200629__frame_sets_for_comparison/frame_346_bad__PhC__16bit__MATCHED_HISTOGRAM__cropped_20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL06.tif";
//        String inputPath = datasets_base_path + "/20200626__datasets_for_work_on_ticket_021-improve-image-normalization/01_dany_datasets/20200516_bottom_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa/20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL06/20200629__frame_sets_for_comparison/frame_345_good__cropped_20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL06.tif";
//        String inputPath = datasets_base_path + "/20200626__datasets_for_work_on_ticket_021-improve-image-normalization/01_dany_datasets/20200516_bottom_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa/20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL06/cropped_20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL06.tif";
        String outputPath = datasets_base_path + "/20200626__datasets_for_work_on_ticket_021-improve-image-normalization/01_dany_datasets/20200516_bottom_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa/20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL06/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL02() {
        // NOTES: * cropped image performs slightly better, but not good
//        String inputPath = datasets_base_path + "/20200626__datasets_for_work_on_ticket_021-improve-image-normalization/01_dany_datasets/20200516_bottom_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa/20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL02/20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL02.tif";
        String inputPath = datasets_base_path + "/20200626__datasets_for_work_on_ticket_021-improve-image-normalization/01_dany_datasets/20200516_bottom_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa/20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL02/cropped_20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL02.tif";
        String outputPath = datasets_base_path + "/20200626__datasets_for_work_on_ticket_021-improve-image-normalization/01_dany_datasets/20200516_bottom_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa/20200516_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_gluaa_glyaa_2_MMStack_Pos3_preproc_GL06/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _20191112_glc_spcm_1_MMStack_Pos7_preproc_GL15() {
//        String inputPath = datasets_base_path + "/20191112_H07_bolA/20191112_glc_spcm_1_MMStack_Pos7_preproc_GL15/20191112_glc_spcm_1_MMStack_Pos7_preproc_GL15.tif";
//        String inputPath = datasets_base_path + "/20191112_H07_bolA/20191112_glc_spcm_1_MMStack_Pos7_preproc_GL15/cropped_20191112_glc_spcm_1_MMStack_Pos7_preproc_GL15.tif";
        String inputPath = datasets_base_path + "/20191112_H07_bolA/20191112_glc_spcm_1_MMStack_Pos7_preproc_GL15/cropped_smoothed_20191112_glc_spcm_1_MMStack_Pos7_preproc_GL15.tif";
//        String inputPath = datasets_base_path + "/20191112_H07_bolA/20191112_glc_spcm_1_MMStack_Pos7_preproc_GL15/cropped_2_20191112_glc_spcm_1_MMStack_Pos7_preproc_GL15.tif";
//        String inputPath = datasets_base_path + "/20191112_H07_bolA/20191112_glc_spcm_1_MMStack_Pos7_preproc_GL15/phc_channel_only__20191112_glc_spcm_1_MMStack_Pos7_preproc_GL15.tif";
        String outputPath = datasets_base_path + "/20191112_H07_bolA/20191112_glc_spcm_1_MMStack_Pos7_preproc_GL15/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _20200528_glcIPTG_spcm_1_MMStack_Pos15_preproc_GL01() {
//        String inputPath = datasets_base_path + "/20200626__datasets_for_work_on_ticket_021-improve-image-normalization/00_datasets_from_theo/01_bad_datasets/20200528_glcIPTG_spcm_1_MMStack_Pos15_preproc_GL01/20200528_glcIPTG_spcm_1_MMStack_Pos15_preproc_GL01.tif";
        String inputPath = datasets_base_path + "/20200626__datasets_for_work_on_ticket_021-improve-image-normalization/00_datasets_from_theo/01_bad_datasets/20200528_glcIPTG_spcm_1_MMStack_Pos15_preproc_GL01/cropped_20200528_glcIPTG_spcm_1_MMStack_Pos15_preproc_GL01.tif";
//        String inputPath = datasets_base_path + "/20200626__datasets_for_work_on_ticket_021-improve-image-normalization/00_datasets_from_theo/01_bad_datasets/20200528_glcIPTG_spcm_1_MMStack_Pos15_preproc_GL01/cropped_2_20200528_glcIPTG_spcm_1_MMStack_Pos15_preproc_GL01.tif";
//        String inputPath = datasets_base_path + "/20200626__datasets_for_work_on_ticket_021-improve-image-normalization/00_datasets_from_theo/01_bad_datasets/20200528_glcIPTG_spcm_1_MMStack_Pos15_preproc_GL01/cropped_first_frames_20200528_glcIPTG_spcm_1_MMStack_Pos15_preproc_GL01.tif";
//        String inputPath = datasets_base_path + "/20200626__datasets_for_work_on_ticket_021-improve-image-normalization/00_datasets_from_theo/01_bad_datasets/20200528_glcIPTG_spcm_1_MMStack_Pos15_preproc_GL01/phc_channel_only__20200528_glcIPTG_spcm_1_MMStack_Pos15_preproc_GL01.tif";
        String outputPath = datasets_base_path + "/20200626__datasets_for_work_on_ticket_021-improve-image-normalization/00_datasets_from_theo/01_bad_datasets/20200528_glcIPTG_spcm_1_MMStack_Pos15_preproc_GL01/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _20180711_glyc_lactuloseTMG20uM_1_MMStack_Pos1_preproc_GL07() {
        String inputPath = datasets_base_path + "/20200618_deepmoma_testing/20180711_glyc_lactuloseTMG20uM_1_MMStack_Pos1_preproc_GL07/cropped_20180711_glyc_lactuloseTMG20uM_1_MMStack_Pos1_preproc_GL07.tif";
        String outputPath = datasets_base_path + "/20200618_deepmoma_testing/20180711_glyc_lactuloseTMG20uM_1_MMStack_Pos1_preproc_GL07/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _20180711_glyc_lactuloseTMG20uM_1_MMStack_Pos3_preproc_GL16() {
        String inputPath = datasets_base_path + "/20200618_deepmoma_testing/20180711_glyc_lactuloseTMG20uM_1_MMStack_Pos3_preproc_GL16/cropped_20180711_glyc_lactuloseTMG20uM_1_MMStack_Pos3_preproc_GL16.tif";
        String outputPath = datasets_base_path + "/20200618_deepmoma_testing/20180711_glyc_lactuloseTMG20uM_1_MMStack_Pos3_preproc_GL16/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _lugange_001() {
//        String inputPath = datasets_base_path + "/20200422_test_lugange_dataset/series_01_crop_with_filamenting_cell.tif";
        String inputPath = datasets_base_path + "/20200422_test_lugange_dataset/cropped_series_01_crop_with_filamenting_cell.tif";
        String outputPath = datasets_base_path + "/20200422_test_lugange_dataset/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos0_preproc_GL04() {
        String inputPath = datasets_base_path + "/Dany_20190515/20190515_hi1_med1_med2_rpmB_glu_gly_7_chr_hi1_deepmoma_2019-10-12_1d98d2f6/FAILED_20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos0_preproc_GL04_deepmoma/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos0_preproc_GL04.tif";
        String outputPath = datasets_base_path + "/Dany_20190515/20190515_hi1_med1_med2_rpmB_glu_gly_7_chr_hi1_deepmoma_2019-10-12_1d98d2f6/FAILED_20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos0_preproc_GL04_deepmoma/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _20190515_hi1_med1_med2_rpmB_glu_gly_7_chr_rpmB_curated__dataset__Pos25_GL1() {
        String inputPath = datasets_base_path + "/Dany_20190515/20190515_hi1_med1_med2_rpmB_glu_gly_7_chr_hi1_deepmoma_2019-10-12_1d98d2f6/20190515_hi1_med1_med2_rpmB_glu_gly_7_chr_rpmB_curated__dataset__Pos25_GL1/images_phase_contrast_01_dany_20190515_hi1_med1_med2_rpmB_glu_gly_7_chr_rpmB_curated__dataset__Pos25_GL1__reshaped.tiff";
        String outputPath = datasets_base_path + "/Dany_20190515/20190515_hi1_med1_med2_rpmB_glu_gly_7_chr_hi1_deepmoma_2019-10-12_1d98d2f6/20190515_hi1_med1_med2_rpmB_glu_gly_7_chr_rpmB_curated__dataset__Pos25_GL1/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }


    
    public void _cropped_420200403_5_rpmB_1_rrnB_synthrich7_1_MMStack_Pos13_preproc_GL16() {
        String inputPath = datasets_base_path + "/Dany_synthetic_rich_media/cropped_420200403_5_rpmB_1_rrnB_synthrich7_1_MMStack_Pos13_preproc_GL16.tif";
        String outputPath = datasets_base_path + "/Dany_synthetic_rich_media/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _20200417_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_richdefinedmedium_8_MMStack_Pos14_preproc_GL15() {
        String inputPath = datasets_base_path + "/05_inhomogeneous_phase_contrast_intensity/00_dany_20200417_top_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_richdefinedmedium/Pos14_GL15/20200417_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_richdefinedmedium_8_MMStack_Pos14_preproc_GL15.tif";
        String outputPath = datasets_base_path + "/05_inhomogeneous_phase_contrast_intensity/00_dany_20200417_top_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_richdefinedmedium/Pos14_GL15/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _20200417_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_richdefinedmedium_8_MMStack_Pos14_preproc_GL14() {
        String inputPath = datasets_base_path + "/05_inhomogeneous_phase_contrast_intensity/00_dany_20200417_top_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_richdefinedmedium/Pos14_GL14/20200417_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_richdefinedmedium_8_MMStack_Pos14_preproc_GL14.tif";
        String outputPath = datasets_base_path + "/05_inhomogeneous_phase_contrast_intensity/00_dany_20200417_top_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_richdefinedmedium/Pos14_GL14/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _20190614_glu_lowLac_1_MMStack_Pos0_preproc_GL03() {
        String inputPath = datasets_base_path + "/20190614_glu_lowLac_1_MMStack_Pos0_preproc_GL03/20190614_glu_lowLac_1_MMStack_Pos0_preproc_GL03.tif";
        String outputPath = datasets_base_path + "/20190614_glu_lowLac_1_MMStack_Pos0_preproc_GL03/output/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _20190614_glu_lowLac_1_MMStack_Pos0_preproc_GL02() {
        String inputPath = datasets_base_path + "/20190614_glu_lowLac_1_MMStack_Pos0_preproc_GL02/20190614_glu_lowLac_1_MMStack_Pos0_preproc_GL02__frames_220-250.tif";
        String outputPath = datasets_base_path + "/20190614_glu_lowLac_1_MMStack_Pos0_preproc_GL02/output/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _new_10frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3() {
        String inputPath = datasets_base_path + "/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3/new_10frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3.tif";
        String outputPath = datasets_base_path + "/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3/output/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _50frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3() {
        String inputPath = datasets_base_path + "/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3/50frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3.tiff";
        String outputPath = datasets_base_path + "/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3/output/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _450frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3() {
        String inputPath = datasets_base_path + "/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3/450frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3.tif";
        String outputPath = datasets_base_path + "/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3/output/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3() {
        String inputPath = datasets_base_path + "/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3.tiff";
//        String inputPath = datasets_base_path + "/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3/cropped_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3.tiff";
        String outputPath = datasets_base_path + "/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3/output/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _cropped_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3() {
        String inputPath = datasets_base_path + "/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3/cropped_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3.tiff";
        String outputPath = datasets_base_path + "/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3/output/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void headless_20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01__frames_400_450() {
        String inputPath = datasets_base_path + "/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01__frames_400-450.tif";
        String outputPath = datasets_base_path + "/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01/output_headless/";
        MoMA moma = new MoMA();
        MoMA.HEADLESS = true;
        MoMA.main(new String[]{"-i", inputPath, "-o", outputPath});
    }

    
    public void _20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01__frames_400_450() {
        String inputPath = datasets_base_path + "/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01__frames_400-450.tif";
        String outputPath = datasets_base_path + "/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01/output/";
        startMoma(false, inputPath, outputPath, null, null, true);
//        startMoma(new String[]{"-i", inputPath, "-o", outputPath, "-p", "/home/micha/Documents/01_work/git/MoMA/mm.properties"});
    }

    
    public void _20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01() {
        String inputPath = datasets_base_path + "/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01.tif";
        String outputPath = datasets_base_path + "/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01/output/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _20191105_glc_spcm_1_MMStack_Pos7_preproc_GL15() {
        String inputPath = datasets_base_path + "/20191105_glc_spcm_1_MMStack_Pos7_preproc_GL15/20191105_glc_spcm_1_MMStack_Pos7_preproc_GL15.tif";
        String outputPath = datasets_base_path + "/20191105_glc_spcm_1_MMStack_Pos7_preproc_GL15/output/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    
    public void _cropped_20191105_glc_spcm_1_MMStack_Pos7_preproc_GL15() {
        String inputPath = datasets_base_path + "/20191105_glc_spcm_1_MMStack_Pos7_preproc_GL15/cropped_20191105_glc_spcm_1_MMStack_Pos7_preproc_GL15.tif";
        String outputPath = datasets_base_path + "/20191105_glc_spcm_1_MMStack_Pos7_preproc_GL15/output/";
        startMoma(false, inputPath, outputPath, null, null, true);
    }

    private void startMoma(boolean headless, String inputPath, String outputPath, Integer tmin, Integer tmax, boolean deleteProbabilityMaps) {
        startMoma(headless, inputPath, outputPath, tmin, tmax, deleteProbabilityMaps, null);
    }

    private void startMoma(boolean headless, String inputPath, String outputPath, Integer tmin, Integer tmax, boolean deleteProbabilityMaps, String[] additionalArgs) {
        if (deleteProbabilityMaps) {
            remove_probability_maps(inputPath);
        }
        create_output_folder(outputPath);

        String[] args;

        if (tmin != null && tmax != null) {
            args = new String[]{"-i", inputPath, "-o", outputPath, "-tmin", tmin.toString(), "-tmax", tmax.toString()};
        } else if (tmin != null && tmax == null) {
            args = new String[]{"-i", inputPath, "-o", outputPath, "-tmin", tmin.toString()};
        } else if (tmin == null && tmax != null) {
            args = new String[]{"-i", inputPath, "-o", outputPath, "-tmax", tmax.toString()};
        } else { // both tmin and tmax are null
            args = new String[]{"-i", inputPath, "-o", outputPath};
        }
        if (additionalArgs != null) {
            args = concatenateWithCollection(args, additionalArgs);
        }
        MoMA.HEADLESS = headless;
        MoMA.main(args);
    }

    private void create_output_folder(String outputPath) {
        File file = new File(outputPath);
        file.mkdir();
    }

    /**
     * Delete preexisting probability maps. During testing, we often want to test the generation
     * of the probability maps, which are cached to disk and loaded, if they exist for a given model.
     * This function removes those cached files to always run the U-Net preprocessing.
     *
     * @param path
     */
    private void remove_probability_maps(String path) {
        PathMatcher matcher =
                FileSystems.getDefault().getPathMatcher("glob:*__model_*.tif*");
        File f = new File(path);
        File parentFolder = new File(f.getParent());

        String[] pathnames = parentFolder.list();
        for (String name : pathnames) {
            String filePath = parentFolder + "/" + name;
            if (matcher.matches(Paths.get(name))) {
                System.out.print(filePath);
                File f2 = new File(filePath);
                if (f2.delete())                      //returns Boolean value
                {
                    System.out.println("Deleted: " + f.getName());   //getting and printing the file name
                } else {
                    System.out.println("Failed to delete: " + f.getName());
                }
            }
        }
    }
}