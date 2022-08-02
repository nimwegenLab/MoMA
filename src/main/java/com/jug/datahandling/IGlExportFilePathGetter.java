package com.jug.datahandling;

import java.nio.file.Path;

public interface IGlExportFilePathGetter {
    Path getInputImagePath();

    Path getOutputPath();

    Path getMmPropertiesOutputFilePath();

    void makeExportDataOutputDirectory();

    Path getCellTracksFilePath();

    Path getCellStatsFilePath();

    Path getAssignmentCostsFilePath();

    Path getCellMaskImageFilePath();

    Path getGroundTruthFrameListFilePath();

    Path getGurobiMpsFilePath();

    Path getGurobiLpFilePath();

    Path getGurobiSolFilePath();

    Path getGurobiMstFilePath();

    Path getCurationStatsFilePath();

    boolean gurobiMpsFileExists();

    Path getGurobiEnvironmentLogFilePath();

    Path getMomaLogFile();

    void makeTrackingDataOutputDirectory();
}
