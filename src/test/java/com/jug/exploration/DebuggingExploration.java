package com.jug.exploration;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;
import static com.jug.util.io.FileUtils.createEmptyDirectory;

public class DebuggingExploration {
    String datasets_base_path = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development/bugfix/";

    public static void main(String[] args) {
        DebuggingExploration tests = new DebuggingExploration();

        tests._20221010_first_run_with_trackonly_does_not_store_gurobi_log_in_analysis_folder__test_trackonly();
//        tests._20221010_first_run_with_trackonly_does_not_store_gurobi_log_in_analysis_folder__test_reloading();

//        tests._20220919_fix_exception_when_hovering_components_while_ilp_is_not_ready__track_only();

//        tests._20220912_fix_IndexOutOfBoundsException_when_saving_tracking__run_ui();
//        tests._20220912_fix_IndexOutOfBoundsException_when_saving_tracking__run_trackonly();

//        tests._20220905_force_gurobi_exception_during_model_loading__test_trackonly();
//        tests._20220905_force_gurobi_exception_during_model_loading__test_exporting();

//        tests._20220905_fix_incorrect_export_folder_path__test_trackonly();
//        tests._20220905_fix_incorrect_export_folder_path__test_exporting();

//        tests._20220817_debug_missing_assignments_and_components__test_4__Pos27_GL16();
//        tests._20220817_debug_missing_assignments_and_components__test_3__Pos17_GL31();
//        tests._20220817_debug_missing_assignments_and_components__test_2__Pos0_GL7();
//        tests._20220817_debug_missing_assignments_and_components__test_1__Pos0_GL7();
//        tests._test_version_output();
//        tests._20220816_fix_opt_range_slider();
//        tests._20220815_fix_moma_fails_does_not_correctly_restore_ilp_state__test_interactive_tracking();
//        tests._20220815_fix_moma_fails_does_not_correctly_restore_ilp_state__test_interactive_tracking_reload();

//        tests._20220815_fix_moma_fails_does_not_correctly_restore_ilp_state__test_trackonly();
//        tests._20220815_fix_moma_fails_does_not_correctly_restore_ilp_state__test_reloading();

//        tests._20220815_fix_moma_fails_does_not_correctly_restore_ilp_state__test_reloading_scicore_tracking_result();
//        tests._20220811_fix_issue_with_continuity_constraint_violation();
//        tests._20220810_fix_issue_with_missing_assignments();
//        tests._20220525_endoftracking_terminator_not_being_written_to_cell_stats_csv_file_1();
//        tests._2020524_fix_issue_with_non_exported_cell_mask__reproduce_issue();
//        tests._2020524_fix_issue_with_non_exported_cell_mask__debug_issue();
    }

    /**
     * This method template serves quickly create a test-function based on the test-dataset template locate here:
     * /home/micha/Documents/01_work/15_moma_notes/02_moma_development/bugfix/000__debug_template
     */
//    public void __debug_test_method_template__() {
//        String subfolder = "000__debug_template";
//        Path inputPath = Paths.get(datasets_base_path, subfolder, "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
//        Path properties_file_path = Paths.get(datasets_base_path, subfolder, "mm.properties");
//        Integer tmin = null;
//        Integer tmax = 10;
//        String analysisName = "CHANGE_THIS";
//        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-headless", "-p", properties_file_path.toString(), "-analysis", analysisName, "-trackonly"});
//    }


    /**
     * Actual debugging test-methods are below.
     */
    public void _20221010_first_run_with_trackonly_does_not_store_gurobi_log_in_analysis_folder__test_trackonly() {
        String subfolder = "20221010-first-run-with-trackonly-does-not-store-gurobi-log-in-analysis-folder";
        Path inputPath = Paths.get(datasets_base_path, subfolder, "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
        Path properties_file_path = Paths.get(datasets_base_path, subfolder, "mm.properties");
        Integer tmin = null;
        Integer tmax = 10;
        String analysisName = "20221010-fix-gurobi-log-creation";
        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-headless", "-p", properties_file_path.toString(), "-analysis", analysisName, "-trackonly"});
    }

    public void _20221010_first_run_with_trackonly_does_not_store_gurobi_log_in_analysis_folder__test_reloading() {
        String subfolder = "20221010-first-run-with-trackonly-does-not-store-gurobi-log-in-analysis-folder";
        String analysisName = "20221010-fix-gurobi-log-creation";
        Path reload_folder_path = Paths.get(datasets_base_path, subfolder);
        startMoma(false, null, null, null, null, false, new String[]{"-analysis", analysisName, "-reload", reload_folder_path.toString()});
    }

    public void _20220919_fix_exception_when_hovering_components_while_ilp_is_not_ready__track_only() {
        String datasetSubPath = "20220919-fix-exception-when-hovering-components-while-ilp-is-not-ready";
        Path inputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos0_GL28", "20220817_VNG1040_AB30min_1_MMStack_Pos0_Pos0_GL28.tif");
        Path mmPropertiesPath = Paths.get(datasets_base_path, datasetSubPath, "mm.properties");
        String analysisName = "20220919-fix-exception-when-hovering-components";
        Integer tmin = null;
        Integer tmax = 20;
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", mmPropertiesPath.toString(), "-analysis", analysisName});
    }


    public void _20220912_fix_IndexOutOfBoundsException_when_saving_tracking__run_ui() {
        String datasetSubPath = "20220912-fix-IndexOutOfBoundsException-when-saving-tracking";
        Path glPath = Paths.get(datasets_base_path, datasetSubPath, "Pos0_GL28");
        String analysisName = "20220912-fix-IndexOutOfBoundsException";
//        startMoma(true, null, null, null, null, false, new String[]{"-reload",glPath.toString(),"-analysis",analysisName});
        startMoma(false, null, null, null, null, false, new String[]{"-reload",glPath.toString(),"-analysis",analysisName});
    }

    public void _20220912_fix_IndexOutOfBoundsException_when_saving_tracking__run_trackonly() {
        String datasetSubPath = "20220912-fix-IndexOutOfBoundsException-when-saving-tracking";
        Path inputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos0_GL28", "20220817_VNG1040_AB30min_1_MMStack_Pos0_Pos0_GL28.tif");
        Path mmPropertiesPath = Paths.get(datasets_base_path, datasetSubPath, "mm.properties");
        String analysisName = "20220912-fix-IndexOutOfBoundsException";
        Integer tmin = null;
        Integer tmax = 20;
        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-f", "-p", mmPropertiesPath.toString(), "-analysis", analysisName, "-trackonly"});
    }


    public void _20220905_force_gurobi_exception_during_model_loading__test_exporting() {
        String datasetSubPath = "20220815-fix-moma-fails-does-not-correctly-restore-ilp-state";
        Path glPath = Paths.get(datasets_base_path, datasetSubPath, "Pos0_GL7");
        String analysisName = "debug_analysis__20220905_fix_incorrect_export_folder_path";
//        startMoma(true, null, null, null, null, false, new String[]{"-reload",glPath.toString(),"-analysis",analysisName});
        startMoma(true, null, null, null, null, false, new String[]{"-reload",glPath.toString(),"-analysis",analysisName});
    }

    public void _20220905_force_gurobi_exception_during_model_loading__test_trackonly() {
        String datasetSubPath = "20220815-fix-moma-fails-does-not-correctly-restore-ilp-state";
        Path inputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos0_GL7", "20220530_VNG1040_AB2h_1_MMStack_Pos0_GL7.tif");
        Path mmPropertiesPath = Paths.get(datasets_base_path, datasetSubPath, "mm.properties");
        String analysisName = "debug_analysis__20220905_fix_incorrect_export_folder_path";
        Integer tmin = null;
        Integer tmax = 10;
        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-p", mmPropertiesPath.toString(), "-analysis", analysisName, "-trackonly"});
    }

    public void _20220905_fix_incorrect_export_folder_path__test_exporting() {
        String datasetSubPath = "20220815-fix-moma-fails-does-not-correctly-restore-ilp-state";
        Path glPath = Paths.get(datasets_base_path, datasetSubPath, "Pos0_GL7");
        String analysisName = "debug_analysis_3";
//        startMoma(true, null, null, null, null, false, new String[]{"-reload",glPath.toString(),"-analysis",analysisName});
        startMoma(true, null, null, null, null, false, new String[]{"-reload",glPath.toString(),"-analysis",analysisName});
    }

    public void _20220905_fix_incorrect_export_folder_path__test_trackonly() {
        String datasetSubPath = "20220815-fix-moma-fails-does-not-correctly-restore-ilp-state";
        Path inputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos0_GL7", "20220530_VNG1040_AB2h_1_MMStack_Pos0_GL7.tif");
        Path mmPropertiesPath = Paths.get(datasets_base_path, datasetSubPath, "mm.properties");
        String analysisName = "debug_analysis_3";
        Integer tmin = null;
        Integer tmax = 10;
        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-p", mmPropertiesPath.toString(), "-analysis", analysisName, "-trackonly"});
    }

    public void _20220817_debug_missing_assignments_and_components__test_4__Pos27_GL16() {
        String datasetSubPath = "20220817-debug-missing-assignments-and-components/gl_test_case_3";
        Path inputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos27_GL16", "20220810_glc_scpm_1_MMStack_Pos27_GL16.tif");
        Path mmPropertiesPath = Paths.get(datasets_base_path, datasetSubPath, "mm.properties");
        String analysisName = "debug_test_analysis";
        Path outputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos27_GL16", analysisName);
        createEmptyDirectory(outputPath);
        Integer tmin = null;
        Integer tmax = null;
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-p", mmPropertiesPath.toString(), "-analysis", analysisName, "-force"});
    }

    public void _20220817_debug_missing_assignments_and_components__test_3__Pos17_GL31() {
        String datasetSubPath = "20220817-debug-missing-assignments-and-components/gl_test_case_2";
        Path inputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos17_GL31", "20220810_glc_scpm_1_MMStack_Pos17_GL31.tif");
        Path mmPropertiesPath = Paths.get(datasets_base_path, datasetSubPath, "mm.properties");
        String analysisName = "debug_test_analysis";
        Path outputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos17_GL31", analysisName);
        createEmptyDirectory(outputPath);
        Integer tmin = null;
        Integer tmax = null;
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-p", mmPropertiesPath.toString(), "-analysis", analysisName, "-force"});
    }

    public void _20220817_debug_missing_assignments_and_components__test_2__Pos0_GL7() {
        String datasetSubPath = "20220817-debug-missing-assignments-and-components/gl_test_case_1";
        Path inputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos0_GL7", "20220530_VNG1040_AB2h_1_MMStack_Pos0_GL7.tif");
        Path mmPropertiesPath = Paths.get(datasets_base_path, datasetSubPath, "mm.properties");
        String analysisName = "debug_test_analysis";
        Path outputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos0_GL7", analysisName);
        createEmptyDirectory(outputPath);
        int tmin = 130;
        int tmax = 180;
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-p", mmPropertiesPath.toString(), "-analysis", analysisName, "-force"});
    }

    public void _20220817_debug_missing_assignments_and_components__test_1__Pos0_GL7() {
        String datasetSubPath = "20220817-debug-missing-assignments-and-components/gl_test_case_1";
        Path inputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos0_GL7", "20220530_VNG1040_AB2h_1_MMStack_Pos0_GL7.tif");
        Path mmPropertiesPath = Paths.get(datasets_base_path, datasetSubPath, "mm.properties");
        String analysisName = "debug_test_analysis";
        Path outputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos0_GL7", analysisName);
        createEmptyDirectory(outputPath);
        Integer tmin = null;
        Integer tmax = null;
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-p", mmPropertiesPath.toString(), "-analysis", analysisName, "-force"});
    }

    public void _test_version_output() {
        startMoma(false, null, null, null, null, false, new String[]{"-version"});
    }

    public void _20220816_fix_opt_range_slider() {
        String datasetSubPath = "20220816-fix-opt-range-slider";
        Path inputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos0_GL7", "20220530_VNG1040_AB2h_1_MMStack_Pos0_GL7.tif");
        Path mmPropertiesPath = Paths.get(datasets_base_path, datasetSubPath, "mm.properties");
        String analysisName = "debug_test_analysis";
        Path outputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos0_GL7", analysisName);
        createEmptyDirectory(outputPath);
        Integer tmin = null;
        Integer tmax = null;
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-p", mmPropertiesPath.toString(), "-analysis", analysisName, "-force"});
    }

    public void _20220815_fix_moma_fails_does_not_correctly_restore_ilp_state__test_interactive_tracking_reload() {
        String datasetSubPath = "20220815-fix-moma-fails-does-not-correctly-restore-ilp-state";
        Path glPath = Paths.get(datasets_base_path, datasetSubPath, "Pos0_GL7");
        String analysisName = "debug_test_analysis_2";
        startMoma(false, null, null, null, null, false, new String[]{"-reload", glPath.toString(), "-analysis", analysisName});
    }

    public void _20220815_fix_moma_fails_does_not_correctly_restore_ilp_state__test_interactive_tracking() {
        String datasetSubPath = "20220815-fix-moma-fails-does-not-correctly-restore-ilp-state";
        Path inputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos0_GL7", "20220530_VNG1040_AB2h_1_MMStack_Pos0_GL7.tif");
        Path mmPropertiesPath = Paths.get(datasets_base_path, datasetSubPath, "mm.properties");
        String analysisName = "debug_test_analysis_2";
        Path outputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos0_GL7", analysisName);
        createEmptyDirectory(outputPath);
        Integer tmin = 1;
        Integer tmax = 50;
        startMoma(false, inputPath.toString(), null, tmin, tmax, false, new String[]{"-p", mmPropertiesPath.toString(), "-analysis", analysisName, "-force"});
    }

    public void _20220815_fix_moma_fails_does_not_correctly_restore_ilp_state__test_trackonly() {
        String datasetSubPath = "20220815-fix-moma-fails-does-not-correctly-restore-ilp-state";
        Path inputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos0_GL7", "20220530_VNG1040_AB2h_1_MMStack_Pos0_GL7.tif");
        Path mmPropertiesPath = Paths.get(datasets_base_path, datasetSubPath, "mm.properties");
        String analysisName = "debug_test_analysis_1";
        Integer tmin = null;
        Integer tmax = 10;
        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-p", mmPropertiesPath.toString(), "-analysis", analysisName, "-trackonly"});
    }

    public void _20220815_fix_moma_fails_does_not_correctly_restore_ilp_state__test_reloading() {
        String datasetSubPath = "20220815-fix-moma-fails-does-not-correctly-restore-ilp-state";
//        Path inputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos0_GL7", "20220530_VNG1040_AB2h_1_MMStack_Pos0_GL7.tif");
        Path glPath = Paths.get(datasets_base_path, datasetSubPath, "Pos0_GL7");
        String analysisName = "debug_test_analysis_1";
//        Path outputPath = Paths.get(datasets_base_path, datasetSubPath, "output");
//        if (!outputPath.toFile().exists()) {
//            outputPath.toFile().mkdir();
//        }
//        Path mmPropertiesPath = Paths.get(datasets_base_path, datasetSubPath, "mm.properties");
        startMoma(false, null, null, null, null, false, new String[]{"-reload",glPath.toString(),"-analysis",analysisName});
    }

    public void _20220815_fix_moma_fails_does_not_correctly_restore_ilp_state__test_reloading_scicore_tracking_result() {
        String datasetSubPath = "20220815-fix-moma-fails-does-not-correctly-restore-ilp-state";
//        Path inputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos0_GL7", "20220530_VNG1040_AB2h_1_MMStack_Pos0_GL7.tif");
        Path glPath = Paths.get(datasets_base_path, datasetSubPath, "Pos0_GL7");
//        Path outputPath = Paths.get(datasets_base_path, datasetSubPath, "output");
//        if (!outputPath.toFile().exists()) {
//            outputPath.toFile().mkdir();
//        }
//        Path mmPropertiesPath = Paths.get(datasets_base_path, datasetSubPath, "mm.properties");
        Integer tmin = null;
        Integer tmax = null;
        startMoma(false, null, null, null, null, false, new String[]{"-reload",glPath.toString(),"-analysis","prj_mm_antibio__analysis_1"});
    }

    public void _20220811_fix_issue_with_continuity_constraint_violation() {
        String datasetSubPath = "20220811-fix-issue-with-continuity-constraint-violation";
        Path inputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos1_GL2", "20220701_VNG1040_AB2h_4_MMStack_Pos0_Pos1_GL2.tif");
        Path outputPath = Paths.get(datasets_base_path, datasetSubPath, "output");
        if (!outputPath.toFile().exists()) {
            outputPath.toFile().mkdir();
        }
        Path mmPropertiesPath = Paths.get(datasets_base_path, datasetSubPath, "mm.properties");
        Integer tmin = 24;
        Integer tmax = 26;
        startMoma(false, inputPath.toString(), outputPath.toString(), tmin, tmax, true, new String[]{"-p",mmPropertiesPath.toString()});
    }

    public void _20220810_fix_issue_with_missing_assignments() {
        String datasetSubPath = "20220810-fix-issue-with-missing-assignments";
        Path inputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos1_GL2", "20220701_VNG1040_AB2h_4_MMStack_Pos0_Pos1_GL2.tif");
        Path outputPath = Paths.get(datasets_base_path, datasetSubPath, "output");
        if (!outputPath.toFile().exists()) {
            outputPath.toFile().mkdir();
        }
        Path mmPropertiesPath = Paths.get(datasets_base_path, datasetSubPath, "mm.properties");
        Integer tmin = 244;
        Integer tmax = 245;
        startMoma(false, inputPath.toString(), outputPath.toString(), tmin, tmax, true, new String[]{"-p",mmPropertiesPath.toString()});
    }

    public void _20220525_endoftracking_terminator_not_being_written_to_cell_stats_csv_file_1() {
        String datasetSubPath = "20220525-endoftracking-terminator-not-being-written-to-cell-stats-csv-file/";
        Path inputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos3_GL25", "20220320_VNG1040_AB2h_1_Frame0-478_resaved_MMStack_Pos0_Pos3_GL25.tif");
        Path outputPath = Paths.get(datasets_base_path, datasetSubPath, "output_1/");
        Path mmPropertiesPath = Paths.get(datasets_base_path, datasetSubPath, "mm.properties");
        Integer tmin = 249;
        Integer tmax = 252;
//        startMoma(false, inputPath.toString(), outputPath.toString(), tmin, tmax, true, new String[]{"-ground_truth_export","-p", mmPropertiesPath.toString()});
        startMoma(true, inputPath.toString(), outputPath.toString(), tmin, tmax, true, new String[]{"-ground_truth_export"});
    }

    public void _2020524_fix_issue_with_non_exported_cell_mask__reproduce_issue() {
        String datasetSubPath = "2020524-fix-issue-with-non-exported-cell-mask/";
        Path inputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos3_GL25", "20220320_VNG1040_AB2h_1_Frame0-478_resaved_MMStack_Pos0_Pos3_GL25.tif");
        Path outputPath = Paths.get(datasets_base_path, datasetSubPath, "reproduce_issue/");
        Path mmPropertiesPath = Paths.get(datasets_base_path, datasetSubPath, "mm.properties");
        Integer tmin = 249;
        Integer tmax = 252;
//        startMoma(false, inputPath.toString(), outputPath.toString(), tmin, tmax, true, new String[]{"-ground_truth_export","-p", mmPropertiesPath.toString()});
        startMoma(true, inputPath.toString(), outputPath.toString(), tmin, tmax, true, new String[]{"-ground_truth_export"});
    }
}