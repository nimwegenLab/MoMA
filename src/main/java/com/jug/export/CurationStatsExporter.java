package com.jug.export;

import com.jug.Growthlane;
import com.jug.MoMA;
import com.jug.config.ConfigurationManager;
import com.jug.datahandling.IGlExportFilePaths;
import gurobi.GRBException;

import java.io.File;

public class CurationStatsExporter implements ResultExporterInterface {
    private final ConfigurationManager configurationManager;

    public CurationStatsExporter(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    @Override
    public void export(Growthlane gl, IGlExportFilePaths exportFilePaths) throws GRBException {
        File outputFolder = exportFilePaths.getOutputPath().toFile();

        /* Export user inputs to the tracking algorithm */
        final int tmin = configurationManager.getMinTime();
        final int tmax = configurationManager.getMaxTime();
        final File file = new File(outputFolder, String.format( "[%d-%d]__%s.moma", tmin, tmax, MoMA.getDefaultFilenameDecoration()));
        MoMA.getGui().model.getCurrentGL().getIlp().saveState(file);
    }
}
