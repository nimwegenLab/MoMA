package com.jug.export;

import com.jug.Growthlane;
import com.jug.datahandling.IGlExportFilePathGetter;
import com.jug.lp.GrowthlaneTrackingILP;
import com.jug.util.componenttree.ComponentProperties;
import gurobi.GRBException;

public class ComponentIntensityExporter implements ResultExporterInterface {
    private final ResultTable resultTable;
    private final ResultTableColumn<Object> frameCol;
    GrowthlaneTrackingILP ilp;
    public ComponentIntensityExporter(Growthlane growthlane, ComponentProperties componentProperties) {
        ilp = growthlane.getIlp();
        resultTable = new ResultTable(",");
        frameCol = resultTable.addColumn(new ResultTableColumn<>("frame"));
    }

    @Override
    public void export(Growthlane gl, IGlExportFilePathGetter exportFilePaths) throws GRBException {

    }
}
