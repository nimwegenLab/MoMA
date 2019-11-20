package com.jug;

import org.junit.Test;

public class MoMATest {
    public static void main(String[] args){
        MoMATest tests = new MoMATest();
        // TODO-MM-20191120: User tmin and tmax instead of having multiple duplicated datasets, with different frame-ranges.
//        tests._new_10frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3();
//        tests._50frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3();
//        tests._450frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3();
//        tests._20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3();
        tests._20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01__frames_400_450();
//        tests._20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01();
//        tests._20191105_glc_spcm_1_MMStack_Pos7_preproc_GL15();
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