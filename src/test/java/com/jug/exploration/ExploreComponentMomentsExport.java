package com.jug.exploration;

import com.jug.MoMA;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;
import static com.jug.util.JavaUtils.concatenateWithCollection;

public class ExploreComponentMomentsExport {
    String datasets_base_path = "/media/micha/T7/20210816_test_data_michael/Moma/MM_Testing/000_development/feature/20220216-export-pixel-moments/";

    public static void main(String[] args) {
        ExploreComponentMomentsExport tests = new ExploreComponentMomentsExport();
        tests._dany_20200812_8proms_ace_1_MMStack_Pos25_GL7();
    }

    public void _dany_20200812_8proms_ace_1_MMStack_Pos25_GL7() {
        String datasetSubPath = "/dany_20200812_8proms_ace_1_MMStack/Pos25_GL7/";
        String inputPath = datasets_base_path + datasetSubPath + "/20200812_8proms_ace_1_MMStack_Pos25_GL7.tif";
        String outputPath = datasets_base_path + datasetSubPath + "/output/";
        Integer tmin = 0;
        Integer tmax = null;
        startMoma(false, inputPath, outputPath, tmin, tmax, true, new String[]{"-ground_truth_export"});
    }
}