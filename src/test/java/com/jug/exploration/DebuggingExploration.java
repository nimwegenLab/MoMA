package com.jug.exploration;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;

public class DebuggingExploration {
    String datasets_base_path = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development/bugfix/";

    public static void main(String[] args) {
        DebuggingExploration tests = new DebuggingExploration();

        tests._20220810_fix_issue_with_missing_assignments_when_using_growthrate_filtering();
//        tests._20220525_endoftracking_terminator_not_being_written_to_cell_stats_csv_file_1();
//        tests._2020524_fix_issue_with_non_exported_cell_mask__reproduce_issue();
//        tests._2020524_fix_issue_with_non_exported_cell_mask__debug_issue();
    }

    public void _20220810_fix_issue_with_missing_assignments_when_using_growthrate_filtering() {
        String datasetSubPath = "20220810-fix-issue-with-missing-assignments-when-using-growthrate-filtering/";
        Path inputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos1_GL2", "20220701_VNG1040_AB2h_4_MMStack_Pos0_Pos1_GL2.tif");
        Path outputPath = Paths.get(datasets_base_path, datasetSubPath, "output_1/");
        if (!outputPath.toFile().exists()) {
            outputPath.toFile().mkdir();
        }
        Path mmPropertiesPath = Paths.get(datasets_base_path, datasetSubPath, "mm.properties");
        Integer tmin = 1;
        Integer tmax = 30;
        startMoma(false, inputPath.toString(), outputPath.toString(), tmin, tmax, true, new String[]{"-ground_truth_export","-p",mmPropertiesPath.toString()});
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