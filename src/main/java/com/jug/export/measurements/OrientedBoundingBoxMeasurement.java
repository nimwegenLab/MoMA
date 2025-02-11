package com.jug.export.measurements;

import com.jug.export.ResultTable;
import com.jug.export.ResultTableColumn;
import com.jug.export.SegmentRecord;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.BoundingBoxProperties;
import com.jug.util.componenttree.ComponentInterface;
import com.jug.util.componenttree.OrientedBoundingBoxCalculator;
import com.jug.util.math.Vector2DPolyline;
import net.imagej.ops.geom.geom2d.LabelRegionToPolygonConverter;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.Context;

public class OrientedBoundingBoxMeasurement implements SegmentMeasurementInterface {
    private final LabelRegionToPolygonConverter regionToPolygonConverter;
    private final OrientedBoundingBoxCalculator boundingBoxCalculator;
    private ResultTableColumn<Double> center_x_col;
    private ResultTableColumn<Double> center_y_col;
    private ResultTableColumn<Double> rotation_angle_col;
    private ResultTableColumn<Double> width_col;
    private ResultTableColumn<Double> height_col;

    public OrientedBoundingBoxMeasurement(Context context) {
        regionToPolygonConverter = new LabelRegionToPolygonConverter();
        regionToPolygonConverter.setContext(context);
        boundingBoxCalculator = new OrientedBoundingBoxCalculator();
    }

    @Override
    public void setOutputTable(ResultTable outputTable) {
        center_x_col = outputTable.addColumn(new ResultTableColumn<>("oriented_bbox_center_x_px", "%.2f"));
        center_y_col = outputTable.addColumn(new ResultTableColumn<>("oriented_bbox_center_y_px", "%.2f"));
        width_col = outputTable.addColumn(new ResultTableColumn<>("oriented_bbox_width_px", "%.2f"));
        height_col = outputTable.addColumn(new ResultTableColumn<>("oriented_bbox_length_px", "%.2f"));
        rotation_angle_col = outputTable.addColumn(new ResultTableColumn<>("oriented_bbox_orientation_angle_rad", "%.4f"));
    }

    @Override
    public void measure(SegmentMeasurementDataInterface data) {
        ComponentInterface component = data.getComponentToMeasure();
        final Polygon2D poly = regionToPolygonConverter.convert(component.getRegion(), Polygon2D.class);
        Polygon2D orientedBoundingBoxPolygon = boundingBoxCalculator.calculate(poly);
        Vector2DPolyline boundingBoxFeature = Vector2DPolyline.createFromVertexList(orientedBoundingBoxPolygon.vertices());
        boundingBoxFeature.setType(Vector2DPolyline.PolyshapeType.POLYGON);
        component.addComponentFeature("orientedbbox", boundingBoxFeature);
        BoundingBoxProperties result = new BoundingBoxProperties(orientedBoundingBoxPolygon);
        center_x_col.addValue(result.getCenterCoordinate().getX());
        center_y_col.addValue(result.getCenterCoordinate().getY());
        width_col.addValue(result.getWidth());
        height_col.addValue(result.getHeight());
        rotation_angle_col.addValue(result.getRotationAngle());
    }
}
