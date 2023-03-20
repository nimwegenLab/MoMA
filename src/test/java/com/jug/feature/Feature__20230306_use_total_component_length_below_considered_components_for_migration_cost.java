package com.jug.feature;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;


public class Feature__20230306_use_total_component_length_below_considered_components_for_migration_cost {
    final String datasetSubfolder;
    String analysisName;
    Integer tmin;
    Integer tmax;

    String datasetsBasePath = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */

    public Feature__20230306_use_total_component_length_below_considered_components_for_migration_cost() {
        datasetSubfolder = "feature/20230306-use-total-component-length-below-considered-components-for-migration-cost"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */
        analysisName = "test_analysis"; /* you can change this if you want to; but it is not needed */
        tmin = null;
        tmax = 10;
    }

    public static void main(String[] args) {
        Feature__20230306_use_total_component_length_below_considered_components_for_migration_cost tests = new Feature__20230306_use_total_component_length_below_considered_components_for_migration_cost();

//        tests.run_interactive__lis_20221102_2_29();
//        tests.run_interactive__lis_20220701_5_5();
        tests.run_trackonly__lis_20220701_5_5();
    }

    /**
     * Test-methods are below.
     */
    public void run_interactive__lis_20221102_2_29() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "/data/lis_20221102_2_29/20221102_VNG1040_SHU_1_MMStack_Pos2_GL29.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "/data/lis_20221102_2_29/mm.properties");
        analysisName = "test_enter_assignment";
        tmin = 6;
        tmax = 30;
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }

    public void run_interactive__lis_20220701_5_5() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "/data/lis_20220701_5_5/20220701_VNG1040_AB2h_4_MMStack_Pos0_Pos5_GL5.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "/data/lis_20220701_5_5/mm.properties");
        analysisName = "test_enter_assignment";
        tmin = null;
        tmax = 10;
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }

    public void run_trackonly__lis_20220701_5_5() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "/data/lis_20220701_5_5/20220701_VNG1040_AB2h_4_MMStack_Pos0_Pos5_GL5.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "/data/lis_20220701_5_5/mm.properties");
        analysisName = "test_batch_run";
        tmin = null;
        tmax = 10;
        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-headless", "-p", properties_file_path.toString(), "-analysis", analysisName, "-trackonly"});
    }
}