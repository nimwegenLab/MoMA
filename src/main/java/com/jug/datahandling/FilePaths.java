package com.jug.datahandling;

import com.jug.util.Hash;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.*;

import static org.apache.commons.io.FilenameUtils.removeExtension;

public class FilePaths {
    Path propertiesFile;
    private Path inputImagePath;
    private Path outputPath;
    private String directoryPath;
    private String modelFile;
    private Path gurobiMpsFilePath;

    private Path dotMomaFilePath;

    public void setLoadingDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
        propertiesFile = Paths.get(this.directoryPath, "mm.properties");
        outputPath = Paths.get(this.directoryPath);
        Files.exists(propertiesFile);
        gurobiMpsFilePath = Paths.get(this.directoryPath, "gurobi_model.mps");
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

    public Path getGurobiMpsFilePath() {
        return gurobiMpsFilePath;
    }

    public Path getDotMomaFilePath() {
        dotMomaFilePath = Paths.get(this.directoryPath, "*.moma");

//        String glob = "glob:" + this.directoryPath + "/*.moma";
        String glob = "glob:**/*.moma";
//        String path = "D:/";
//        match(glob, path);
        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(glob);
//        Path path = null;
        if (pathMatcher.matches(Paths.get(this.directoryPath))) {
            int bla = 1;
        }
        return dotMomaFilePath;
    }


}
