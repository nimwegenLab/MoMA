package com.jug.bugfix;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;


public class Bugfix__20230725_fix_csv_position_column_header_for_new_position_naming_scheme {
    final String datasetSubfolder;
    String analysisName;
    Integer tmin;
    Integer tmax;

    String datasetsBasePath = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */

    public Bugfix__20230725_fix_csv_position_column_header_for_new_position_naming_scheme() {
        datasetSubfolder = "bugfix/20230725-fix-csv-position-column-header-for-new-position-naming-scheme"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */
        analysisName = "test_analysis"; /* you can change this if you want to; but it is not needed */
        tmin = null;
        tmax = 10;
    }

    public static void main(String[] args) {
        Bugfix__20230725_fix_csv_position_column_header_for_new_position_naming_scheme tests = new Bugfix__20230725_fix_csv_position_column_header_for_new_position_naming_scheme();

        tests.run_track_and_export();
    }

    /**
     * Test-methods are below.
     */
    public void run_track_and_export() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "wt_zwf_oxi_rep2_1_MMStack__8-Pos001_GL29.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "test_tracking";
        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-headless", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }
}