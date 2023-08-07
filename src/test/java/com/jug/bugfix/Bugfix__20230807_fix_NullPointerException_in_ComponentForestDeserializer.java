package com.jug.bugfix;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;


public class Bugfix__20230807_fix_NullPointerException_in_ComponentForestDeserializer {
    final String datasetSubfolder;
    String analysisName;
    Integer tmin;
    Integer tmax;

    String datasetsBasePath = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */

    public Bugfix__20230807_fix_NullPointerException_in_ComponentForestDeserializer() {
        datasetSubfolder = "bugfix/20230807-fix-NullPointerException-in-ComponentForestDeserializer"; /* DO NOT CHANGE: value is overwritten by the script start_topic_branch.sh, which uses this template to create a session for e.g. feature-development or bug-fixing */
        analysisName = "test_analysis"; /* you can change this if you want to; but it is not needed */
        tmin = null;
        tmax = 10;
    }

    public static void main(String[] args) {
        Bugfix__20230807_fix_NullPointerException_in_ComponentForestDeserializer tests = new Bugfix__20230807_fix_NullPointerException_in_ComponentForestDeserializer();

//        tests.run_trackonly__1_Pos001_GL53();
//        tests.run_export__1_Pos001_GL53__2();
//        tests.run_interactive__1_Pos001_GL53();
//        tests.run_export__1_Pos001_GL53();
//        tests.run_export__1_Pos006_GL16();
        tests.run_interactive__1_Pos006_GL16();
    }

    /**
     * Test-methods are below.
     */
    public void run_interactive__1_Pos001_GL53() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "1-Pos001_GL53", "wt_zwf_oxi_rep2_1_MMStack__1-Pos001_GL53.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "1-Pos001_GL53", "mm.properties");
        analysisName = "test_interactive";
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }

    public void run_trackonly__1_Pos001_GL53() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "1-Pos001_GL53", "wt_zwf_oxi_rep2_1_MMStack__1-Pos001_GL53.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "1-Pos001_GL53", "mm.properties");
        analysisName = "implement_handle_empty_growthlane_frames";
        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-headless", "-f", "-p", properties_file_path.toString(), "-analysis", analysisName, "-trackonly"});
    }

    public void run_export__1_Pos001_GL53__2() {
        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder, "1-Pos001_GL53");
        analysisName = "implement_handle_empty_growthlane_frames";
        startMoma(true, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }

    public void run_export__1_Pos001_GL53() {
        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder, "1-Pos001_GL53");
        analysisName = "slurm_test_3";
        startMoma(true, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }

    public void run_interactive__1_Pos006_GL16() {
        Path inputPath = Paths.get(datasetsBasePath, datasetSubfolder, "1-Pos006_GL16", "wt_zwf_oxi_rep2_1_MMStack__1-Pos006_GL16.tif");
        Path properties_file_path = Paths.get(datasetsBasePath, datasetSubfolder, "1-Pos006_GL16", "mm.properties");
        analysisName = "test_interactive";
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", properties_file_path.toString(), "-analysis", analysisName});
    }

    public void run_export__1_Pos006_GL16() {
        Path reload_folder_path = Paths.get(datasetsBasePath, datasetSubfolder, "1-Pos006_GL16");
        analysisName = "slurm_test_3";
        startMoma(true, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }
}