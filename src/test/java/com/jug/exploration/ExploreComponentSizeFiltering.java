package com.jug.exploration;

import com.jug.MoMA;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

import static com.jug.util.JavaUtils.concatenateWithCollection;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;

public class ExploreComponentSizeFiltering {
    String datasets_base_path = "/media/micha/T7/20210816_test_data_michael/Moma/MM_Testing/000_development/feature/20220303-add-component-size-filter-and-make-it-configurable/";

    public static void main(String[] args) {
        ExploreComponentSizeFiltering tests = new ExploreComponentSizeFiltering();

        tests._lis_20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12();
    }

    public void _lis_20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12() {
        String datasetSubPath = "/lis_20211026/Pos7_GL12";
        String inputPath = datasets_base_path + datasetSubPath + "/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif";
        String outputPath = datasets_base_path + datasetSubPath + "/output/";
        Integer tmin = 400;
        Integer tmax = 800;
        startMoma(false, inputPath, outputPath, tmin, tmax, true, new String[]{"-ground_truth_export"});
    }
}