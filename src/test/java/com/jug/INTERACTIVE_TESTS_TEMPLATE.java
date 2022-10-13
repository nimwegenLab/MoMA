package com.jug;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;


public class INTERACTIVE_TESTS_TEMPLATE {
    private final String debugFolderName;
    private final String analysisName;
    Integer tmin;
    Integer tmax;

    String datasets_base_path = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development/bugfix/";

    public INTERACTIVE_TESTS_TEMPLATE() {
        debugFolderName = "000__debug_template"; /* change this name to the name of your debug-branch without `bugfix/`, which is where you should have created you data-folder for the debugging session */
        analysisName = "debug_analysis"; /* you can change this if you want to; but it is not needed */
        tmin = null;
        tmax = 10;
    }

    public static void main(String[] args) {
        INTERACTIVE_TESTS_TEMPLATE tests = new INTERACTIVE_TESTS_TEMPLATE();

        tests.run_trackonly();
//        tests.run_reloading();
//        tests.run_export();
    }

    /**
     * Actual debugging test-methods are below.
     */
    public void run_trackonly() {
        Path inputPath = Paths.get(datasets_base_path, debugFolderName, "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
        Path properties_file_path = Paths.get(datasets_base_path, debugFolderName, "mm.properties");
        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-headless", "-p", properties_file_path.toString(), "-analysis", analysisName, "-trackonly"});
    }

    public void run_reloading() {
        Path reload_folder_path = Paths.get(debugFolderName);
        startMoma(false, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }

    public void run_export() {
        Path reload_folder_path = Paths.get(debugFolderName);
        startMoma(true, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }

}