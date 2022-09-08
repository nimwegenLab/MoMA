package com.jug.exploration;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;
import static com.jug.util.io.FileUtils.createEmptyDirectory;

public class FeatureExploration_20220908_create_assignments_from_ilp_model_during_loading {
    String datasets_base_path = "/home/micha/Documents/01_work/15_moma_notes/02_moma_development/bugfix/";

    public static void main(String[] args) {
        FeatureExploration_20220908_create_assignments_from_ilp_model_during_loading tests = new FeatureExploration_20220908_create_assignments_from_ilp_model_during_loading();

//        tests._feature_exploration_20220908_create_assignments__test_trackonly();
        tests._feature_exploration_20220908_create_assignments__test_exporting();
    }

    public void _feature_exploration_20220908_create_assignments__test_exporting() {
        String datasetSubPath = "20220815-fix-moma-fails-does-not-correctly-restore-ilp-state";
        Path glPath = Paths.get(datasets_base_path, datasetSubPath, "Pos0_GL7");
        String analysisName = "feature_20220908_create_assignments";
//        startMoma(true, null, null, null, null, false, new String[]{"-reload",glPath.toString(),"-analysis",analysisName});
        startMoma(true, null, null, null, null, false, new String[]{"-reload",glPath.toString(),"-analysis",analysisName});
    }

    public void _feature_exploration_20220908_create_assignments__test_trackonly() {
        String datasetSubPath = "20220815-fix-moma-fails-does-not-correctly-restore-ilp-state";
        Path inputPath = Paths.get(datasets_base_path, datasetSubPath, "Pos0_GL7", "20220530_VNG1040_AB2h_1_MMStack_Pos0_GL7.tif");
        Path mmPropertiesPath = Paths.get(datasets_base_path, datasetSubPath, "mm.properties");
        String analysisName = "feature_20220908_create_assignments";
        Integer tmin = null;
        Integer tmax = 10;
        startMoma(true, inputPath.toString(), null, tmin, tmax, false, new String[]{"-p", mmPropertiesPath.toString(), "-analysis", analysisName, "-trackonly"});
    }
}