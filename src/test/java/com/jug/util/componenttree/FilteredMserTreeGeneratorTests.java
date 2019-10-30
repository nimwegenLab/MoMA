package com.jug.util.componenttree;

import com.moma.auxiliary.Plotting;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.IOService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FilteredMserTreeGeneratorTests {
    private IOService io = (new Context()).service(IOService.class);

    public static void main(String... args) throws IOException {
        new FilteredMserTreeGeneratorTests().constructorTest1();
    }

    @Test
    public void constructorTest1() throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/parent_watershedding/sourceImage.tif";
        Img currentImage = (Img) io.open(imageFile);
        ImageJFunctions.show(currentImage);
        ComponentForest<SimpleComponent<FloatType>> tree = new FilteredMserTreeGenerator().buildIntensityTree(currentImage);
        Plotting.drawComponentTree2(tree, new ArrayList<>());
    }
}