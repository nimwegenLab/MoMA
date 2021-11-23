package com.jug.util.componenttree;

import com.jug.datahandling.IImageProvider;
import com.jug.lp.ImageProviderMock;
import com.jug.util.imglib2.Imglib2Utils;
import com.moma.auxiliary.Plotting;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ComponentPropertiesTest {
    public static void main(String... args) throws IOException, InterruptedException {
        ImageJ ij = new ImageJ();
        ij.ui().showUI();
        new ComponentTreeGeneratorTests().testWatershedding();
    }

    /**
     * Add test for gerating the component tree on a sample image and displaying it.
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testGettingComponentProperties() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/probabilities_watershedding_000.tif";
        assertTrue(new File(imageFile).exists());

        ImageJ ij = new ImageJ();
        Img input = (Img) ij.io().open(imageFile);
        assertNotNull(input);

        int frameIndex = 12;
        IImageProvider imageProviderMock = new ImageProviderMock(input);
        RandomAccessibleInterval<FloatType> currentImage = Views.hyperSlice(input, 2, frameIndex);
        assertEquals(2, currentImage.numDimensions());

        ImageJFunctions.show(currentImage);

        ComponentTreeGenerator componentTreeGenerator = getComponentTreeGenerator(ij);

        ComponentForest<AdvancedComponent<FloatType>> tree = componentTreeGenerator.buildIntensityTree(imageProviderMock, frameIndex, 1.0f);

        ComponentProperties props = new ComponentProperties(ij.op(), new Imglib2Utils(ij.op()));

        ComponentPositionComparator verticalComponentPositionComparator = new ComponentPositionComparator(1);
        List<AdvancedComponent<FloatType>> roots = new ArrayList<>(tree.roots());
        roots.sort(verticalComponentPositionComparator);

        System.out.println("verticalPosition, minorAxis, majorAxis, majorAxisTiltAngle, area, totalIntensity, backgroundRoiArea, totalBackgroundIntensity");
        for(AdvancedComponent component : roots){
            double verticalPosition = props.getCentroid(component).getB();
            double minorAxis = props.getMinorMajorAxis(component).getA();
            double majorAxis = props.getMinorMajorAxis(component).getB();
            double majorAxisTiltAngle = props.getTiltAngle(component);
            double totalIntensity = props.getTotalIntensity(component, component.getSourceImage());
            double totalBackgroundIntensity = props.getTotalBackgroundIntensity(component, currentImage);
            long backgroundRoiArea = props.getBackgroundArea(component, currentImage);
            int area = props.getArea(component);
            System.out.println(String.format("%f, %f, %f, %f, %d, %f, %d, %f", verticalPosition, minorAxis, majorAxis, majorAxisTiltAngle, area, totalIntensity, backgroundRoiArea, totalBackgroundIntensity));
        }

        Plotting.drawComponentTree2(tree, new ArrayList<>());
    }

    @NotNull
    private ComponentTreeGenerator getComponentTreeGenerator(ImageJ ij) {
        OpService ops = ij.op();
        Imglib2Utils imglib2Utils = new Imglib2Utils(ops);
        ComponentProperties componentProperties = new ComponentProperties(ops, imglib2Utils);
        RecursiveComponentWatershedder recursiveComponentWatershedder = new RecursiveComponentWatershedder(ij.op());
        WatershedMaskGenerator watershedMaskGenerator = new WatershedMaskGenerator(0, 0.5f);
        ComponentTreeGenerator componentTreeGenerator = new ComponentTreeGenerator(recursiveComponentWatershedder, componentProperties, watershedMaskGenerator, imglib2Utils);
        return componentTreeGenerator;
    }
}