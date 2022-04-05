package com.jug.export.measurements;

import com.jug.export.ResultTable;
import com.jug.export.ResultTableColumn;
import com.jug.util.componenttree.ComponentInterface;
import com.jug.util.componenttree.ComponentProperties;
import org.javatuples.Sextet;

public class ContourMomentsMeasurement implements SegmentMeasurementInterface {
    private ResultTableColumn<Double> contourArea;
    private ResultTableColumn<Double> contourCentroidX;
    private ResultTableColumn<Double> contourCentroidY;
    private ResultTableColumn<Double> contourVarianceX;
    private ResultTableColumn<Double> contourVarianceY;
    private ResultTableColumn<Double> contourCovarianceXY;
    private ComponentProperties componentProperties;

    public ContourMomentsMeasurement(ComponentProperties componentProperties) {
        this.componentProperties = componentProperties;
    }

    @Override
    public void setOutputTable(ResultTable outputTable) {
        contourArea = outputTable.addColumn(new ResultTableColumn<>("contour_area__px"));
        contourCentroidX = outputTable.addColumn(new ResultTableColumn<>("contour_centroid_x__px"));
        contourCentroidY = outputTable.addColumn(new ResultTableColumn<>("contour_centroid_y__px"));
        contourVarianceX = outputTable.addColumn(new ResultTableColumn<>("contour_variance_x__px2"));
        contourVarianceY = outputTable.addColumn(new ResultTableColumn<>("contour_variance_y__px2"));
        contourCovarianceXY = outputTable.addColumn(new ResultTableColumn<>("contour_covariance__px2"));
    }

    @Override
    public void measure(SegmentMeasurementDataInterface data) {
        Sextet<Double, Double, Double, Double, Double, Double> moments = componentProperties.getCentralMoments(data.getComponentToMeasure());
        contourArea.addValue(moments.getValue0());
        contourCentroidX.addValue(moments.getValue1());
        contourCentroidY.addValue(moments.getValue2());
        contourVarianceX.addValue(moments.getValue3());
        contourVarianceY.addValue(moments.getValue4());
        contourCovarianceXY.addValue(moments.getValue5());
    }
}
