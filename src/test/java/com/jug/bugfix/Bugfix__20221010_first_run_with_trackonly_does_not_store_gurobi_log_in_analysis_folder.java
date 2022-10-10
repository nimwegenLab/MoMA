package com.jug.bugfix;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;


public class Bugfix__20221010_first_run_with_trackonly_does_not_store_gurobi_log_in_analysis_folder {
    String datasets_base_path = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development/bugfix/";

    public static void main(String[] args) {
        Bugfix__20221010_first_run_with_trackonly_does_not_store_gurobi_log_in_analysis_folder tests = new Bugfix__20221010_first_run_with_trackonly_does_not_store_gurobi_log_in_analysis_folder();

        tests.run_trackonly();
//        tests.run_reloading();
    }

    /**
     * This method template serves quickly create a test-function based on the test-dataset template locate here:
     * /home/micha/Documents/01_work/15_moma_notes/02_moma_development/bugfix/000__debug_template
     */
//    public void __debug_test_method_template__() {
//        String subfolder = "000__debug_template";
//        Path inputPath = Paths.get(datasets_base_path, subfolder, "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
//        Path properties_file_path = Paths.get(datasets_base_path, subfolder, "mm.properties");
//        Integer tmin = null;
//        Integer tmax = 10;
//        String analysisName = "CHANGE_THIS";
//        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-headless", "-p", properties_file_path.toString(), "-analysis", analysisName, "-trackonly"});
//    }


    /**
     * Actual debugging test-methods are below.
     */
    public void run_trackonly() {
        String subfolder = "20221010-first-run-with-trackonly-does-not-store-gurobi-log-in-analysis-folder";
        Path inputPath = Paths.get(datasets_base_path, subfolder, "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
        Path properties_file_path = Paths.get(datasets_base_path, subfolder, "mm.properties");
        Integer tmin = null;
        Integer tmax = 10;
        String analysisName = "20221010-fix-gurobi-log-creation";
        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-headless", "-p", properties_file_path.toString(), "-analysis", analysisName, "-trackonly"});
    }

    public void run_reloading() {
        String subfolder = "20221010-first-run-with-trackonly-does-not-store-gurobi-log-in-analysis-folder";
        String analysisName = "20221010-fix-gurobi-log-creation";
        Path reload_folder_path = Paths.get(datasets_base_path, subfolder);
        startMoma(false, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }
}