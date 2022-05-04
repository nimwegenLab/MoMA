package com.jug.datahandling;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

public class FilePaths {
    Path propertiesFile;

    public void setLoadingDirectoryPath(String directoryPath) {
        propertiesFile = Paths.get(directoryPath, "mm.properties");
        Files.exists(propertiesFile);
    }

    public Path getPropertiesFile(){
        return propertiesFile;
    }
}
