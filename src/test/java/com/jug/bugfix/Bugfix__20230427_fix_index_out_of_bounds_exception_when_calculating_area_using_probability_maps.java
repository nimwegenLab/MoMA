package com.jug.bugfix;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;


public class Bugfix__20230427_fix_index_out_of_bounds_exception_when_calculating_area_using_probability_maps {
    final String datasetSubfolder;
    String analysisName;
    Integer tmin;
    Integer tmax;

    String datasetsBasePath = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */

    public Bugfix__20230427_fix_index_out_of_bounds_exception_when_calculating_area_using_probability_maps() {
        datasetSubfolder = "bugfix/20230427-fix-index-out-of-bounds-exception-when-calculating-area-using-probability-maps"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */
        analysisName = "test_analysis"; /* you can change this if you want to; but it is not needed */
        tmin = null;
        tmax = 10;
    }

    public static void main(String[] args) {
        Bugfix__20230427_fix_index_out_of_bounds_exception_when_calculating_area_using_probability_maps tests = new Bugfix__20230427_fix_index_out_of_bounds_exception_when_calculating_area_using_probability_maps();

//        tests.run_export_reproduce_error();
//        tests.run_trackonly_isolate_error();
        tests.run_export_isolate_error();
    }

    /**
     * Test-methods are below.
     */
    public void run_export_reproduce_error() {
        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder,"/data/Pos2_GL10");
//        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder);
        analysisName = "prj_mm_antibio_analysis_2";
        startMoma(true, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }

    public void run_trackonly_isolate_error() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder,"/data/Pos2_GL10","20220701_VNG1040_AB2h_4_MMStack_Pos0_Pos2_GL10.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "run_trackonly_isolate_error";
        tmin = 167;
        tmax = 170;
        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-headless", "-p", properties_file_path.toString(), "-analysis", analysisName, "-trackonly"});
    }

    public void run_export_isolate_error() {
//        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder,"/data/Pos2_GL10","20220701_VNG1040_AB2h_4_MMStack_Pos0_Pos2_GL10.tif");
        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder,"/data/Pos2_GL10");
        analysisName = "run_trackonly_isolate_error";
//        startMoma(false, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
        startMoma(true, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }
}