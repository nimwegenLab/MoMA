package com.jug.export;

import com.jug.Growthlane;
import com.jug.datahandling.IGlExportFilePathGetter;
import com.jug.datahandling.Version;
import gurobi.GRBException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MetaDataExporter implements ResultExporterInterface {
    @Override
    public void export(Growthlane gl, IGlExportFilePathGetter exportFilePaths) throws GRBException {
        Version version = new Version("0.1.0");
        File outputFile = exportFilePaths.getFileFormatFilePath().toFile();
        try {
            FileWriter writer = new FileWriter(outputFile);
            writer.write(version.toJson());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Could not write file-format information to file: " + outputFile, e);
        }
    }
}
