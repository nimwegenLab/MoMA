package com.jug.util.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    public static void createDirectory(String outputPath) {
        File file = new File(outputPath);
        file.mkdir();
    }

    public static void deleteDirectory(Path pathToDirectory) {
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(pathToDirectory.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createEmptyDirectory(Path pathToDirectory){
        File f = pathToDirectory.toFile();
        if (!f.exists()) {
            f.mkdirs();
        }
        deleteDirectoryContent(pathToDirectory);
    }

    public static void deleteDirectoryContent(Path pathToDirectory){
        File f = pathToDirectory.toFile();
        for (File c : f.listFiles())
            deleteRecursively(c);
    }

    private static void deleteRecursively(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                deleteRecursively(c);
        }
        f.delete();
    }

    public static void deleteFiles(List<Path> filesToDelete) {
        for (Path file : filesToDelete) {
            deleteFile(file);
        }
    }

    public static void deleteFile(Path file) {
        if (!file.toFile().delete()) {
            new RuntimeException("Failed to delete: " + file.toFile().getName());
        }
    }

    public static List<Path> getMatchingFilesInDirectory(Path parentFolder, String globExpression) {
//        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globExpression);
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globExpression);
        List<Path> matchingFiles = new ArrayList<>();
        String[] pathnames = new File(parentFolder.toString()).list();
        for (String name : pathnames) {
            Path filePath = Paths.get(parentFolder.toString(), name);
            if (matcher.matches(filePath)) {
                matchingFiles.add(filePath);
            }
        }
        return matchingFiles;
    }
}
