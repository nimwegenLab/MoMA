package com.jug.util.componenttree;

import com.moma.auxiliary.Plotting;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ComponentPropertiesTest {
    public static void main(String... args) throws IOException, InterruptedException {
        ImageJ ij = new ImageJ();
        new ComponentTreeGeneratorTests().testWatershedding();
    }

    /**
     * Add test for gerating the component tree on a sample image and displaying it.
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testGettingComponentProperties() throws IOException, InterruptedException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/probabilities_watershedding_000.tif";
        assertTrue(new File(imageFile).exists());

        ImageJ ij = new ImageJ();
        Img input = (Img) ij.io().open(imageFile);
        assertNotNull(input);

        RandomAccessibleInterval<FloatType> currentImage = Views.hyperSlice(input, 2, 12);
        assertEquals(2, currentImage.numDimensions());

        ImageJFunctions.show(currentImage);
        ComponentForest<SimpleComponent<FloatType>> tree = new ComponentTreeGenerator().buildIntensityTree(currentImage);

        ComponentProperties props = new ComponentProperties();

        ComponentPositionComparator verticalComponentPositionComparator = new ComponentPositionComparator(1);
        List<SimpleComponent<FloatType>> roots = new ArrayList<>(tree.roots());
        roots.sort(verticalComponentPositionComparator);

        for(SimpleComponent component : roots){
            double verticalPosition = props.getCentroid(component).getB();
            double minorAxis = props.getMinorMajorAxis(component).getA();
            double majorAxis = props.getMinorMajorAxis(component).getB();
            int area = props.getArea(component);
            System.out.println(String.format("properties: %f, %f, %f, %d", verticalPosition, minorAxis, majorAxis, area));
        }

        Plotting.drawComponentTree2(tree, new ArrayList<>());
    }
}