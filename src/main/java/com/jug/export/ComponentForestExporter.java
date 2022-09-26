package com.jug.export;

import com.jug.Growthlane;
import com.jug.GrowthlaneFrame;
import com.jug.datahandling.IGlExportFilePathGetter;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.AdvancedComponentForest;
import com.jug.util.componenttree.ComponentForestSerializer;
import gurobi.GRBException;
import net.imglib2.type.numeric.real.FloatType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ComponentForestExporter implements ResultExporterInterface {
    @Override
    public void export(Growthlane gl, IGlExportFilePathGetter exportFilePaths) throws GRBException {
        List<GrowthlaneFrame> glFrames = gl.getFrames();
        List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> componentForests = new ArrayList<>();
        for (GrowthlaneFrame frame : glFrames) {
            componentForests.add(frame.getComponentForest());
        }

        ComponentForestSerializer serializer = new ComponentForestSerializer();
        String jsonString = serializer.serializeToJson(componentForests);

        File outputFile = exportFilePaths.getComponentTreeJsonFile();
        exportFilePaths.createFile(outputFile);
        try {
            FileWriter writer = new FileWriter(outputFile);
            writer.write(jsonString);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Could not write file-format information to file: " + outputFile, e);
        }
    }
}
