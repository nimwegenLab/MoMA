package com.jug.export;

import com.jug.MoMA;
import com.jug.config.ConfigurationManager;
import gurobi.GRBException;

import java.io.File;

public class MMPropertiesExporter implements ResultExporterInterface {
    private ConfigurationManager configurationManager;

    public MMPropertiesExporter(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    @Override
    public void export(ResultExporterData resultData) throws GRBException {
        File outputFolder = resultData.getOutputFolder();
        configurationManager.saveParams(new File(outputFolder, "mm.properties"), MoMA.getGuiFrame());
    }
}
