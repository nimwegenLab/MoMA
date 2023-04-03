package com.jug.feature;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;


public class Feature__20230330_2_improve_tracking_performance_at_detection_roi_border {
    final String datasetSubfolder;
    String analysisName;
    Integer tmin;
    Integer tmax;

    String datasetsBasePath = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */

    public Feature__20230330_2_improve_tracking_performance_at_detection_roi_border() {
        datasetSubfolder = "feature/20230330-2-improve-tracking-performance-at-detection-roi-border"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */
        analysisName = "test_analysis"; /* you can change this if you want to; but it is not needed */
        tmin = null;
        tmax = 10;
    }

    public static void main(String[] args) {
        Feature__20230330_2_improve_tracking_performance_at_detection_roi_border tests = new Feature__20230330_2_improve_tracking_performance_at_detection_roi_border();

//        tests.run_interactive__theo_20221220_25_36();
//        tests.run_interactive__theo_20221220_28_35();
//        tests.run_interactive__lis_20220530_19_7();
//        tests.run_interactive__lis_20211026_7_12();
//        tests.run_interactive__lis_20211026_11_29();
//        tests.run_interactive__lis_20211026_12_26();
//        tests.run_interactive__lis_20211026_12_26();
//        tests.run_interactive__lis_20211026_18_2();
    }

    /**
     * Test-methods are below.
     */
    public void run_interactive__theo_20221220_25_36() {
        tmin = 300;
        tmax = 400;
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "data/theo_20221220_25_36/20221220_glu_spcm_1_MMStack_Pos25_GL36.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "test_interactive";
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }

    public void run_interactive__theo_20221220_28_35() {
        tmin = 300;
        tmax = 400;
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "data/theo_20221220_28_35/20221220_glu_spcm_1_MMStack_Pos28_GL35.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "test_interactive";
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }

    public void run_interactive__lis_20220530_19_7() {
        tmin = null;
        tmax = 100;
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "data/lis_20220530_19_7/20220530_VNG1040_AB2h_1_MMStack_Pos19_GL7.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "test_interactive";
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }

    public void run_interactive__lis_20211026_7_12() {
        tmin = 300;
        tmax = 400;
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "data/lis_20211026_7_12/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "test_interactive";
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }

    public void run_interactive__lis_20220530_11_29() {
        tmin = 300;
        tmax = 400;
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "data/theo_20221220_28_35/20220530_VNG1040_AB2h_1_MMStack_Pos11_GL29.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "test_interactive";
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }

    public void run_interactive__lis_20220530_12_26() {
        tmin = 300;
        tmax = 400;
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "data/theo_20221220_12_26/20220530_VNG1040_AB2h_1_MMStack_Pos12_GL26.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "test_interactive";
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }

    public void run_interactive__lis_20220530_18_2() {
        tmin = 300;
        tmax = 400;
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "data/theo_20221220_18_2/20220530_VNG1040_AB2h_1_MMStack_Pos18_GL2.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "test_interactive";
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }
}