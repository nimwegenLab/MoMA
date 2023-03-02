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

        tests.run_interactive__lis_20221102_2_29();
    }

    /**
     * Test-methods are below.
     */
    public void run_interactive__lis_20221102_2_29() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "/data/lis_20221102_2_29/20221102_VNG1040_SHU_1_MMStack_Pos2_GL29.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "/data/lis_20221102_2_29/mm.properties");
        analysisName = "examine_tracking_errors_1";
        tmin = null;
        tmax = 30;
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }
}