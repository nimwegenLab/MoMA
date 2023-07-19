package com.jug.feature;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;


public class Feature__20230719_add_flag_to_disable_multithread {
    final String datasetSubfolder;
    String analysisName;
    Integer tmin;
    Integer tmax;

    String datasetsBasePath = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */

    public Feature__20230719_add_flag_to_disable_multithread() {
        datasetSubfolder = "feature/20230719-add-flag-to-disable-multithread"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */
        analysisName = "test_analysis"; /* you can change this if you want to; but it is not needed */
        tmin = null;
        tmax = 10;
    }

    public static void main(String[] args) {
        Feature__20230719_add_flag_to_disable_multithread tests = new Feature__20230719_add_flag_to_disable_multithread();

        tests.run_trackonly();
//        tests.run_multithreaded_trackonly();
    }

    /**
     * Test-methods are below.
     */
    public void run_trackonly() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "test_batch_run";
        tmin = null;
        tmax = 100;
        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-headless", "-f", "-p", properties_file_path.toString(), "-analysis", analysisName, "-trackonly"});
    }

    public void run_multithreaded_trackonly() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "test_batch_run";
        tmin = null;
        tmax = 100;
        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-headless", "-f", "-p", properties_file_path.toString(), "-analysis", analysisName, "-multithreaded", "-trackonly"});
    }
}