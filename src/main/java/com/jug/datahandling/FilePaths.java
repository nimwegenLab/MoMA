package com.jug.datahandling;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

public class FilePaths {
    Path propertiesFile;
    private Path inputImagePath;

    public void setLoadingDirectoryPath(String directoryPath) {
        propertiesFile = Paths.get(directoryPath, "mm.properties");
        Files.exists(propertiesFile);
    }

    public Path getPropertiesFile(){
        return propertiesFile;
    }

    public void setInputImagePath(String inputImagePath) {
        this.inputImagePath = Paths.get(inputImagePath);
    }

    public Path getInputImagePath() {
        return inputImagePath;
    }
}
