package com.jug.feature;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;


public class Feature__20230329_add_calculation_of_assignment_cost_using_component_length {
    final String datasetSubfolder;
    String analysisName;
    Integer tmin;
    Integer tmax;

    String datasetsBasePath = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */

    public Feature__20230329_add_calculation_of_assignment_cost_using_component_length() {
        datasetSubfolder = "feature/20230329-add-calculation-of-assignment-cost-using-component-length"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */
        analysisName = "test_analysis"; /* you can change this if you want to; but it is not needed */
        tmin = null;
        tmax = 10;
    }

    public static void main(String[] args) {
        Feature__20230329_add_calculation_of_assignment_cost_using_component_length tests = new Feature__20230329_add_calculation_of_assignment_cost_using_component_length();

        tests.run_interactive__lis__20211026__Pos7_GL12();
    }

    /**
     * Test-methods are below.
     */
    public void run_interactive__lis__20211026__Pos7_GL12() {
        String glSubfolder = "data/lis__20211026__Pos7_GL12";
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, glSubfolder, "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, glSubfolder, "mm.properties");
        analysisName = "output_interactive__lis__20211026__Pos7_GL12";
        tmin = null;
        tmax = 100;
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }

    public void run_interactive__dany__20200812__Pos25_GL7() {
        String glSubfolder = "data/dany__20200812__Pos25_GL7";
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, glSubfolder, "20200812_8proms_ace_1_MMStack_Pos25_GL7.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "output_interactive__dany__20200812__Pos25_GL7";
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }
}