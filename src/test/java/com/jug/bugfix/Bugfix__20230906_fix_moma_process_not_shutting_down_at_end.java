package com.jug.bugfix;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;


public class Bugfix__20230906_fix_moma_process_not_shutting_down_at_end {
    final String datasetSubfolder;
    String analysisName;
    Integer tmin;
    Integer tmax;

    String datasetsBasePath = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */

    public Bugfix__20230906_fix_moma_process_not_shutting_down_at_end() {
        datasetSubfolder = "bugfix/20230906-fix-moma-process-not-shutting-down-at-end"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */
        analysisName = "test_analysis"; /* you can change this if you want to; but it is not needed */
        tmin = null;
        tmax = 10;
    }

    public static void main(String[] args) {
        Bugfix__20230906_fix_moma_process_not_shutting_down_at_end tests = new Bugfix__20230906_fix_moma_process_not_shutting_down_at_end();

        tests.run_trackonly();
    }

    /**
     * Test-methods are below.
     */
    public void run_trackonly() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "mm.properties");
        analysisName = "test_run_from_ide";
        tmax = 5;
        // command below corresponds to command-line call:
        // moma -f -tmax 5 -p /home/micha/Documents/01_work/15_moma_notes/02_moma_development/bugfix/20230906-fix-moma-process-not-shutting-down-at-end/mm.properties -analysis test_run_from_terminal -headless -trackonly -i /home/micha/Documents/01_work/15_moma_notes/02_moma_development/bugfix/20230906-fix-moma-process-not-shutting-down-at-end/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif
        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-headless", "-p", properties_file_path.toString(), "-analysis", analysisName, "-trackonly"});
    }
}