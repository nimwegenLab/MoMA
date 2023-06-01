package com.jug.feature;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;


public class Feature__20230601_make_MAX_CELL_DROP_configurable_from_mm_properties {
    final String datasetSubfolder;
    String analysisName;
    Integer tmin;
    Integer tmax;

    String datasetsBasePath = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */

    public Feature__20230601_make_MAX_CELL_DROP_configurable_from_mm_properties() {
        datasetSubfolder = "feature/20230601-make-MAX_CELL_DROP-configurable-from-mm-properties/data/Pos0_GL9"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */
        analysisName = "test_analysis"; /* you can change this if you want to; but it is not needed */
        tmin = 100;
        tmax = 110;
    }

    public static void main(String[] args) {
        Feature__20230601_make_MAX_CELL_DROP_configurable_from_mm_properties tests = new Feature__20230601_make_MAX_CELL_DROP_configurable_from_mm_properties();

        tests.run_interactive();
    }

    /**
     * Test-methods are below.
     */
    public void run_interactive() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "20220320_VNG1040_AB2h_1_Frame0-478_resaved_MMStack_Pos0_Pos0_GL9.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "test_interactive";
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }
}