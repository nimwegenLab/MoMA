package com.jug.datahandling;

import java.io.File;
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

    Path getFileFormatFilePath();

    Path getCurationStatsFilePath();

    boolean gurobiMpsFileExists();

    Path getGurobiEnvironmentLogFilePath();

    File getMomaLogFile();

    void makeTrackingDataOutputDirectory();

    File getHtmlIndexFilePath();

    File getHtmlImageDirectoryPath();

    File getComponentTreeJsonFile();

    File assignmentFilterIntensityInformation();

    void createFile(File file);
}
