package com.jug.util.componenttree;

import com.jug.util.TestUtils;
import net.imglib2.type.numeric.real.FloatType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ComponentForestSerializationTests {

    private final TestUtils testUtils;

    public ComponentForestSerializationTests() {
        testUtils = new TestUtils();
    }

    public static void main(String... args) throws IOException, InterruptedException {
        new ComponentForestSerializationTests().testComponentSerialization();
    }

    public void testComponentSerialization() throws IOException {
        List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> componentForests = getAdvancedComponentForests(5);

        ComponentForestSerializer sut = new ComponentForestSerializer();

//        Path jsonFile = Files.createTempFile("", ".json");
        String jsonString = sut.serializeToDisk(componentForests);

//        for (AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> tree : componentForests) {
//            Plotting.drawComponentTree2(tree, new ArrayList<>(), tree.rootsSorted().get(0).getSourceImage());
//        }
    }

    @NotNull
    private List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> getAdvancedComponentForests(int numberOfForests) throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/20201119_VNG1040_AB2h_2h_1__Pos5_GL17/probability_maps__frames430-450__cropped__20201119_VNG1040_AB2h_2h_1_MMStack_Pos5_GL17__model_20210715_5b27d7aa.tif";

        List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> componentForests = new ArrayList<>();
        for (int frameIndex = 0; frameIndex < numberOfForests; frameIndex++) {
            componentForests.add((AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>) testUtils.getComponentTreeFromProbabilityImage(imageFile, frameIndex, 1.0f));
        }
        return componentForests;
    }
}
