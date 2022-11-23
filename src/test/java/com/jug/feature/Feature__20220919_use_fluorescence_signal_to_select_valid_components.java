package com.jug.feature;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;


public class Feature__20220919_use_fluorescence_signal_to_select_valid_components {
    final String datasetSubfolder;
    String analysisName;
    Integer tmin;
    Integer tmax;

    String datasetsBasePath = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */

    public Feature__20220919_use_fluorescence_signal_to_select_valid_components() {
        datasetSubfolder = "feature/20220919-use-fluorescence-signal-to-select-valid-components"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */
        analysisName = "test_analysis"; /* you can change this if you want to; but it is not needed */
        tmin = 400;
        tmax = 500;
    }

    public static void main(String[] args) {
        Feature__20220919_use_fluorescence_signal_to_select_valid_components tests = new Feature__20220919_use_fluorescence_signal_to_select_valid_components();

        tests.run_interactive();
//        tests.run_trackonly();
//        tests.run_reloading();
//        tests.run_export();
    }

    /**
     * Test-methods are below.
     */
    public void run_interactive() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "test_interactive_run"; /* you can change this if you want to; but it is not needed */
        tmin = 440;
        tmax = 460;
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }

    public void run_trackonly() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "test_batch_run"; /* you can change this if you want to; but it is not needed */
        tmin = null;
        tmax = 10;
        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-headless", "-p", properties_file_path.toString(), "-analysis", analysisName, "-trackonly"});
    }

    public void run_reloading() {
        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder);
        analysisName = "test_batch_run"; /* you can change this if you want to; but it is not needed */
//        analysisName = "test_interactive_run"; /* you can change this if you want to; but it is not needed */
        startMoma(false, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }

    public void run_export() {
        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder);
        analysisName = "test_batch_run"; /* you can change this if you want to; but it is not needed */
        startMoma(true, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }
}