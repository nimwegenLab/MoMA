package com.jug;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;


public class Feature__20221027_make_migration_cost_optional {
    final String datasetSubfolder;
    String analysisName;
    Integer tmin;
    Integer tmax;

    String datasetsBasePath = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development"; /* DO NOT CHANGE: value is overwritten by the script start_session.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */

    public Feature__20221027_make_migration_cost_optional() {
        datasetSubfolder = "feature/20221027-make-migration-cost-optional"; /* DO NOT CHANGE: value is overwritten by the script start_session.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */
        analysisName = "test_analysis"; /* you can change this if you want to; but it is not needed */
        tmin = null;
        tmax = 10;
    }

    public static void main(String[] args) {
        Feature__20221027_make_migration_cost_optional tests = new Feature__20221027_make_migration_cost_optional();

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