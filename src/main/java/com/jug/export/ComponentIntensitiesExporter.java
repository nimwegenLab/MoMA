package com.jug.export;

import com.jug.Growthlane;
import com.jug.config.IConfiguration;
import com.jug.datahandling.IGlExportFilePathGetter;
import com.jug.lp.ImageProperties;
import com.jug.util.componenttree.ComponentInterface;
import gurobi.GRBException;

import java.io.IOException;
import java.util.List;

public class ComponentIntensitiesExporter implements ResultExporterInterface {
    private final IConfiguration configuration;
    private ImageProperties imageProperties;
    private ResultTable table;

    public ComponentIntensitiesExporter(IConfiguration configuration, ImageProperties imageProperties) {
        this.configuration = configuration;
        this.imageProperties = imageProperties;
    }

    @Override
    public void export(Growthlane gl, IGlExportFilePathGetter exportFilePaths) throws GRBException {
        System.out.println("START: Export component intensities.");
        this.table = new ResultTable(",");
        int channel = configuration.getFluorescentAssignmentFilterChannel();
        List<ComponentInterface> components = gl.getIlp().getAllComponentsInIlp();
        ResultTableColumn<String> idCol = this.table.getColumn(String.class, "component_id");
        ResultTableColumn<Double> componentMaskIntensityCol = this.table.getColumn(Double.class, String.format("mask_intensity_ch_%d__au", channel));
        ResultTableColumn<Double> componentBackgroundIntensityCol = this.table.getColumn(Double.class, String.format("component_bkgr_intensity_ch_%d__au", channel));
        ResultTableColumn<Long> componentSize = this.table.getColumn(Long.class, "component_size__px");
        ResultTableColumn<Double> imageRoiIntensityCol = this.table.getColumn(Double.class, String.format("image_roi_intensity_ch_%d__au", channel));
        ResultTableColumn<Double> imageRoiIntensitySizeCol = this.table.getColumn(Double.class, String.format("image_roi_size__px", channel));
        for (ComponentInterface component : components) {
            idCol.addValue(component.getStringId());
            componentSize.addValue(component.size());
            componentMaskIntensityCol.addValue(component.getMaskIntensity(channel));
            componentBackgroundIntensityCol.addValue(component.getBackgroundIntensity(channel));
            imageRoiIntensityCol.addValue(imageProperties.getBackgroundIntensityMean(channel));
//            imageRoiIntensitySizeCol.
        }
//        try {
//            table.writeToFile(exportFilePaths.assignmentFilterIntensityInformation());
//        } catch (final IOException e1) {
//            throw new RuntimeException(String.format("Could not write component intensities to file: %s", exportFilePaths.assignmentFilterIntensityInformation().toString()));
//        }
        System.out.println("FINISH: Export component intensities.");
    }
}
