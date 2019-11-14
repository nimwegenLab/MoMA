package com.jug;

import org.junit.Test;

public class MoMATest {
    public static void main(String[] args){
        MoMATest test1 = new MoMATest();
        test1.main();
    }

    @Test
    public void main() {
        MoMA moma = new MoMA();
        moma.HEADLESS = true;
        MoMA.main(new String[]{"-i", "/home/micha/Documents/01_work/git/MoMA/test_datasets/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3/50frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3.tiff",
                               "-o", "/home/micha/Documents/01_work/git/MoMA/test_datasets/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3/output_50frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3/"});
    }
}