package com.jug.exploration;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;


public class Exploration__20230120_try_loading_data_from_version_v0_7_from_theo {
    final String datasetSubfolder;
    String analysisName;
    Integer tmin;
    Integer tmax;

    String datasetsBasePath = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */

    public Exploration__20230120_try_loading_data_from_version_v0_7_from_theo() {
        datasetSubfolder = "exploration/20230120-try-loading-data-from-version-v0_7-from-theo"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */
        analysisName = "test_analysis"; /* you can change this if you want to; but it is not needed */
        tmin = null;
        tmax = 10;
    }

    public static void main(String[] args) {
        Exploration__20230120_try_loading_data_from_version_v0_7_from_theo tests = new Exploration__20230120_try_loading_data_from_version_v0_7_from_theo();

//        tests.run_interactive();
//        tests.run_trackonly();
//        tests.run_reloading();
//        tests.run_reloading_theos_analysis();
        tests.run_export_theos_analysis();
//        tests.run_export();
    }

    /**
     * Test-methods are below.
     */
    public void run_interactive() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "Pos2_GL10", "20221220_glu_spcm_1_MMStack_Pos2_GL10.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "test_interactive";
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }

    public void run_trackonly() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "Pos2_GL10", "20221220_glu_spcm_1_MMStack_Pos2_GL10.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "test_batch_run";
        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-headless", "-p", properties_file_path.toString(), "-analysis", analysisName, "-trackonly"});
    }

    public void run_reloading() {
        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder, "Pos2_GL10");
        analysisName = "test_batch_run";
        startMoma(false, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }

    public void run_reloading_theos_analysis() {
        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder, "Pos2_GL10");
        analysisName = "20221220_analysis";
        startMoma(false, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }

    public void run_export_theos_analysis() {
        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder, "Pos2_GL10");
        analysisName = "20221220_analysis";
        startMoma(true, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }

}