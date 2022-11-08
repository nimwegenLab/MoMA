package com.jug.bugfix;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;


public class Bugfix__20221108_fix_issue_with_non_existing_assignments_in_data_from_lis {
    final String datasetSubfolder;
    String analysisName;
    Integer tmin;
    Integer tmax;

    String datasetsBasePath = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */

    public Bugfix__20221108_fix_issue_with_non_existing_assignments_in_data_from_lis() {
        datasetSubfolder = "bugfix/20221108-fix-issue-with-non-existing-assignments-in-data-from-lis"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */
        analysisName = "test_analysis"; /* you can change this if you want to; but it is not needed */
        tmin = null;
        tmax = null;
    }

    public static void main(String[] args) {
        Bugfix__20221108_fix_issue_with_non_existing_assignments_in_data_from_lis tests = new Bugfix__20221108_fix_issue_with_non_existing_assignments_in_data_from_lis();

        tests.run_interactive__frames_569_to_571();
//        tests.run_interactive();
//        tests.run_trackonly();
//        tests.run_reloading();
//        tests.run_export();
    }

    /**
     * Test-methods are below.
     */
    public void run_interactive__frames_569_to_571() {
        String glSubfolder = "Pos0_GL32";
        tmin = 571;
        tmax = 572;
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, glSubfolder, "20221104_VNG1040_SHU_1_MMStack_Pos0_GL32.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, glSubfolder, "prj_mm_antibio_analysis_1/track_data__prj_mm_antibio_analysis_1/mm.properties");
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }

    public void run_interactive() {
        String glSubfolder = "Pos0_GL32";
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, glSubfolder, "20221104_VNG1040_SHU_1_MMStack_Pos0_GL32.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, glSubfolder, "prj_mm_antibio_analysis_1/track_data__prj_mm_antibio_analysis_1/mm.properties");
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }

//    public void run_trackonly() {
//        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
//        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
//        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-headless", "-p", properties_file_path.toString(), "-analysis", analysisName, "-trackonly"});
//    }
//
//    public void run_reloading() {
//        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder);
//        startMoma(false, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
//    }
//
//    public void run_export() {
//        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder);
//        startMoma(true, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
//    }

}