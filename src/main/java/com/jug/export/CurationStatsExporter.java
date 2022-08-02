package com.jug.export;

import com.jug.Growthlane;
import com.jug.datahandling.IGlExportFilePathGetter;
import gurobi.GRBException;

public class CurationStatsExporter implements ResultExporterInterface {
    @Override
    public void export(Growthlane gl, IGlExportFilePathGetter exportFilePaths) throws GRBException {
        gl.getIlp().saveState(exportFilePaths.getCurationStatsFilePath().toFile());
    }
}
