package com.jug.logging;

import com.jug.datahandling.IGlExportFilePathGetter;

import java.io.*;
import java.util.Objects;
import java.util.function.Supplier;

public class LoggerToFile {
    private Supplier<File> logFileSupplier;

    public LoggerToFile(Supplier<File> logFileSupplier) {
        this.logFileSupplier = Objects.requireNonNull(logFileSupplier, "exportFilePaths is null");
    }

    public void print(String toPrint) {
        try {
//            exportFilePaths.makeTrackingDataOutputDirectory();
//            exportFilePaths.getMomaLogFile().toFile().createNewFile();
            File logFile = logFileSupplier.get();
            PrintWriter fileStream = new PrintWriter(logFile);
            fileStream.append("this is some test output\n");
//            fileStream.append(toPrint);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
