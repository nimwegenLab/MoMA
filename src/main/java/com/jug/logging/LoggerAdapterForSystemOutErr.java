package com.jug.logging;

import com.jug.gui.LoggerWindow;
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

    public void initialize() {
        final OutputStream out = new OutputStream() {

            private final PrintStream original = new PrintStream(System.out);

            @Override
            public void write(final int b) {
                loggerWindow.updateConsoleTextArea(String.valueOf((char) b));
                fileLogger.print(String.valueOf((char) b));
                original.print((char) b);
            }

            @Override
            public void write(@NotNull final byte[] b, final int off, final int len) {
                loggerWindow.updateConsoleTextArea(new String(b, off, len));
                fileLogger.print(new String(b, off, len));
                original.print(new String(b, off, len));
            }

            @Override
            public void write(@NotNull final byte[] b) {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }
}
