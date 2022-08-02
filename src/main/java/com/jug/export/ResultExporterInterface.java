package com.jug.export;

import com.jug.Growthlane;
import com.jug.datahandling.IGlExportFilePathGetter;
import gurobi.GRBException;

public interface ResultExporterInterface {
    void export(Growthlane gl, IGlExportFilePathGetter exportFilePaths) throws GRBException;
}
