package com.jug.datahandling;

import com.jug.util.Hash;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.jug.util.io.FileUtils.getMatchingFilesInDirectory;
import static java.util.Objects.isNull;
import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.scijava.util.FileUtils.deleteRecursively;

public class GlFileManager implements IGlExportFilePathGetter, IGlExportFilePathSetter {
    private Path globalPropertiesFile;
    private Path inputImagePath;
    private Path outputPath;
    private String modelFile;
    private String analysisName;

    public void setLoadingDirectoryPath(Path directoryPath) {
        outputPath = directoryPath;
    }

    public void setAnalysisName(String analysisName) {
        this.analysisName = analysisName;
    }

    public String getAnalysisName() { /* TODO-MM-20220728: If this 'analysisName' is null, then we should return a generated name here; I still need to think about how to best do this */
        if(isNull(analysisName)){
            return temporaryAnalysisName;
        }
        return analysisName;
    }

    public void setGlobalPropertiesFile(Path globalPropertiesFile) {
        this.globalPropertiesFile = globalPropertiesFile;
    }

    public Path getGlobalPropertiesFile(){
        return globalPropertiesFile;
    } /* TODO-MM-20220728: Most likely, we need to distinguish here between the path to the default properties file in the user-home and the one that we will store to...: */

    public Path getAnalysisPropertiesFile() {
        return Paths.get(getTrackingDataOutputPath().toString(), "mm.properties");
//        throw new NotImplementedException("This should return the properties file of the current GL analysis.");
//        return globalPropertiesFile;
    } /* TODO-MM-20220728: Most likely, we need to distinguish here between the path to the default properties file in the user-home and the one that we will store to...: */

    public void setInputImagePath(Path inputImagePath) {
        this.inputImagePath = inputImagePath;
    }

    public Path getInputImageParentDirectoryPath() {
        return getInputImagePath().getParent();
    }

    @Override
    public Path getInputImagePath() {
        return inputImagePath;
    }

    @Override
    public void setOutputPath(Path outputPath) {
        this.outputPath = outputPath;
    }

    public Path getOutputPath() {
        if (isNull(outputPath)) {
            outputPath = Paths.get(getInputImageParentDirectoryPath().toString(), getAnalysisName());
        }
        return outputPath;
    }

    private String temporaryAnalysisName = ".~analysisTmp";

    public Path getTrackingDataOutputPath() {
        return Paths.get(getOutputPath().normalize().toString(), "track_data__" + getAnalysisName());
    }

    public boolean trackingDataOutputPathExists() {
        return getTrackingDataOutputPath().toFile().exists();
    }

    public Path getExportOutputPath() {
        return Paths.get(getOutputPath().normalize().toString(), "export_data__" + getAnalysisName());
    }

    public void makeTrackingDataOutputDirectory() {
        if (getTrackingDataOutputPath().toFile().exists()) {
            return;
        }
        if(!getTrackingDataOutputPath().toFile().mkdirs()){
            throw new RuntimeException("Could not create the output directory: " + getTrackingDataOutputPath());
        }
    }

    String htmlOutputFileBaseName = "CellTracksOverview";

    @Override
    public File getHtmlIndexFilePath() {
        return Paths.get(getExportOutputPath().toString(), htmlOutputFileBaseName + "__" + getInputTiffFileName() + ".html").toFile();
    }

    @Override
    public File getHtmlImageDirectoryPath() {
        File file = Paths.get(getExportOutputPath().toString(), htmlOutputFileBaseName + "__" + getInputTiffFileName()).toFile();
        if (file.exists()) {
            return file;
        }
        if (!file.mkdirs()) {
            throw new RuntimeException("Could not create the output directory: " + file.getAbsolutePath());
        }
        return file;
    }

    @Override
    public void makeExportDataOutputDirectory() {
        if (getExportOutputPath().toFile().exists()) {
            return;
        }
        if(!getExportOutputPath().toFile().mkdirs()){
            throw new RuntimeException("Could not create the output directory: " + getExportOutputPath());
        }
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

    @Override
    public Path getCellTracksFilePath() {
        String filename = "CellTracks__" + getInputTiffFileName() + ".csv";
        return Paths.get(getExportOutputPath().toString(), filename);
    }

    @Override
    public Path getCellStatsFilePath() {
        String filename = "CellStats__" + getInputTiffFileName() + ".csv";
        return Paths.get(getExportOutputPath().toString(), filename);
    }

    @Override
    public Path getAssignmentCostsFilePath() {
        String filename = "AssignmentCosts__" + getInputTiffFileName() + ".csv";
        return Paths.get(getExportOutputPath().toString(), filename);
    }

    @Override
    public Path getCellMaskImageFilePath() {
        String filename = "CellMasks__" + getInputTiffFileName() + ".tif";
        return Paths.get(getExportOutputPath().toString(), filename);
    }

    @Override
    public Path getGroundTruthFrameListFilePath() {
        String filename = "GroundTruthFrames__" + getInputTiffFileName() + ".csv";
        return Paths.get(getExportOutputPath().toString(), filename);
    }

    @NotNull
    public String getProbabilityImageFilePath() {
        Path outputFolderPath = getTrackingDataOutputPath();
        return outputFolderPath + "/probability_maps.tif";
    }

    @NotNull
    private String getInputTiffFileName() {
        File file = new File(getInputImagePath().toString());
        return removeExtension(file.getName());
    }

    public void saveProbabilityImage(Img<FloatType> probabilityMap) {
        makeTrackingDataOutputDirectory();
        ImagePlus tmp_image = ImageJFunctions.wrap(probabilityMap, "probability_maps");
        IJ.saveAsTiff(tmp_image, getProbabilityImageFilePath());
    }

    public Path getDotMomaFilePath() {
        List<Path> matchingFiles = getMatchingFilesInDirectory(getTrackingDataOutputPath(), "**/*.moma");
        if (matchingFiles.size() == 0) {
            throw new RuntimeException("Error: Could not find *.moma file in GL-directory: " + getTrackingDataOutputPath());
        }
        if (matchingFiles.size() > 1) {
            throw new RuntimeException("Error: It is unclear, which *.moma file should be loaded. The number of *.moma files >1 in GL-directory: " + getTrackingDataOutputPath());
        }
        return matchingFiles.get(0);
    }

    private final String modelFileName = "gurobi_model";

    @Override
    public boolean gurobiMpsFileExists() {
        return getGurobiMpsFilePath().toFile().exists();
    }

    @Override
    public Path getGurobiEnvironmentLogFilePath() {
        return Paths.get(getTrackingDataOutputPath().toString(), "gurobi_environment.log");
    }

    @Override
    public File getMomaLogFile() {
        makeTrackingDataOutputDirectory();
        File file = Paths.get(getTrackingDataOutputPath().toString(), "moma.log").toFile();
        if(file.exists()){
            return file;
        }
        try {
            file.createNewFile();
            return file;
        } catch (IOException e) {
            throw new RuntimeException("Could not create file: " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public File getAssignmentFilterIntensitiesCsvFile() {
        return Paths.get(getTrackingDataOutputPath().toString(), "assignment_filter_intensities.csv").toFile();
    }

    @Override
    public File getComponentTreeJsonFile() {
        return Paths.get(getTrackingDataOutputPath().toString(), "component_forests.json").toFile();
    }

    @Override
    public Path getGurobiMpsFilePath() {
        return Paths.get(getTrackingDataOutputPath().toString(), modelFileName + ".mps");
    }

    @Override
    public Path getGurobiLpFilePath() {
        return Paths.get(getTrackingDataOutputPath().toString(), modelFileName + ".lp");
    }

    @Override
    public Path getGurobiSolFilePath() {
        return Paths.get(getTrackingDataOutputPath().toString(), modelFileName + ".sol");
    }

    @Override
    public Path getGurobiMstFilePath() {
        return Paths.get(getTrackingDataOutputPath().toString(), modelFileName + ".mst");
    }

    @Override
    public Path getCurationStatsFilePath() {
        return Paths.get(getTrackingDataOutputPath().toString(), "curation.moma");
    }

    @Override
    public Path getFileFormatFilePath() {
        return Paths.get(getTrackingDataOutputPath().toString(), "file_format.json");
    }

    public Path getMmPropertiesOutputFilePath() {
        return Paths.get(getTrackingDataOutputPath().toString(), "mm.properties");
    }

    public void deleteTrackingDataOutputPath() {
        deleteRecursively(getTrackingDataOutputPath().toFile());
    }

    @Override
    public void createFile(File file) {
        if (file.exists()) {
            return;
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("Could not create file: " + file.getAbsolutePath(), e);
        }
    }
}
