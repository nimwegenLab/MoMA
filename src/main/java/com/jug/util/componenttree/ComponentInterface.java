package com.jug.util.componenttree;

import com.jug.util.math.Vector2DPolyline;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.img.Img;
import net.imglib2.roi.MaskInterval;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;

import java.util.List;
import java.util.Set;

public interface ComponentInterface<T extends Type<T>, C extends ComponentInterface<T, C>> extends Component<T, C> {
    float getCost();

    RandomAccessibleInterval<FloatType> getSourceImage();

    double[] firstMomentPixelCoordinates();

    Double getWatershedLinePixelValueAverage();

    List<Double> getWatershedLinePixelValuesAsDoubles();

    LabelRegion<Integer> getRegion();

    <T extends NativeType<T>> Img<T> getComponentImage(T pixelValue);

    void addComponentFeature(String featureName, Vector2DPolyline feature);

    Vector2DPolyline getComponentFeature(String featureName);

    Set<String> getComponentFeatureNames();

    List<Double> getComponentPixelValuesAsDouble();

    Pair<Double, Double> getPixelValueExtremaInsideRange(double rangeMin, double rangeMax);

    MaskInterval getBorderMask();

    MaskInterval getDilatedMask();

    MaskInterval getErodedMask();

    Img<BitType> getCoreMaskImg();

    C getParent();

    double getPixelValueAverage();

    String getStringId();

    /**
     * Return the image intensity of channel channelNumber within the mask of the component.
     * @param channelNumber
     * @return
     */
    double getMaskIntensity(int channelNumber);
}
