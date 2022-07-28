package com.jug.export;

import com.jug.Growthlane;
import com.jug.datahandling.IGlExportFilePaths;
import com.jug.lp.GRBModel.IGRBModelAdapter;
import gurobi.GRB;
import gurobi.GRBException;

import java.io.File;
import java.util.function.Supplier;

public class IlpModelExporter implements ResultExporterInterface {
    private final Supplier<String> defaultFilenameDecorationSupplier;

    public IlpModelExporter(Supplier<String> defaultFilenameDecorationSupplier) {
        this.defaultFilenameDecorationSupplier = defaultFilenameDecorationSupplier;
    }

    @Override
    public void export(Growthlane gl, IGlExportFilePaths exportFilePaths) throws GRBException {
        gl.getIlp().addStorageLockConstraintsToAssignments();
        IGRBModelAdapter model = gl.getIlp().model;
        model.write(exportFilePaths.getGurobiLpFilePath().toString());
        model.write(exportFilePaths.getGurobiMpsFilePath().toString());
        model.write(exportFilePaths.getGurobiSolFilePath().toString());
        model.write(exportFilePaths.getGurobiJsonFilePath().toString());
        model.write(exportFilePaths.getGurobiMstFilePath().toString());
        gl.getIlp().removeStorageLockConstraintsFromAssignments();
    }
}
