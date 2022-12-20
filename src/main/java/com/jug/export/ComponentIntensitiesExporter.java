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
    private final ImageProperties imageProperties;
    private ResultTable table;

    public ComponentIntensitiesExporter(IConfiguration configuration, ImageProperties imageProperties) {
        this.configuration = configuration;
        this.imageProperties = imageProperties;
    }

    @Override
    public void export(Growthlane gl, IGlExportFilePathGetter exportFilePaths) throws GRBException {
        System.out.println("START: Export component intensities.");
        int channelNumber = configuration.getFluorescenceAssignmentFilterChannel();
        List<ComponentInterface> components = gl.getIlp().getAllComponentsInIlp();
        this.table = new ResultTable(",");
        ResultTableColumn<String> regionNameCol = this.table.getColumn(String.class, "region_name");
        ResultTableColumn<String> regionTypeCol = this.table.getColumn(String.class, "region_type");
        ResultTableColumn<Long> componentSizeCol = this.table.getColumn(Long.class, "region_size__px");
        ResultTableColumn<Integer> frameCol = this.table.getColumn(Integer.class, "frame_number");
        ResultTableColumn<Double> intensityTotalCol = this.table.getColumn(Double.class, "intensity_total__au");
        ResultTableColumn<Double> intensityStdCol = this.table.getColumn(Double.class, "intensity_std__au");
        for (ComponentInterface component : components) {
            /* add values for the foreground intensities */
            regionNameCol.addValue(component.getStringId());
            regionTypeCol.addValue("frgr");
            frameCol.addValue(component.getFrameNumber());
            componentSizeCol.addValue(component.size());
            intensityTotalCol.addValue(component.getMaskIntensityTotal(channelNumber));
            intensityStdCol.addValue(component.getMaskIntensitiesStd(channelNumber));

            /* add values for the background intensities */
            regionNameCol.addValue(component.getStringId());
            regionTypeCol.addValue("bkgr");
            frameCol.addValue(component.getFrameNumber());
            componentSizeCol.addValue(component.getBackgroundRoiSize());
            intensityTotalCol.addValue(component.getBackgroundIntensityTotal(channelNumber));
            intensityStdCol.addValue(component.getBackgroundIntensityStd(channelNumber));
        }

        /* add values for the image background */
        for (int frame = 0; frame <= gl.getTimeStepMaximum(); frame++) {
            regionNameCol.addValue("ImgBkgrT" + frame);
            regionTypeCol.addValue("bkgr");
            frameCol.addValue(frame);
            componentSizeCol.addValue(imageProperties.getBackgroundRoiSize());
            intensityTotalCol.addValue(imageProperties.getBackgroundIntensityTotalAtFrame(channelNumber, frame));
            intensityStdCol.addValue(imageProperties.getBackgroundIntensityStdAtFrame(channelNumber, frame));
        }

        try {
            table.writeToFile(exportFilePaths.getAssignmentFilterIntensitiesCsvFile());
        } catch (final IOException e1) {
            throw new RuntimeException(String.format("Could not write component intensities to file: %s", exportFilePaths.getAssignmentFilterIntensitiesCsvFile().toString()));
        }
        System.out.println("FINISH: Export component intensities.");
    }
}
