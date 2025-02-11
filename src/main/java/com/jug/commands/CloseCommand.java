package com.jug.commands;

import com.jug.config.CommandLineArgumentsParser;
import com.jug.config.ConfigurationManager;

import javax.swing.*;
import java.awt.event.WindowEvent;

public class CloseCommand implements ICommand {
    private final ConfigurationManager configurationManager;
    private final CommandLineArgumentsParser commandLineArgumentParser;
    private final JFrame guiFrame;

    public CloseCommand(JFrame guiFrame, ConfigurationManager configurationManager, CommandLineArgumentsParser commandLineArgumentParser) {
        this.guiFrame = guiFrame;
        this.configurationManager = configurationManager;
        this.commandLineArgumentParser = commandLineArgumentParser;
    }

    public void run() {
        configurationManager.saveParams(guiFrame);
        guiFrame.dispatchEvent(new WindowEvent(guiFrame, WindowEvent.WINDOW_CLOSING));
    }
}
