package com.jug.datahandling;

import java.nio.file.Path;

public interface IGlExportFilePaths {
    Path getOutputPath();

    Path getGurobiMpsFilePath();

    Path getGurobiLpFilePath();

    Path getGurobiSolFilePath();

    Path getGurobiMstFilePath();
}
