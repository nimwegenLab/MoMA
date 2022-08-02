package com.jug.logging;

import com.jug.gui.LoggerWindow;
import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Objects;

/**
 * This is a hacky adapter class to allow for logging output to 'System.out' and 'System.err' to disk and the
 * logger-window (implemented in LoggerWindow.class).
 *
 * Currently, there is no logging framework in place to allow for decent handling of logging. Instead, everything is
 * being written to 'System.out' and 'System.err', which was being written to the LoggerWindow. Furthermore, no output
 * of the logs to disk is implemented.
 *
 * The goal of this class is to improve on the situation (as a stop gap) by making the output to
 * 'System.out' and 'System.err' available to other classes.
 */
public class LoggerAdapterForSystemOutErr {
    private LoggerWindow loggerWindow;
    private LoggerToFile fileLogger;

    public LoggerAdapterForSystemOutErr(LoggerWindow loggerWindow, LoggerToFile fileLogger) {
        this.loggerWindow = Objects.requireNonNull(loggerWindow, "loggerWindow is null");
        this.fileLogger = Objects.requireNonNull(fileLogger);
    }

    private PrintStream getSystemOutputStream() {
        return originalSystemOutputStream;
    }

    PrintStream originalSystemOutputStream;

    private OutputStream out;

    public void initialize() {
        originalSystemOutputStream = new PrintStream(System.out);

        out = new OutputStream() {
            @Override
            public void write(final int b) {
                loggerWindow.updateConsoleTextArea(String.valueOf((char) b));
                fileLogger.print(String.valueOf((char) b));
                originalSystemOutputStream.print((char) b);
            }

            @Override
            public void write(@NotNull final byte[] b, final int off, final int len) {
                loggerWindow.updateConsoleTextArea(new String(b, off, len));
                fileLogger.print(new String(b, off, len));
                originalSystemOutputStream.print(new String(b, off, len));
            }

            @Override
            public void write(@NotNull final byte[] b) {
                throw new NotImplementedException();
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    public void print(String toPrint) {
        fileLogger.print(toPrint);
        loggerWindow.updateConsoleTextArea(toPrint);
        getSystemOutputStream().print(toPrint);
    }

    public void println(String toPrint) {
        print(toPrint + "\n");
    }
}
