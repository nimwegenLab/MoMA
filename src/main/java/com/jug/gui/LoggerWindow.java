package com.jug.gui;

import com.jug.config.ConfigurationManager;
import com.jug.util.PseudoDic;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;

public class LoggerWindow {
    private final String windowTitle;
    private ConfigurationManager configurationManager;
    /**
     * Frame hosting the console output.
     */
    private JFrame frameConsoleWindow;

    /**
     * TextArea hosting the console output within the JFrame frameConsoleWindow.
     */
    private JTextArea consoleWindowTextArea;

    public LoggerWindow(PseudoDic dic, ConfigurationManager configurationManager) {
        windowTitle = String.format( "%s Console Window", dic.getGitVersionProvider().getVersionString() );
        this.configurationManager = configurationManager;
    }

    /**
     * Created and shows the console window and redirects System.out and
     * System.err to it.
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
            public void write( final byte[] b, final int off, final int len ) {
                updateConsoleTextArea( new String( b, off, len ) );
                original.print( new String( b, off, len ) );
            }

            @Override
            public void write( final byte[] b ) {
                write( b, 0, b.length );
            }
        };

        final OutputStream err = new OutputStream() {

            private final PrintStream original = new PrintStream( System.out );

            @Override
            public void write( final int b ) {
                updateConsoleTextArea( String.valueOf( ( char ) b ) );
                original.print((char) b);
            }

            @Override
            public void write( final byte[] b, final int off, final int len ) {
                updateConsoleTextArea( new String( b, off, len ) );
                original.print( new String( b, off, len ) );
            }

            @Override
            public void write( final byte[] b ) {
                write( b, 0, b.length );
            }
        };

        System.setOut( new PrintStream( out, true ) );
        System.setErr( new PrintStream( err, true ) );
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
     * @return
     */
    public boolean isConsoleVisible() {
        return this.frameConsoleWindow.isVisible();
    }

}
