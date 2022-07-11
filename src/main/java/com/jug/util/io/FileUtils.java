package com.jug.util.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

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
}
