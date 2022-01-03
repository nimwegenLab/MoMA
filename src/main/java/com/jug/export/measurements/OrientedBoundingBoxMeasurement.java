package com.jug.export.measurements;

import com.jug.export.ResultTable;
import com.jug.export.ResultTableColumn;
import com.jug.export.SegmentRecord;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.BoundingBoxProperties;
import com.jug.util.componenttree.OrientedBoundingBoxCalculator;
import net.imagej.ops.geom.geom2d.LabelRegionToPolygonConverter;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.Context;

public class OrientedBoundingBoxMeasurement implements SegmentMeasurementInterface {
    private final ResultTableColumn<Double> center_x_col;
    private final ResultTableColumn<Double> center_y_col;
    private final ResultTableColumn<Double> rotation_angle_col;
    private final ResultTableColumn<Double> width_col;
    private final ResultTableColumn<Double> height_col;
    private final ResultTable outputTable;
    private final LabelRegionToPolygonConverter regionToPolygonConverter;
    private final OrientedBoundingBoxCalculator boundingBoxCalculator;

    public OrientedBoundingBoxMeasurement(ResultTable outputTable, Context context) {
        this.outputTable = outputTable;
        center_x_col = outputTable.addColumn(new ResultTableColumn<>("oriented_bbox_center_x_px", "%.2f"));
        center_y_col = outputTable.addColumn(new ResultTableColumn<>("oriented_bbox_center_y_px", "%.2f"));
        width_col = outputTable.addColumn(new ResultTableColumn<>("oriented_bbox_width", "%.2f"));
        height_col = outputTable.addColumn(new ResultTableColumn<>("oriented_bbox_length", "%.2f"));
        rotation_angle_col = outputTable.addColumn(new ResultTableColumn<>("oriented_bbox_orientation_angle", "%.2f"));

        regionToPolygonConverter = new LabelRegionToPolygonConverter();
        regionToPolygonConverter.setContext(context);
        boundingBoxCalculator = new OrientedBoundingBoxCalculator();
    }

    @Override
    public void measure(SegmentRecord segmentRecord) {
        AdvancedComponent<FloatType> component = segmentRecord.hyp.getWrappedComponent();
        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
        Polygon2D orientedBoundingBoxPolygon = boundingBoxCalculator.calculate(poly);
        BoundingBoxProperties result = new BoundingBoxProperties(orientedBoundingBoxPolygon);
        center_x_col.addValue(result.getCenterCoordinate().getX());
        center_y_col.addValue(result.getCenterCoordinate().getY());
        width_col.addValue(result.getWidth());
        height_col.addValue(result.getHeight());
        rotation_angle_col.addValue(result.getRotationAngle());
    }
}
