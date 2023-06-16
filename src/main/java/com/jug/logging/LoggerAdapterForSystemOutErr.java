package com.jug.logging;

import com.jug.gui.LoggerWindow;
import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Objects;

/**
 * This adapter class allows logging output to 'System.out' and 'System.err' to disk and the
 * logger-window (implemented in LoggerWindow.class).
 *
 * Currently, there is no logging framework in place to allow for decent handling of logging. Instead, everything is
 * being written to 'System.out' and 'System.err', which was being written to the LoggerWindow. Furthermore, no output
 * of the logs to disk is implemented.
 *
 * The goal of this class is to improve on the situation (as a stop gap) by making the output to
 * 'System.out' and 'System.err' available to other logging classes (and hence write it to disk).
 */
public class LoggerAdapterForSystemOutErr {
    private LoggerWindow loggerWindow;
    private LoggerToFile fileLogger;

    public LoggerAdapterForSystemOutErr(LoggerWindow loggerWindow, LoggerToFile fileLogger) {
        this.loggerWindow = Objects.requireNonNull(loggerWindow, "loggerWindow is null");
        this.fileLogger = Objects.requireNonNull(fileLogger, "fileLogger is null");
    }

    private PrintStream getSystemOutputStream() {
        return originalSystemOutputStream;
    }

    PrintStream originalSystemOutputStream;

    private OutputStream out;

    public void initialize() {
        System.out.println("Breakpoint 7");

        originalSystemOutputStream = new PrintStream(System.out);

        System.out.println("Breakpoint 8");

        out = new OutputStream() {
            @Override
            public void write(final int b) {
                loggerWindow.updateConsoleTextArea(String.valueOf((char) b));
                fileLogger.printWithDateTime(String.valueOf((char) b));
                originalSystemOutputStream.print((char) b);
            }

            @Override
            public void write(@NotNull final byte[] b, final int off, final int len) {
                loggerWindow.updateConsoleTextArea(new String(b, off, len));
                fileLogger.printWithDateTime(new String(b, off, len));
                originalSystemOutputStream.print(new String(b, off, len));
            }

            @Override
            public void write(@NotNull final byte[] b) {
                throw new NotImplementedException();
            }
        };

        System.out.println("Breakpoint 9");

        System.setOut(new PrintStream(out, true));

        System.out.println("Breakpoint 10");

        System.setErr(new PrintStream(out, true));

        System.out.println("Breakpoint 11");
    }

    public void print(String toPrint) {
        fileLogger.printWithDateTime(toPrint);
        loggerWindow.updateConsoleTextArea(toPrint + "\n");
        getSystemOutputStream().print(toPrint + "\n");
    }
}
