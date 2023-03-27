package com.jug.integration;

import com.jug.exploration.ExplorationTestHelpers;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NotImplementedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;

public class DataExportIntegrationTest {

    @TempDir
    Path temporaryWorkingDirectory;

    static String CACHE_DIR_PROPERTY_KEY = "imagej.tensorflow.models.dir";

    @Test
    public void tracking_and_exporting_yields_same_result() throws IOException {
        Path testDataSourcePath = Paths.get(new File("").getAbsolutePath(), "src/test/resources/test/integration/lis_20221102_2_29/");

        FileUtils.copyDirectory(testDataSourcePath.toFile(), temporaryWorkingDirectory.toFile());
        System.out.println(String.format("Test working directory: %s", temporaryWorkingDirectory));

        System.setProperty(CACHE_DIR_PROPERTY_KEY, temporaryWorkingDirectory.toString());

        Path inputImagePath = Paths.get(temporaryWorkingDirectory.toString(), "20221102_VNG1040_SHU_1_MMStack_Pos2_GL29.tif");
        Path propertiesFilePath = Paths.get(temporaryWorkingDirectory.toString(), "mm.properties");
        Path referenceDataPath = Paths.get(temporaryWorkingDirectory.toString(), "reference_output_data");
        String analysisName = "test_output";
        Integer tmin = null;
        Integer tmax = null;

        startMoma(true, inputImagePath.toString(), null, tmin, tmax, false, new String[]{"-f", "-headless", "-p", propertiesFilePath.toString(), "-analysis", analysisName});

        Path trackDataPath = Paths.get(referenceDataPath.toString(), "track_data__test_output");
        Path mmpropertiesActual = trackDataPath.resolve("mm.properties");
        Path mmpropertiesExpected = temporaryWorkingDirectory.resolve("mm.properties");
        ExplorationTestHelpers.compareTextFilesByLine(
                mmpropertiesExpected,
                mmpropertiesActual,
                0,
                Arrays.asList("#", "GENERATED_BY_MOMA_VERSION"),
                true);

        throw new NotImplementedException("test not finished.");
    }
}
