package com.jug;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;


public class TEMPLATE_CLASS_FOR_INTERACTIVE_TESTING {
    private final String datasetSubfolder;
    private final String analysisName;
    Integer tmin;
    Integer tmax;

    String datasetsBasePath = "TEMPLATE::BASE_PATH_TO_FOLDER_WITH_TEST_DATASETS"; /* DO NOT CHANGE: value is overwritten by the script start_session.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */

    public TEMPLATE_CLASS_FOR_INTERACTIVE_TESTING() {
        datasetSubfolder = "TEMPLATE::RELATIVE_PATH_TO_TEST_DATASET_SUBFOLDER"; /* DO NOT CHANGE: value is overwritten by the script start_session.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */
        analysisName = "test_analysis"; /* you can change this if you want to; but it is not needed */
        tmin = null;
        tmax = 10;
    }

    public static void main(String[] args) {
        TEMPLATE_CLASS_FOR_INTERACTIVE_TESTING tests = new TEMPLATE_CLASS_FOR_INTERACTIVE_TESTING();

        tests.run_trackonly();
//        tests.run_reloading();
//        tests.run_export();
    }

    /**
     * Test-methods are below.
     */
    public void run_trackonly() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-headless", "-p", properties_file_path.toString(), "-analysis", analysisName, "-trackonly"});
    }

    public void run_reloading() {
        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder);
        startMoma(false, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }

    public void run_export() {
        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder);
        startMoma(true, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }

}