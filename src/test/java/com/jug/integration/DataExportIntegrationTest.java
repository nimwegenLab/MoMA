package com.jug.integration;

import com.jug.exploration.ExplorationTestHelpers;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;

public class DataExportIntegrationTest {

    @TempDir
    Path temporaryWorkingDirectory;

    @Test
    public void tracking_and_exporting_yields_same_result() throws IOException {
        Path testDataSourcePath = Paths.get(new File("").getAbsolutePath(), "src/test/resources/test/integration/lis_20221102_2_29/");

        FileUtils.copyDirectory(testDataSourcePath.toFile(), temporaryWorkingDirectory.toFile());
        System.out.println(String.format("Test working directory: %s", temporaryWorkingDirectory));

        Path inputImagePath = Paths.get(temporaryWorkingDirectory.toString(), "20221102_VNG1040_SHU_1_MMStack_Pos2_GL29.tif");
        Path propertiesFilePath = Paths.get(temporaryWorkingDirectory.toString(), "mm.properties");
        Path referenceDataPath = Paths.get(temporaryWorkingDirectory.toString(), "reference_output_data");
        String analysisName = "test_output";
        Integer tmin = null;
        Integer tmax = null;

        startMoma(true, inputImagePath.toString(), null, tmin, tmax, false, new String[]{"-f", "-headless", "-p", propertiesFilePath.toString(), "-analysis", analysisName});
    }
}
