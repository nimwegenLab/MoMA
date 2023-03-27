package com.jug.integration;

import com.jug.exploration.ExplorationTestHelpers;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.jug.exploration.ExplorationTestHelpers.startMoma;
import static org.junit.jupiter.api.Assertions.assertAll;

public class DataExportIntegrationTest {

    @TempDir
    Path temporaryWorkingDirectory;

    static String CACHE_DIR_PROPERTY_KEY = "imagej.tensorflow.models.dir";

    @Test
    public void tracking_and_exporting_yields_same_result() throws IOException {
        /* ARRANGE */
        Path testDataSourcePath = Paths.get(new File("").getAbsolutePath(), "src/test/resources/test/integration/lis_20221102_2_29/");

        FileUtils.copyDirectory(testDataSourcePath.toFile(), temporaryWorkingDirectory.toFile());
        System.out.println(String.format("Test working directory: %s", temporaryWorkingDirectory));

        System.setProperty(CACHE_DIR_PROPERTY_KEY, temporaryWorkingDirectory.toString());

        Path inputImagePath = Paths.get(temporaryWorkingDirectory.toString(), "20221102_VNG1040_SHU_1_MMStack_Pos2_GL29.tif");
        Path propertiesFilePath = Paths.get(temporaryWorkingDirectory.toString(), "mm.properties");
        Path pathToExpectedData = Paths.get(temporaryWorkingDirectory.toString(), "expected_output");
        String analysisName = "output";

        Path expectedTrackDataPath = pathToExpectedData.resolve("track_data__output");
        Path actualTrackDataPath = temporaryWorkingDirectory.resolve(Paths.get(analysisName, "track_data__output"));

        Integer tmin = null;
        Integer tmax = null;

        /* ACT */
        startMoma(true, inputImagePath.toString(), null, tmin, tmax, false, new String[]{"-f", "-headless", "-p", propertiesFilePath.toString(), "-analysis", analysisName});

        /* ASSERT */
        assertAll(
                () -> assertFileIsUnchanged(expectedTrackDataPath.resolve("mm.properties"), temporaryWorkingDirectory.resolve("mm.properties")),
                () -> assertFileIsUnchanged(expectedTrackDataPath.resolve("assignment_costs.csv"), actualTrackDataPath.resolve("assignment_costs.csv"))
        );
    }

    private void assertFileIsUnchanged(Path expectedFile, Path actualFile) throws IOException {
        ExplorationTestHelpers.assertFilesAreEqual(
                expectedFile,
                actualFile,
                0,
                Arrays.asList("#", "GENERATED_BY_MOMA_VERSION", "IMPORT_PATH"));
    }
}
