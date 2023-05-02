package com.jug.bugfix;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;


public class Bugfix__20230502_loading_prune_roots_throws_exception {
    final String datasetSubfolder;
    String analysisName;
    Integer tmin;
    Integer tmax;

    String datasetsBasePath = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */

    public Bugfix__20230502_loading_prune_roots_throws_exception() {
        datasetSubfolder = "bugfix/20230502-loading-prune-roots-throws-exception"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */
        analysisName = "test_analysis"; /* you can change this if you want to; but it is not needed */
        tmin = null;
        tmax = 10;
    }

    public static void main(String[] args) {
        Bugfix__20230502_loading_prune_roots_throws_exception tests = new Bugfix__20230502_loading_prune_roots_throws_exception();

        tests.reproduce_export_NullPointerException();
    }

    /**
     * Test-methods are below.
     */
    public void reproduce_export_NullPointerException() {
        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder,"/data/Pos1_GL12");
        analysisName = "20230418_analysis";
        startMoma(true, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }

}