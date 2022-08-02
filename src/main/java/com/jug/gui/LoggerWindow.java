package com.jug.gui;

import com.jug.config.ConfigurationManager;
import org.jetbrains.annotations.NotNull;
import org.nd4j.linalg.io.Assert;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Objects;

public class LoggerWindow {
    private final String windowTitle;
    private final ConfigurationManager configurationManager;
    /**
     * Frame hosting the console output.
     */
    private JFrame frameConsoleWindow;

    /**
     * TextArea hosting the console output within the JFrame frameConsoleWindow.
     */
    private JTextArea consoleWindowTextArea;

    public LoggerWindow(String momaVersionString, ConfigurationManager configurationManager) {
        Assert.notNull(momaVersionString, "momaVersionString is null");
        this.configurationManager = Objects.requireNonNull(configurationManager, "configurationManager is null");
        windowTitle = String.format( "%s Console Window", momaVersionString );
    }

    /**
     * Creates and shows the console window and redirects 'System.out' and
     * 'System.err' to it.
     */
    public void initConsoleWindow() {
        frameConsoleWindow = new JFrame(windowTitle);
        // frameConsoleWindow.setResizable( false );
        consoleWindowTextArea = new JTextArea();
        consoleWindowTextArea.setLineWrap( true );
        consoleWindowTextArea.setWrapStyleWord( true );

        final int centerX = ( int ) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2;
        final int centerY = ( int ) Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2;
        frameConsoleWindow.setBounds(centerX - configurationManager.GUI_CONSOLE_WIDTH / 2, centerY - configurationManager.GUI_HEIGHT / 2, configurationManager.GUI_CONSOLE_WIDTH, configurationManager.GUI_HEIGHT);
        final JScrollPane scrollPane = new JScrollPane( consoleWindowTextArea );
//		scrollPane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
        scrollPane.setBorder( BorderFactory.createEmptyBorder( 0, 15, 0, 0 ) );
        frameConsoleWindow.getContentPane().add( scrollPane );

        final OutputStream out = new OutputStream() {

            private final PrintStream original = new PrintStream( System.out );

            @Override
            public void write( final int b ) {
                updateConsoleTextArea( String.valueOf( ( char ) b ) );
                original.print((char) b);
            }

            @Override
            public void write(@NotNull final byte[] b, final int off, final int len ) {
                updateConsoleTextArea( new String( b, off, len ) );
                original.print( new String( b, off, len ) );
            }

            @Override
            public void write(@NotNull final byte[] b ) {
                write( b, 0, b.length );
            }
        };

        System.setOut( new PrintStream( out, true ) );
        System.setErr( new PrintStream( out, true ) );
    }

    private void updateConsoleTextArea( final String text ) {
        SwingUtilities.invokeLater(() -> consoleWindowTextArea.append( text ));
    }

    /**
     * Shows the LoggerWindow
     */
    public void showConsoleWindow( final boolean show ) {
        frameConsoleWindow.setVisible( show );
    }

    /**
     * @return indicates if the log-window is currently visible.
     */
    public boolean isConsoleVisible() {
        return this.frameConsoleWindow.isVisible();
    }

}
