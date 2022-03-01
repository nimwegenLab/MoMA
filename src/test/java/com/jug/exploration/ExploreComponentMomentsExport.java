package com.jug.exploration;

import com.jug.MoMA;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

import static com.jug.util.JavaUtils.concatenateWithCollection;

public class ExploreComponentMomentsExport {
    String datasets_base_path = "/media/micha/T7/20210816_test_data_michael/Moma/MM_Testing/";

    public static void main(String[] args) {
        ExploreComponentMomentsExport tests = new ExploreComponentMomentsExport();
        tests._dany_20200812_8proms_ace_1_MMStack_Pos25_GL22();
    }

    public void _dany_20200812_8proms_ace_1_MMStack_Pos25_GL22() {
        String inputPath = datasets_base_path + "/001_moma_debugging/20200812_8proms_ace_1/Pos25_GL22/20200812_8proms_ace_1_MMStack_Pos25_GL22.tif";
        String outputPath = datasets_base_path + "/001_moma_debugging/20200812_8proms_ace_1/Pos25_GL22/output/";
        Integer tmin = 0;
        Integer tmax = 100;
        startMoma(false, inputPath, outputPath, tmin, tmax, true, new String[]{"-ground_truth_export"});
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