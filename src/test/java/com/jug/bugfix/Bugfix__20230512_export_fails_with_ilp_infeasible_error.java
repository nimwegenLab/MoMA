package com.jug.bugfix;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;


public class Bugfix__20230512_export_fails_with_ilp_infeasible_error {
    final String datasetSubfolder;
    String analysisName;
    Integer tmin;
    Integer tmax;

    String datasetsBasePath = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */

    public Bugfix__20230512_export_fails_with_ilp_infeasible_error() {
        datasetSubfolder = "bugfix/20230512-export-fails-with-ilp-infeasible-error"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */
        analysisName = "test_analysis"; /* you can change this if you want to; but it is not needed */
        tmin = null;
        tmax = 10;
    }

    public static void main(String[] args) {
        Bugfix__20230512_export_fails_with_ilp_infeasible_error tests = new Bugfix__20230512_export_fails_with_ilp_infeasible_error();

//        tests.run_export_with_failing_original_data();
//        tests.run_reloading_with_fixed_mps_file();
        tests.run_interactive();
//        tests.run_reloading_interactive_result();
    }

    /**
     * Test-methods are below.
     */
    public void run_export_with_failing_original_data() {
        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder, "data/Pos6_GL7");
        analysisName = "prj_mm_antibio_analysis_2_REPRODUCE";
        startMoma(true, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }

    public void run_reloading_with_fixed_mps_file() {
        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder, "data/Pos6_GL7");
        analysisName = "prj_mm_antibio_analysis_2_FIXED";
        startMoma(false, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }

    public void run_interactive() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "data/Pos6_GL7", "20220701_VNG1040_AB2h_4_MMStack_Pos0_Pos6_GL7.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "data/Pos6_GL7", "mm.properties");
        analysisName = "test_interactive";
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }

    public void run_reloading_interactive_result() {
        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder, "data/Pos6_GL7");
        analysisName = "test_interactive";
        startMoma(false, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }
}