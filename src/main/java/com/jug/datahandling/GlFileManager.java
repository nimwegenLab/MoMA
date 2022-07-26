package com.jug.datahandling;

import com.jug.util.Hash;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.jug.util.io.FileUtils.getMatchingFilesInDirectory;
import static org.apache.commons.io.FilenameUtils.removeExtension;

public class GlFileManager {
    Path propertiesFile;
    private Path inputImagePath;
    private Path outputPath;
    private String directoryPath;
    private String modelFile;
    private Path gurobiMpsFilePath;

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

    private void makeOutputDirectory() {
        getOutputPath().toFile().mkdirs();
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

    public void saveProbabilityImage(Img<FloatType> probabilityMap) {
        makeOutputDirectory();
        ImagePlus tmp_image = ImageJFunctions.wrap(probabilityMap, "probability_maps");
        IJ.saveAsTiff(tmp_image, getProbabilityImageFilePath());
    }

    public Path getGurobiMpsFilePath() {
        return gurobiMpsFilePath;
    }

    public Path getDotMomaFilePath() {
        List<Path> matchingFiles = getMatchingFilesInDirectory(Paths.get(this.directoryPath), "**/*.moma");
        if (matchingFiles.size() > 1) {
            if (matchingFiles.size() == 0) {
                throw new RuntimeException("Error: Could not find *.moma file in GL-directory: " + this.directoryPath);
            }
            throw new RuntimeException("Error: It is unclear, which *.moma file should be loaded. The number of *.moma files >1 in GL-directory: " + this.directoryPath);
        }
        return matchingFiles.get(0);
    }
}
