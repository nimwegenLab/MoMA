package com.jug.export;

import com.jug.Growthlane;
import com.jug.datahandling.IGlExportFilePathGetter;
import com.jug.lp.GRBModel.IGRBModelAdapter;
import com.jug.lp.GrowthlaneTrackingILP;
import gurobi.GRBException;

public class IlpModelExporter implements ResultExporterInterface {
    @Override
    public void export(Growthlane gl, IGlExportFilePathGetter exportFilePaths) throws GRBException {
        GrowthlaneTrackingILP ilp = gl.getIlp();
        if (!ilp.modelIsLockedForStorage()) {
            ilp.addStorageLockConstraintsToAssignments();
        }
        IGRBModelAdapter model = gl.getIlp().model;
        model.write(exportFilePaths.getGurobiLpFilePath().toString());
        model.write(exportFilePaths.getGurobiMpsFilePath().toString());
        model.write(exportFilePaths.getGurobiSolFilePath().toString());
        model.write(exportFilePaths.getGurobiMstFilePath().toString());
        gl.getIlp().removeStorageLockConstraintsFromAssignments();
    }
}
