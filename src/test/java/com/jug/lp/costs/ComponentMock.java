package com.jug.lp.costs;

import com.jug.util.componenttree.ComponentInterface;
import com.jug.util.math.Vector2DPolyline;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.roi.MaskInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ComponentMock implements ComponentInterface {
    private final double[] firstMomentPixelCoordinates;
    private RandomAccessibleInterval<BitType> componentImage;
    private ImageJ ij = new ImageJ();

    public ComponentMock(double[] firstMomentPixelCoordinates) {
        this.firstMomentPixelCoordinates = firstMomentPixelCoordinates;
    }

    public ComponentMock(RandomAccessibleInterval<BitType> componentImage) {
        this.componentImage = componentImage;
        firstMomentPixelCoordinates = new double[0];
    }

    @Override
    public double getBackgroundIntensityStd(int channelNumber) {
        throw new NotImplementedException();
    }

    @Override
    public long getBackgroundRoiSize() {
        throw new NotImplementedException();
    }

    @Override
    public int getFrameNumber() {
        throw new NotImplementedException();
    }

    @Override
    public float getCost() {
        throw new NotImplementedException();
    }

    @Override
    public RandomAccessibleInterval<FloatType> getSourceImage() {
        return null;
    }

    public double[] firstMomentPixelCoordinates() {
        return firstMomentPixelCoordinates;
    }

    @Override
    public Double getWatershedLinePixelValueAverage() {
        return null;
    }

    @Override
    public List<Double> getWatershedLinePixelValuesAsDoubles() {
        return null;
    }

    @Override
    public LabelRegion<Integer> getRegion() {
        if (componentImage == null) {
            return null;
        }
        ImgLabeling labeling = ij.op().labeling().cca(this.componentImage, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);
        LabelRegions regions = new LabelRegions(labeling);
        return regions.getLabelRegion(0);
    }

    @Override
    public void addComponentFeature(String featureName, Vector2DPolyline feature) {
        throw new NotImplementedException();
    }

    @Override
    public Vector2DPolyline getComponentFeature(String featureName) {
        throw new NotImplementedException();
    }

    @Override
    public Set<String> getComponentFeatureNames() {
        throw new NotImplementedException();
    }

    @Override
    public List<Double> getComponentPixelValuesAsDouble() {
        return null;
    }

    @Override
    public Pair<Double, Double> getPixelValueExtremaInsideRange(double rangeMin, double rangeMax) {
        return null;
    }

    @Override
    public MaskInterval getBorderMask() {
        return null;
    }

    @Override
    public MaskInterval getDilatedMask() {
        return null;
    }

    @Override
    public MaskInterval getErodedMask() {
        return null;
    }

    @Override
    public Img<BitType> getCoreMaskImg() {
        return null;
    }

    @Override
    public Img getComponentImage(NativeType pixelValue) {
        return ImgView.wrap(componentImage);
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public Object value() {
        return null;
    }

    @Override
    public ComponentInterface getParent() {
        return null;
    }

    @Override
    public double getPixelValueAverage() {
        return 0;
    }

    @Override
    public String getStringId() {
        return null;
    }

    @Override
    public double getMaskIntensityTotal(int channelNumber) {
        throw new NotImplementedException();
    }

    @Override
    public double getBackgroundIntensityTotal(int channelNumber) { throw  new NotImplementedException(); }

    @Override
    public double getMaskIntensitiesStd(int channelNumber) {
        throw new NotImplementedException();
    }

    @Override
    public double getMaskIntensityMean(int expectedTargetChannelNumber) {
        throw new NotImplementedException();
    }

    @Override
    public List getChildren() {
        return null;
    }

    @NotNull
    @Override
    public Iterator iterator() {
        return null;
    }
}
