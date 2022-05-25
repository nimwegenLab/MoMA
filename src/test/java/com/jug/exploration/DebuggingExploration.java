package com.jug.exploration;

import com.jug.MoMA;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

import static com.jug.util.JavaUtils.concatenateWithCollection;
import static java.util.Objects.isNull;

public class DebuggingExploration {
    String datasets_base_path = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development/bugfix/";

    public static void main(String[] args) {
        DebuggingExploration tests = new DebuggingExploration();

        tests._2020524_fix_issue_with_non_exported_cell_mask__reproduce_issue();
//        tests._2020524_fix_issue_with_non_exported_cell_mask__debug_issue();
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

    public void _2020524_fix_issue_with_non_exported_cell_mask__debug_issue() {
        String datasetSubPath = "2020524-fix-issue-with-non-exported-cell-mask/";
        Path inputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos3_GL25", "20220320_VNG1040_AB2h_1_Frame0-478_resaved_MMStack_Pos0_Pos3_GL25.tif");
        Path outputPath = Paths.get(datasets_base_path, datasetSubPath, "output/");
        Path mmPropertiesPath = Paths.get(datasets_base_path, datasetSubPath, "mm.properties");
        Integer tmin = 240;
        Integer tmax = 260;
//        startMoma(false, inputPath.toString(), outputPath.toString(), tmin, tmax, true, new String[]{"-ground_truth_export"});
        startMoma(false, inputPath.toString(), outputPath.toString(), tmin, tmax, true, new String[]{"-ground_truth_export","-p", mmPropertiesPath.toString()});
    }

    private void startMoma(boolean headless, String inputPath, String outputPath, Integer tmin, Integer tmax, boolean deleteProbabilityMaps, String[] additionalArgs) {
        if (deleteProbabilityMaps) {
            remove_probability_maps(inputPath);
        }
        create_output_folder(outputPath);

        String[] args;

        if (tmin != null && tmax != null) {
            args = new String[]{"-i", inputPath, "-o", outputPath, "-tmin", tmin.toString(), "-tmax", tmax.toString()};
        } else if (tmin != null && tmax == null) {
            args = new String[]{"-i", inputPath, "-o", outputPath, "-tmin", tmin.toString()};
        } else if (tmin == null && tmax != null) {
            args = new String[]{"-i", inputPath, "-o", outputPath, "-tmax", tmax.toString()};
        } else { // both tmin and tmax are null
            args = new String[]{"-i", inputPath, "-o", outputPath};
        }
        if (additionalArgs != null) {
            args = concatenateWithCollection(args, additionalArgs);
        }
        MoMA.HEADLESS = headless;
        MoMA.main(args);
    }

    private void create_output_folder(String outputPath) {
        File file = new File(outputPath);
        file.mkdir();
    }

    /**
     * Delete preexisting probability maps. During testing, we often want to test the generation
     * of the probability maps, which are cached to disk and loaded, if they exist for a given model.
     * This function removes those cached files to always run the U-Net preprocessing.
     *
     * @param path
     */
    private void remove_probability_maps(String path) {
        PathMatcher matcher =
                FileSystems.getDefault().getPathMatcher("glob:*__model_*.tif*");
        File f = new File(path);
        File parentFolder = new File(f.getParent());

        String[] pathnames = parentFolder.list();
        if(isNull(pathnames)) { return; }
        for (String name : pathnames) {
            String filePath = parentFolder + "/" + name;
            if (matcher.matches(Paths.get(name))) {
                System.out.print(filePath);
                File f2 = new File(filePath);
                if (f2.delete())                      //returns Boolean value
                {
                    System.out.println("Deleted: " + f.getName());   //getting and printing the file name
                } else {
                    System.out.println("Failed to delete: " + f.getName());
                }
            }
        }
    }
}