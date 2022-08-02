package com.jug.logging;

import com.jug.datahandling.IGlExportFilePathGetter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;

public class LoggerToFile {
    private IGlExportFilePathGetter exportFilePaths;

    public LoggerToFile(IGlExportFilePathGetter exportFilePaths) {
        this.exportFilePaths = Objects.requireNonNull(exportFilePaths, "exportFilePaths is null");
    }

    public void print(String toPrint) {
        try {
            exportFilePaths.makeTrackingDataOutputDirectory();
            exportFilePaths.getMomaLogFile().toFile().createNewFile();
            PrintWriter fileStream = new PrintWriter(exportFilePaths.getMomaLogFile().toFile());
            fileStream.append("this is some test output\n");
//            fileStream.append(toPrint);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
