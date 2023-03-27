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
                /* Check files create by MoMA (i.e. not by Gurobi). */
                /* NOTES:
                We ignore the file "moma.log", which contains time-stamps on each line, which makes it difficult to compare.
                Also, it would not add much to compare the log (it is still available for debugging, if something goes wrong).
                */
                () -> assertFileIsUnchanged(expectedTrackDataPath.resolve("mm.properties"), temporaryWorkingDirectory.resolve("mm.properties")),
                () -> assertFileIsUnchanged(expectedTrackDataPath.resolve("assignment_costs.csv"), actualTrackDataPath.resolve("assignment_costs.csv")),
                () -> assertFileIsUnchanged(expectedTrackDataPath.resolve("assignment_filter_intensities.csv"), actualTrackDataPath.resolve("assignment_filter_intensities.csv")),
                () -> assertFileIsUnchanged(expectedTrackDataPath.resolve("assignment_states.csv"), actualTrackDataPath.resolve("assignment_states.csv")),
                () -> assertFileIsUnchanged(expectedTrackDataPath.resolve("assignment_states_initial.csv"), actualTrackDataPath.resolve("assignment_states_initial.csv")),
                () -> assertFileIsUnchanged(expectedTrackDataPath.resolve("component_forests.json"), actualTrackDataPath.resolve("component_forests.json")),
                () -> assertFileIsUnchanged(expectedTrackDataPath.resolve("curation.moma"), actualTrackDataPath.resolve("curation.moma")),
                () -> assertFileIsUnchanged(expectedTrackDataPath.resolve("file_format.json"), actualTrackDataPath.resolve("file_format.json")),
                () -> assertFileIsUnchanged(expectedTrackDataPath.resolve("hypothesis_states.csv"), actualTrackDataPath.resolve("hypothesis_states.csv")),
                () -> assertFileIsUnchanged(expectedTrackDataPath.resolve("hypothesis_states_initial.csv"), actualTrackDataPath.resolve("hypothesis_states_initial.csv")),
                /* Check files create by Gurobi. */
                /* NOTES:
                We ignore files "gurobi_model.lp" amd "gurobi_model.mps", because they are very large so that comparison
                takes very long. Also, the files "gurobi_model.mst" and "gurobi_model.sol" contain a subset of their
                information, which would also be different, if the Gurobi model were different (in other words their
                assertions would fail as well, if assertions of "gurobi_model.lp" amd "gurobi_model.mps" were to fail).

                These would be the corresponding assertions for "gurobi_model.lp" and "gurobi_model.mps":
                () -> assertFileIsUnchanged(expectedTrackDataPath.resolve("gurobi_model.lp"), actualTrackDataPath.resolve("gurobi_model.lp")),
                () -> assertFileIsUnchanged(expectedTrackDataPath.resolve("gurobi_model.mps"), actualTrackDataPath.resolve("gurobi_model.mps")),
                */
                () -> assertFileIsUnchanged(expectedTrackDataPath.resolve("gurobi_model.mst"), actualTrackDataPath.resolve("gurobi_model.mst")),
                () -> assertFileIsUnchanged(expectedTrackDataPath.resolve("gurobi_model.sol"), actualTrackDataPath.resolve("gurobi_model.sol")),
                () -> assertFileIsUnchanged(expectedTrackDataPath.resolve("gurobi_environment.log"), actualTrackDataPath.resolve("gurobi_environment.log"))
        );
    }

    private void assertFileIsUnchanged(Path expectedFile, Path actualFile) throws IOException {
        ExplorationTestHelpers.assertFilesAreEqual(
                expectedFile,
                actualFile,
                0,
                Arrays.asList("#", "GENERATED_BY_MOMA_VERSION", "IMPORT_PATH", "logging started"));
    }
}
