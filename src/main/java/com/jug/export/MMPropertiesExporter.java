package com.jug.export;

import com.jug.MoMA;
import com.jug.config.ConfigurationManager;
import gurobi.GRBException;

import javax.swing.*;
import java.io.File;

public class MMPropertiesExporter implements ResultExporterInterface {
    private JFrame guiFrame;
    private ConfigurationManager configurationManager;

    public MMPropertiesExporter(JFrame guiFrame, ConfigurationManager configurationManager) {
        this.guiFrame = guiFrame;
        this.configurationManager = configurationManager;
    }

    @Override
    public void export(ResultExporterData resultData) throws GRBException {
        File outputFolder = resultData.getOutputFolder();
        configurationManager.saveParams(new File(outputFolder, "mm.properties"), guiFrame);
    }
}
