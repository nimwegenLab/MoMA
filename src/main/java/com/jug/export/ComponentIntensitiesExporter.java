package com.jug.export;

import com.jug.Growthlane;
import com.jug.config.IConfiguration;
import com.jug.datahandling.IGlExportFilePathGetter;
import com.jug.util.componenttree.ComponentInterface;
import gurobi.GRBException;

import java.util.List;

public class ComponentIntensitiesExporter implements ResultExporterInterface {
    private IConfiguration configuration;
    private ResultTable resultTable;
    private ResultTableColumn<Double> frameCol;

    public ComponentIntensitiesExporter(IConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void export(Growthlane gl, IGlExportFilePathGetter exportFilePaths) throws GRBException {
        setupCsvTable();
        List<ComponentInterface> components = gl.getIlp().getAllComponentsInIlp();
        for(ComponentInterface component : components){
            double val = component.getMaskIntensity(configuration.getFluorescentAssignmentFilterChannel());
            this.resultTable.addValue(val, "mask_intensity [a.u.]");
        }
    }

    public void setupCsvTable() {
        this.resultTable = new ResultTable(",");
        this.frameCol = resultTable.addColumn(new ResultTableColumn<>("mask_intensity [a.u.]"));
    }
}
