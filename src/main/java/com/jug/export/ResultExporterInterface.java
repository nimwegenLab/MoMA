package com.jug.export;

import com.jug.Growthlane;
import com.jug.datahandling.IGlExportFilePaths;
import gurobi.GRBException;

import java.io.File;
import java.util.List;

public interface ResultExporterInterface {
    void export(Growthlane gl, IGlExportFilePaths exportFilePaths) throws GRBException;
}
