package com.jug.feature;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;


public class Feature__20230301_add_birth_assignment_to_enable_the_creation_of_cell_lineages {
    final String datasetSubfolder;
    String analysisName;
    Integer tmin;
    Integer tmax;

    String datasetsBasePath = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */

    public Feature__20230301_add_birth_assignment_to_enable_the_creation_of_cell_lineages() {
        datasetSubfolder = "feature/20230301-add-birth-assignment-to-enable-the-creation-of-cell-lineages"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */
        analysisName = "test_analysis"; /* you can change this if you want to; but it is not needed */
        tmin = null;
        tmax = 10;
    }

    public static void main(String[] args) {
        Feature__20230301_add_birth_assignment_to_enable_the_creation_of_cell_lineages tests = new Feature__20230301_add_birth_assignment_to_enable_the_creation_of_cell_lineages();

        tests.run_interactive__theo_20221220_21_9();
//        tests.run_interactive__theo_20221220_25_36();
//        tests.run_interactive__theo_20221220_28_35();
//        tests.run_interactive__lis_20220530_19_7();
//        tests.run_trackonly();
//        tests.run_reloading();
//        tests.run_export();
    }

    /**
     * Test-methods are below.
     */
    public void run_interactive__theo_20221220_21_9() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "/data/theo_20221220_21_9/20221220_glu_spcm_1_MMStack_Pos21_GL9.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "/data/theo_20221220_21_9/mm.properties");
        analysisName = "examine_tracking_errors_1";
        tmin = null;
        tmax = null;
//        tmin = 245;
//        tmax = 255;
//        tmin = 42;
//        tmax = 46;
//        tmin = null;
//        tmax = null;
//        tmin = 780;
//        tmax = 790;
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }

    public void run_interactive__theo_20221220_25_36() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "/data/theo_20221220_25_36/20221220_glu_spcm_1_MMStack_Pos25_GL36.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "/data/theo_20221220_25_36/mm.properties");
        analysisName = "examine_tracking_errors_1";
        tmin = null;
        tmax = null;
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }

    public void run_interactive__theo_20221220_28_35() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "/data/theo_20221220_28_35/20221220_glu_spcm_1_MMStack_Pos28_GL35.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "/data/theo_20221220_28_35/mm.properties");
        analysisName = "examine_tracking_errors_1";
        tmin = null;
        tmax = null;
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }

    public void run_interactive__lis_20220530_19_7() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "/data/lis_20220530_19_7/20220530_VNG1040_AB2h_1_MMStack_Pos19_GL7.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "/data/lis_20220530_19_7/mm.properties");
        analysisName = "examine_tracking_errors_1";
        tmin = null;
        tmax = null;
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }

//    public void run_trackonly() {
//        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
//        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
//        analysisName = "test_batch_run";
//        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-headless", "-p", properties_file_path.toString(), "-analysis", analysisName, "-trackonly"});
//    }
//
//    public void run_reloading() {
//        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder);
//        analysisName = "test_batch_run";
//        startMoma(false, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
//    }
//
//    public void run_export() {
//        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder);
//        analysisName = "test_batch_run";
//        startMoma(true, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
//    }

}