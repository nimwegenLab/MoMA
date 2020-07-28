package com.jug;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class MomaCurationComparison {
    String outputFolder = "output_deepmoma";

    public static void main(String[] args){
        String datasetBasePath = "/home/micha/Documents/01_work/11_funding_applications/20200725_opo_freenovation_followup_funding/00_moma_performance_analysis/00_datasets/20190730_rpsB_rrnB_plac_6300_glu_gly_4_chr_rrnB/";

        MomaCurationComparison tests = new MomaCurationComparison();

//        tests.start_test(datasetBasePath + "20190730_rpsB_rrnB_plac_6300_glu_gly_4_MMStack_Pos15_preproc_GL01_curated/", "cropped_20190730_rpsB_rrnB_plac_6300_glu_gly_4_MMStack_Pos15_preproc_GL01.tif");

//        tests.start_test(datasetBasePath + "20190730_rpsB_rrnB_plac_6300_glu_gly_4_MMStack_Pos15_preproc_GL02_curated/", "cropped_20190730_rpsB_rrnB_plac_6300_glu_gly_4_MMStack_Pos15_preproc_GL02.tif");

//        tests.start_test(datasetBasePath + "20190730_rpsB_rrnB_plac_6300_glu_gly_4_MMStack_Pos15_preproc_GL03_curated/", "cropped_20190730_rpsB_rrnB_plac_6300_glu_gly_4_MMStack_Pos15_preproc_GL03.tif");

//        tests.start_test(datasetBasePath + "20190730_rpsB_rrnB_plac_6300_glu_gly_4_MMStack_Pos15_preproc_GL04_curated/", "cropped_20190730_rpsB_rrnB_plac_6300_glu_gly_4_MMStack_Pos15_preproc_GL04.tif");

//        tests.start_test(datasetBasePath + "20190730_rpsB_rrnB_plac_6300_glu_gly_4_MMStack_Pos15_preproc_GL05_curated/", "cropped_20190730_rpsB_rrnB_plac_6300_glu_gly_4_MMStack_Pos15_preproc_GL05.tif");

//        tests.start_test(datasetBasePath + "20190730_rpsB_rrnB_plac_6300_glu_gly_4_MMStack_Pos15_preproc_GL08_curated/", "cropped_20190730_rpsB_rrnB_plac_6300_glu_gly_4_MMStack_Pos15_preproc_GL08.tif");

//        tests.start_test(datasetBasePath + "20190730_rpsB_rrnB_plac_6300_glu_gly_4_MMStack_Pos15_preproc_GL11_curated/", "cropped_20190730_rpsB_rrnB_plac_6300_glu_gly_4_MMStack_Pos15_preproc_GL11.tif");

        tests.start_test(datasetBasePath + "20190730_rpsB_rrnB_plac_6300_glu_gly_4_MMStack_Pos16_preproc_GL02_curated/", "cropped_20190730_rpsB_rrnB_plac_6300_glu_gly_4_MMStack_Pos16_preproc_GL02.tif");
    }

    public void start_test(String datasetFolder, String imageFilename) {
        String inputPath = datasetFolder + "/" + imageFilename;
        String outputPath = datasetFolder + "/" + outputFolder;
        startMoma(inputPath, outputPath);
    }

//    @Test
//    public void Pos15_Gl01(String datasetFolder, String imageFilename) {
//        String datasetFolder = "20190730_rpsB_rrnB_plac_6300_glu_gly_4_MMStack_Pos15_preproc_GL01_curated/";
//        String imageFile = "cropped_20190730_rpsB_rrnB_plac_6300_glu_gly_4_MMStack_Pos15_preproc_GL01.tif";
//        String inputPath = datasetBasePath + datasetFolder + imageFile;
//        String outputPath = datasetBasePath + datasetFolder;
//        startMoma(inputPath, outputPath);
//    }
//
//    @Test
//    public void Pos15_Gl02() {
//        String datasetFolder = "20190730_rpsB_rrnB_plac_6300_glu_gly_4_MMStack_Pos15_preproc_GL01_curated/";
//        String imageFile = "cropped_20190730_rpsB_rrnB_plac_6300_glu_gly_4_MMStack_Pos15_preproc_GL01.tif";
//        String inputPath = datasetBasePath + datasetFolder + imageFile;
//        String outputPath = datasetBasePath + datasetFolder;
//        startMoma(inputPath, outputPath);
//    }

    private void startMoma(String inputPath, String outputPath){
        Path path = Paths.get(outputPath);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            System.err.println("ERROR: Could not create output directory: " + outputPath);
        }
        String[] args = new String[]{"-i", inputPath, "-o", outputPath};
        MoMA moma = new MoMA();
        moma.HEADLESS = false;
        MoMA.main(args);
    }

    /**
     * Delete preexisting probability maps. During testing, we often want to test the generation
     * of the probability maps, which are cached to disk and loaded, if they exist for a given model.
     * This function removes those cached files to always run the U-Net preprocessing.
     * @param path
     */
    private void remove_probability_maps(String path) {
        PathMatcher matcher =
                FileSystems.getDefault().getPathMatcher("glob:*__model_*.tif*");
        File f = new File(path);

        String[] pathnames = f.list();
        for (String name : pathnames) {
            String filePath = path + "/" + name;
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