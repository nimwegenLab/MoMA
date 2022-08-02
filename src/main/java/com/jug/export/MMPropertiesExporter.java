package com.jug.export;

import com.jug.Growthlane;
import com.jug.config.ConfigurationManager;
import com.jug.datahandling.IGlExportFilePathGetter;
import gurobi.GRBException;

import javax.swing.*;

public class MMPropertiesExporter implements ResultExporterInterface {
    private JFrame guiFrame;
    private ConfigurationManager configurationManager;

    public MMPropertiesExporter(JFrame guiFrame, ConfigurationManager configurationManager) {
        this.guiFrame = guiFrame;
        this.configurationManager = configurationManager;
    }

    @Override
    public void export(Growthlane gl, IGlExportFilePathGetter exportFilePaths) throws GRBException {
        configurationManager.saveParams(exportFilePaths.getMmPropertiesOutputFilePath().toFile(), guiFrame);
    }
}
