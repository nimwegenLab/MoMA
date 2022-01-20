package com.jug.lp.costs;

import com.jug.util.componenttree.ComponentInterface;
import com.jug.util.math.Vector2DPolyline;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
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

    public double[] firstMomentPixelCoordinates() {
        return firstMomentPixelCoordinates;
    }

    @Override
    public Double getWatershedLinePixelValueAverage() {
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
    public Set<String> getComponentFeatureNames(String featureName) {
        throw new NotImplementedException();
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
    public Component getParent() {
        return null;
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
