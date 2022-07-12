package com.jug.exploration;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static com.jug.exploration.ExplorationTestHelpers.startMoma;
import static com.jug.util.io.FileUtils.createEmptyDirectory;
import static org.junit.Assert.assertEquals;

public class ExploreResultLoading {
    String datasets_base_path = "/media/micha/T7/data_michael_mell/moma_test_data/000_development/feature/20220121-fix-loading-of-curated-datasets/";

    public static void main(String[] args) throws Exception {
        ExploreResultLoading tests = new ExploreResultLoading();
//        tests._dany_20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16__run_without_mm_properties();
//        tests._dany_20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16__run_from_mm_properties();
//        tests._dany_20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16__test_reloading();
//        tests._20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12__run_without_mm_properties();
//        tests._20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12__test_trackonly();
//        tests._20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12__test_reloading();
//        tests._20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12__test_full_cycle_of_trackonly_then_curation_then_export();
//        tests._20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12__debug_issue_with_headless_run();
//        tests._20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12__component_tree_generation_stability_test();
        tests._20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12__debug_NullPointerException__in__GrowthlaneTrackingILP_recursivelyAddPathBlockingConstraints();
    }

    public void _dany_20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16__run_without_mm_properties() {
        String inputPath = datasets_base_path + "/dany_20200730__Pos3_GL16/20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16.tif";
        String outputPath = datasets_base_path + "/dany_20200730__Pos3_GL16/output/";
        Integer tmin = 70;
        Integer tmax = 80;
        startMoma(false, inputPath, outputPath, tmin, tmax, true, new String[]{"-ground_truth_export"});
    }

    public void _dany_20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16__run_from_mm_properties() {
        String inputPath = datasets_base_path + "/dany_20200730__Pos3_GL16/20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16.tif";
        String outputPath = datasets_base_path + "/dany_20200730__Pos3_GL16/output/";
        String properties_file_path = datasets_base_path + "/dany_20200730__Pos3_GL16/output/mm.properties";
        Integer tmin = 70;
        Integer tmax = 80;
        startMoma(false, inputPath, outputPath, tmin, tmax, true, new String[]{"-ground_truth_export", "-p", properties_file_path});
    }

    public void _dany_20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16__test_reloading() {
        String inputPath = datasets_base_path + "/dany_20200730__Pos3_GL16/20200730_4proms_glu_ez1x_1_MMStack_Pos3_GL16.tif";
        String outputPath = datasets_base_path + "/dany_20200730__Pos3_GL16/output";
        String settings_file_path = datasets_base_path + "/dany_20200730__Pos3_GL16/output/mm.properties";
        String reload_folder_path = datasets_base_path + "/dany_20200730__Pos3_GL16/output";
        Integer tmin = null;
        Integer tmax = null;
//        inputPath = null;
//        outputPath = null;
//        startMoma(true, inputPath, outputPath, tmin, tmax, false, new String[]{"-ground_truth_export", "-p", settings_file_path});
//        startMoma(false, inputPath, outputPath, tmin, tmax, true, new String[]{"-ground_truth_export", "-p", settings_file_path});
        startMoma(false, null, null, null, null, false, new String[]{"-ground_truth_export", "-reload", reload_folder_path});
    }

    public void _20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12__run_without_mm_properties() {
        String subfolder = "lis_20211026__Pos7_GL12";
        Path inputPath = Paths.get(datasets_base_path, subfolder, "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
        Path outputPath = Paths.get(datasets_base_path, subfolder, "output");
        Path properties_file_path = Paths.get(datasets_base_path, subfolder, "mm.properties");
        Integer tmin = null;
        Integer tmax = null;
        createEmptyDirectory(outputPath);
        startMoma(true, inputPath.toString(), outputPath.toString(), tmin, tmax, false, new String[]{"-ground_truth_export", "-p", properties_file_path.toString()});
//        startMoma(false, null, null, null, null, false, new String[]{"-ground_truth_export", "-reload", reload_folder_path});
    }

    public void _20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12__test_trackonly() {
        String subfolder = "lis_20211026__Pos7_GL12";
        Path inputPath = Paths.get(datasets_base_path, subfolder, "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
        Path outputPath = Paths.get(datasets_base_path, subfolder, "output");
        Path properties_file_path = Paths.get(datasets_base_path, subfolder, "mm.properties");
        Integer tmin = 1;
        Integer tmax = 10;
        createEmptyDirectory(outputPath);
        startMoma(true, inputPath.toString(), outputPath.toString(), tmin, tmax, false, new String[]{"-ground_truth_export", "-p", properties_file_path.toString(), "-trackonly"});
    }

    public void _20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12__test_reloading() {
        String subfolder = "lis_20211026__Pos7_GL12";
        Path reload_folder_path = Paths.get(datasets_base_path, subfolder, "output");
        startMoma(false, null, null, null, null, false, new String[]{"-ground_truth_export", "-reload", reload_folder_path.toString()});
    }

    public void _20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12__test_full_cycle_of_trackonly_then_curation_then_export() throws Exception {
        int statusCode;

        String subfolder = "lis_20211026__Pos7_GL12";
        Path inputPath = Paths.get(datasets_base_path, subfolder, "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
        Path outputPath = Paths.get(datasets_base_path, subfolder, "output");
        Path reload_folder_path = Paths.get(datasets_base_path, subfolder, "output");
        Path properties_file_path = Paths.get(datasets_base_path, subfolder, "mm.properties");
        Integer tmin = 1;
        Integer tmax = 10;

        createEmptyDirectory(outputPath);

        /* this runs tracking only */
        statusCode = catchSystemExit(() -> startMoma(true, inputPath.toString(), outputPath.toString(), tmin, tmax, false, new String[]{"-ground_truth_export", "-p", properties_file_path.toString(), "-trackonly"}));
        assertEquals(0, statusCode);

        /* this reloads the dataset for manual curation */
        statusCode = catchSystemExit(() -> startMoma(false, null, null, null, null, false, new String[]{"-ground_truth_export", "-reload", reload_folder_path.toString()}));
        assertEquals(0, statusCode);

        /* this reloads the dastaset in headless to export the tracking results */
        statusCode = catchSystemExit(() -> startMoma(true, null, null, null, null, false, new String[]{"-ground_truth_export", "-reload", reload_folder_path.toString()}));
        assertEquals(0, statusCode);
    }

    public void _20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12__debug_issue_with_headless_run() {
        String subfolder = "lis_20211026__Pos7_GL12";
        Path inputPath = Paths.get(datasets_base_path, subfolder, "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
        int tmax = 10;
        startMoma(true, inputPath.toString(), null, null, tmax, false, new String[]{});
    }

    public void _20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12__component_tree_generation_stability_test() throws Exception {
        String subfolder = "lis_20211026__Pos7_GL12";
        Path inputPath = Paths.get(datasets_base_path, subfolder, "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
        Path outputPath = Paths.get(datasets_base_path, subfolder, "output");
        Path properties_file_path = Paths.get(datasets_base_path, subfolder, "mm.properties");
        Integer tmin = 1;
        Integer tmax = 10;
        int nrOfIterations = 2000;

        createEmptyDirectory(outputPath);
        int statusCode = catchSystemExit(() -> startMoma(true, inputPath.toString(), outputPath.toString(), tmin, tmax, false, new String[]{"-ground_truth_export", "-p", properties_file_path.toString(), "-trackonly"}));
        assertEquals(0, statusCode);

        for (int i = 0; i < nrOfIterations; i++) {
//            statusCode = catchSystemExit(() -> System.exit(0));
            /* this reloads the dastaset in headless to export the tracking results */
            statusCode = catchSystemExit(() -> startMoma(true, null, null, null, null, false, new String[]{"-ground_truth_export", "-reload", outputPath.toString()}));
            assertEquals(0, statusCode);
            System.out.println("############# Finished iteration: " + i + " of " + nrOfIterations + " #############");
        }
        System.out.println("############# Finished test. #############");
    }

    public void _20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12__debug_NullPointerException__in__GrowthlaneTrackingILP_recursivelyAddPathBlockingConstraints() {
        String subfolder = "lis_20211026__Pos7_GL12";
        Path inputPath = Paths.get(datasets_base_path, subfolder, "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif");
        Path outputPath = Paths.get(datasets_base_path, subfolder, "output");
        Path properties_file_path = Paths.get(datasets_base_path, subfolder, "mm.properties");
        Integer tmin = 400;
        Integer tmax = 401;
        createEmptyDirectory(outputPath);
        startMoma(true, inputPath.toString(), outputPath.toString(), tmin, tmax, false, new String[]{"-ground_truth_export", "-p", properties_file_path.toString()});
//        startMoma(false, null, null, null, null, false, new String[]{"-ground_truth_export", "-reload", reload_folder_path});
    }

}
