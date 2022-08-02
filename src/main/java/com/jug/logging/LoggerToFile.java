package com.jug.logging;

import java.io.*;
import java.util.Objects;
import java.util.function.Supplier;

public class LoggerToFile {
    private Supplier<File> logFileSupplier;

    public LoggerToFile(Supplier<File> logFileSupplier) {
        this.logFileSupplier = Objects.requireNonNull(logFileSupplier, "exportFilePaths is null");
    }

    public void printlnWithDateTime(String toPrint){
        if (toPrint.isEmpty()) {
            print(toPrint + "\n"); /* do not prepend date-time to empty lines */
            return;
        }
        print(DateTimeProvider.getDateTime() + "\t" + toPrint + "\n");
    }

    public void printWithDateTime(String toPrint){
        if (toPrint.isEmpty()) {
            print(toPrint); /* do not prepend date-time to empty lines */
            return;
        }
        print(DateTimeProvider.getDateTime() + "\t" + toPrint);
    }

    public void println(String toPrint){
        print(toPrint + "\n");
    }

    public void print(String toPrint) {
        try {
            File logFile = logFileSupplier.get();
            FileWriter fileWriter = new FileWriter(logFile, true);
            BufferedWriter writer = new BufferedWriter(fileWriter);
            writer.append(toPrint);
            writer.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
