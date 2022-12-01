package com.jug.bugfix;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;


public class Bugfix__20220112_fix_spurious_ilp_infeasible_error {
    final String datasetSubfolder;
    String analysisName;
    Integer tmin;
    Integer tmax;

    String datasetsBasePath = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */

    public Bugfix__20220112_fix_spurious_ilp_infeasible_error() {
        datasetSubfolder = "bugfix/20220112-fix-spurious-ilp-infeasible-error"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */
        analysisName = "test_analysis"; /* you can change this if you want to; but it is not needed */
        tmin = null;
        tmax = 10;
    }

    public static void main(String[] args) {
        Bugfix__20220112_fix_spurious_ilp_infeasible_error tests = new Bugfix__20220112_fix_spurious_ilp_infeasible_error();

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
        analysisName = "test_interactive";
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }

    public void run_trackonly() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "test_batch_run";
        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-headless", "-p", properties_file_path.toString(), "-analysis", analysisName, "-trackonly"});
    }

    public void run_reloading() {
        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder);
        analysisName = "test_batch_run";
        startMoma(false, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }

    public void run_export() {
        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder);
        analysisName = "test_batch_run";
        startMoma(true, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }

}