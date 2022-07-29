package com.jug.export;

import com.jug.Growthlane;
import com.jug.MoMA;
import com.jug.config.ConfigurationManager;
import com.jug.datahandling.IGlExportFilePaths;
import gurobi.GRBException;

import java.io.File;

public class CurationStatsExporter implements ResultExporterInterface {
    @Override
    public void export(Growthlane gl, IGlExportFilePaths exportFilePaths) throws GRBException {
        gl.getIlp().saveState(exportFilePaths.getCurationStatsFilePath().toFile());
    }
}
