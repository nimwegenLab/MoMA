package com.jug.export;

import com.jug.Growthlane;
import com.jug.config.IConfiguration;
import com.jug.datahandling.IGlExportFilePathGetter;
import com.jug.util.componenttree.ComponentInterface;
import gurobi.GRBException;

import java.io.IOException;
import java.util.List;

public class ComponentIntensitiesExporter implements ResultExporterInterface {
    private final IConfiguration configuration;
    private ResultTable table;

    public ComponentIntensitiesExporter(IConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void export(Growthlane gl, IGlExportFilePathGetter exportFilePaths) throws GRBException {
        this.table = new ResultTable(",");
        int channel = configuration.getFluorescentAssignmentFilterChannel();
        List<ComponentInterface> components = gl.getIlp().getAllComponentsInIlp();
        ResultTableColumn<String> idCol = this.table.getColumn(String.class, "component_id");
        ResultTableColumn<Double> maskIntensityCol = this.table.getColumn(Double.class, String.format("mask_intensity_ch_%d__au", channel));
        ResultTableColumn<Double> backgroundIntensityCol = this.table.getColumn(Double.class, String.format("component_bkgr_intensity_ch_%d__au", channel));
        for (ComponentInterface component : components) {
            idCol.addValue(component.getStringId());
            maskIntensityCol.addValue(component.getMaskIntensity(channel));
            backgroundIntensityCol.addValue(component.getBackgroundIntensity(channel));
        }
        try {
            table.writeToFile(exportFilePaths.assignmentFilterIntensityInformation());
        } catch (final IOException e1) {
            throw new RuntimeException(String.format("Could not writ component intensities to file: %s", exportFilePaths.assignmentFilterIntensityInformation().toString()));
        }
    }
}
