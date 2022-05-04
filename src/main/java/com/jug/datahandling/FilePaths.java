package com.jug.datahandling;

import com.jug.util.Hash;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.commons.io.FilenameUtils.removeExtension;

public class FilePaths {
    Path propertiesFile;
    private Path inputImagePath;
    private Path outputPath;
    private String modelFile;

    public void setLoadingDirectoryPath(String directoryPath) {
        propertiesFile = Paths.get(directoryPath, "mm.properties");
        outputPath = Paths.get(directoryPath);
        Files.exists(propertiesFile);
    }

    public void setPropertiesFile(Path propertiesFile) {
        this.propertiesFile = propertiesFile;
    }
    public Path getPropertiesFile(){
        return propertiesFile;
    }

    public void setInputImagePath(Path inputImagePath) {
        this.inputImagePath = inputImagePath;
    }

    public Path getInputImagePath() {
        return inputImagePath;
    }

    public void setOutputPath(Path outputPath) {
        this.outputPath = outputPath;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public String getModelChecksum() {
        return calculateModelChecksum(modelFile);
    }

    public void setModelFilePath(String modelFilePath) {
        modelFile = modelFilePath;
    }

    private String calculateModelChecksum(String modelFile) {
        byte[] hash = Hash.SHA256.checksum(new File(modelFile));
        return Hash.toHex(hash).toLowerCase();
    }

    @NotNull
    public String getProbabilityImageFilePath() {
        String checksum = getModelChecksum();
        File file = new File(getInputImagePath().toString());
        String filename = removeExtension(file.getName());
        String outputFolderPath = getOutputPath().toString();
        String processedImageFileName = outputFolderPath + "/" + filename + "__model_" + checksum + ".tif";
        return processedImageFileName;
    }
}
