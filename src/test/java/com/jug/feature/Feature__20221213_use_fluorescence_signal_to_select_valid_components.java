package com.jug.feature;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;


public class Feature__20221213_use_fluorescence_signal_to_select_valid_components {
    final String datasetSubfolder;
    String analysisName;
    Integer tmin;
    Integer tmax;

    String datasetsBasePath = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */

    public Feature__20221213_use_fluorescence_signal_to_select_valid_components() {
        datasetSubfolder = "feature/20221213-use-fluorescence-signal-to-select-valid-components"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */
        analysisName = "test_analysis"; /* you can change this if you want to; but it is not needed */
        tmin = null;
        tmax = 10;
    }

    public static void main(String[] args) {
        Feature__20221213_use_fluorescence_signal_to_select_valid_components tests = new Feature__20221213_use_fluorescence_signal_to_select_valid_components();

//        tests.run_interactive();
//        tests.run_trackonly__20211026_7_12();
//        tests.run_reloading__20211026_7_12();
//        tests.run_export();

        tests.run_interactive__20220530_17_6();
//        tests.run_trackonly__20220530_17_6();
//        tests.run_reloading__20220530_17_6();

//        tests.run_trackonly__20220701_10_3();
//        tests.run_reloading__20220701_10_3();
    }

    /**
     * Test-methods are below.
     */
    public void run_interactive() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "test_interactive";
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }

    public void run_trackonly__20211026_7_12() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "test_batch_run__20211026_7_12";
        tmin = null;
        tmax = 500;
        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-headless", "-p", properties_file_path.toString(), "-analysis", analysisName, "-trackonly"});
    }

    public void run_reloading__20211026_7_12() {
        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder);
        analysisName = "test_batch_run__20211026_7_12";
        startMoma(false, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }

    public void run_interactive__20220530_17_6() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "data/20220530_17_6/20220530_VNG1040_AB2h_1_MMStack_Pos17_GL6.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "test_interactive__20220530_17_6";
        tmin = 305;
        tmax = 306;
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }

    public void run_trackonly__20220530_17_6() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "data/20220530_17_6/20220530_VNG1040_AB2h_1_MMStack_Pos17_GL6.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "test_batch_run__20220530_17_6";
        tmin = null;
        tmax = 480;
        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-headless", "-p", properties_file_path.toString(), "-analysis", analysisName, "-trackonly"});
    }

    public void run_reloading__20220530_17_6() {
        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder, "data/20220530_17_6");
        analysisName = "test_batch_run__20220530_17_6";
        startMoma(false, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }

    public void run_trackonly__20220701_10_3() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "data/20220701_10_3/20220701_VNG1040_AB2h_4_MMStack_Pos0_Pos10_GL3.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "test_batch_run__20220701_10_3";
        tmin = null;
        tmax = 480;
        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-headless", "-p", properties_file_path.toString(), "-analysis", analysisName, "-trackonly"});
    }

    public void run_reloading__20220701_10_3() {
        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder, "data/20220701_10_3");
        analysisName = "test_batch_run__20220701_10_3";
        startMoma(false, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }

    public void run_export() {
        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder);
        analysisName = "test_batch_run";
        startMoma(true, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }

}