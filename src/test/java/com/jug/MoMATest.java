package com.jug;

import org.junit.Test;

public class MoMATest {
    public static void main(String[] args){
        MoMATest tests = new MoMATest();
        // TODO-MM-20191120: User tmin and tmax instead of having multiple duplicated datasets, with different frame-ranges.
        tests._20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos0_preproc_GL04(); // COMPARISON DATASET 1
//        tests._20200417_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_richdefinedmedium_8_MMStack_Pos14_preproc_GL14();
//        tests._cropped_420200403_5_rpmB_1_rrnB_synthrich7_1_MMStack_Pos13_preproc_GL16(); // COMPARISON DATASET 2
//        tests._20190614_glu_lowLac_1_MMStack_Pos0_preproc_GL03();
//        tests._20190614_glu_lowLac_1_MMStack_Pos0_preproc_GL02();
//        tests._new_10frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3();
//        tests._50frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3();
//        tests._450frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3();
//        tests._20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3();
//        tests._20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01__frames_400_450();
//        tests.headless_20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01__frames_400_450();
//        tests._20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01();
//        tests._20191105_glc_spcm_1_MMStack_Pos7_preproc_GL15();
    }

//-i
///home/micha/Documents/01_work/git/MoMA/test_datasets/Dany_synthetic_rich_media/cropped_420200403_5_rpmB_1_rrnB_synthrich7_1_MMStack_Pos13_preproc_GL16.tif
//##########################################################
//        -i
///home/micha/Documents/01_work/git/MoMA/test_datasets/Dany_synthetic_rich_media/420200403_5_rpmB_1_rrnB_synthrich7_1_MMStack_Pos13_preproc_GL16.tif

    @Test
    public void _20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos0_preproc_GL04() {
        String inputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/Dany_20190515/20190515_hi1_med1_med2_rpmB_glu_gly_7_chr_hi1_deepmoma_2019-10-12_1d98d2f6/FAILED_20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos0_preproc_GL04_deepmoma/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos0_preproc_GL04.tif";
        String outputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/Dany_20190515/20190515_hi1_med1_med2_rpmB_glu_gly_7_chr_hi1_deepmoma_2019-10-12_1d98d2f6/FAILED_20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos0_preproc_GL04_deepmoma/";
        startMoma(new String[]{"-i", inputPath, "-o", outputPath});
    }

    @Test
    public void _cropped_420200403_5_rpmB_1_rrnB_synthrich7_1_MMStack_Pos13_preproc_GL16() {
        String inputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/Dany_synthetic_rich_media/cropped_420200403_5_rpmB_1_rrnB_synthrich7_1_MMStack_Pos13_preproc_GL16.tif";
        String outputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/Dany_synthetic_rich_media/";
        startMoma(new String[]{"-i", inputPath, "-o", outputPath});
    }

    @Test
    public void _20200417_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_richdefinedmedium_8_MMStack_Pos14_preproc_GL15() {
        String inputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/05_inhomogeneous_phase_contrast_intensity/00_dany_20200417_top_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_richdefinedmedium/Pos14_GL15/20200417_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_richdefinedmedium_8_MMStack_Pos14_preproc_GL15.tif";
        String outputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/05_inhomogeneous_phase_contrast_intensity/00_dany_20200417_top_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_richdefinedmedium/Pos14_GL15/";
        startMoma(new String[]{"-i", inputPath, "-o", outputPath});
    }

    @Test
    public void _20200417_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_richdefinedmedium_8_MMStack_Pos14_preproc_GL14() {
        String inputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/05_inhomogeneous_phase_contrast_intensity/00_dany_20200417_top_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_richdefinedmedium/Pos14_GL14/20200417_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_richdefinedmedium_8_MMStack_Pos14_preproc_GL14.tif";
        String outputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/05_inhomogeneous_phase_contrast_intensity/00_dany_20200417_top_rpmB_rplN_rpsB_rrnB_hi1_hi3_med2_med3_richdefinedmedium/Pos14_GL14/";
        startMoma(new String[]{"-i", inputPath, "-o", outputPath});
    }

    @Test
    public void _20190614_glu_lowLac_1_MMStack_Pos0_preproc_GL03() {
        String inputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20190614_glu_lowLac_1_MMStack_Pos0_preproc_GL03";
        String outputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20190614_glu_lowLac_1_MMStack_Pos0_preproc_GL03/output/";
        startMoma(new String[]{"-i", inputPath, "-o", outputPath});
    }

    @Test
    public void _20190614_glu_lowLac_1_MMStack_Pos0_preproc_GL02() {
        String inputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20190614_glu_lowLac_1_MMStack_Pos0_preproc_GL02/20190614_glu_lowLac_1_MMStack_Pos0_preproc_GL02__frames_220-250.tif";
        String outputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20190614_glu_lowLac_1_MMStack_Pos0_preproc_GL02/output/";
        startMoma(new String[]{"-i", inputPath, "-o", outputPath});
    }

    @Test
    public void _new_10frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3() {
        String inputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3/new_10frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3.tif";
        String outputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3/output/";
        startMoma(new String[]{"-i", inputPath, "-o", outputPath});
    }

    @Test
    public void _50frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3() {
        String inputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3/50frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3.tiff";
        String outputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3/output/";
        startMoma(new String[]{"-i", inputPath, "-o", outputPath});
    }

    @Test
    public void _450frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3() {
        String inputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3/450frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3.tif";
        String outputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3/output/";
        startMoma(new String[]{"-i", inputPath, "-o", outputPath});
    }

    @Test
    public void _20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3() {
        String inputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3.tiff";
        String outputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3/output/";
        startMoma(new String[]{"-i", inputPath, "-o", outputPath});
    }

    @Test
    public void headless_20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01__frames_400_450() {
        String inputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01__frames_400-450.tif";
        String outputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01/output_headless/";
        MoMA moma = new MoMA();
        moma.HEADLESS = true;
        MoMA.main(new String[]{"-i", inputPath, "-o", outputPath});
    }

    @Test
    public void _20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01__frames_400_450() {
        String inputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01__frames_400-450.tif";
        String outputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01/output/";
        startMoma(new String[]{"-i", inputPath, "-o", outputPath});
    }

    @Test
    public void _20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01() {
        String inputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01.tif";
        String outputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01/output/";
        startMoma(new String[]{"-i", inputPath, "-o", outputPath});
    }

    @Test
    public void _20191105_glc_spcm_1_MMStack_Pos7_preproc_GL15() {
        String inputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20191105_glc_spcm_1_MMStack_Pos7_preproc_GL15/20191105_glc_spcm_1_MMStack_Pos7_preproc_GL15.tif";
        String outputPath = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20191105_glc_spcm_1_MMStack_Pos7_preproc_GL15/output/";
        startMoma(new String[]{"-i", inputPath, "-o", outputPath});
    }

    private void startMoma(String[] args){
        MoMA moma = new MoMA();
        moma.HEADLESS = false;
        MoMA.main(args);
    }
}