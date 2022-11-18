package com.jug.util.componenttree;

import com.google.gson.Gson;
import com.jug.datahandling.IImageProvider;
import com.jug.util.TestUtils;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.type.numeric.real.FloatType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static java.util.Objects.isNull;

public class ComponentForestSerializationTests {

    private final TestUtils testUtils;

    public ComponentForestSerializationTests() {
        testUtils = new TestUtils();
    }

//    public static void main(String... args) throws IOException, InterruptedException {
//        new ComponentForestSerializationTests().serializing_and_deserializing_component_yields_equal_component();
//    }

    @ParameterizedTest()
    @CsvSource({"0, 27.510710166578065",
                "1, 2756.5084530115128",
                "2, 41669.0"})
    public void getBackgroundIntensity__for_deserialized_component__returns_correct_intensities(int channelNumber, double expectedIntensity) throws IOException {
        AdvancedComponent<FloatType> sutComponent = getComponent("HypT5T120B158L48R59H228230625");

        sutComponent.getBackgroundIntensity(channelNumber); /* force calculation of the value*/
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);

        Assertions.assertEquals(expectedIntensity, componentDeserialized.getBackgroundIntensity(channelNumber), 1e-6);
    }

    @ParameterizedTest()
    @CsvSource({"0, 27.510710166578065",
                "1, 2756.5084530115128",
                "2, 41669.0"})
    public void getBackgroundIntensities__when_called_on_pojo__map_value_are_correctly_set(int channelNumber, double expectedIntensity) throws IOException {
        AdvancedComponent<FloatType> sutComponent = getComponent("HypT5T120B158L48R59H228230625");
        sutComponent.getBackgroundIntensity(channelNumber); /* force calculation of the value*/

        AdvancedComponentPojo pojo = sutComponent.getSerializableRepresentation();
        Map<Integer, Double> intensities = pojo.getBackgroundIntensities();

        Assertions.assertEquals(expectedIntensity, intensities.get(channelNumber), 1e-6);
    }

    @ParameterizedTest()
    @CsvSource({"0, 152.452898748219",
                "1, 56636.20825576782",
                "2, 101679.0"})
    public void getMaskIntensity__for_deserialized_component__returns_correct_intensities(int channelNumber, double expectedIntensity) throws IOException {
        AdvancedComponent<FloatType> sutComponent = getComponent("HypT5T120B158L48R59H228230625");

        sutComponent.getMaskIntensityTotal(channelNumber);
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);

        Assertions.assertEquals(expectedIntensity, componentDeserialized.getMaskIntensityTotal(channelNumber), 1e-6);
    }

    @ParameterizedTest()
    @CsvSource({"0, 152.452898748219",
                "1, 56636.20825576782",
                "2, 101679.0"})
    public void getMaskIntensities__when_called_on_pojo__map_value_are_correctly_set(int channelNumber, double expectedIntensity) throws IOException {
        AdvancedComponent<FloatType> sutComponent = getComponent("HypT5T120B158L48R59H228230625");
        sutComponent.getMaskIntensityTotal(channelNumber);

        AdvancedComponentPojo pojo = sutComponent.getSerializableRepresentation();
        Map<Integer, Double> intensities = pojo.getMaskIntensities();

        Assertions.assertEquals(expectedIntensity, intensities.get(channelNumber), 1e-6);
    }

    @Test
    public void isequal__for_json_serialized_copy_of_list_of_component_trees__is_true() throws IOException {
        List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> componentForests = getAdvancedComponentForestList(0, 1);
        ComponentForestSerializer serializer = new ComponentForestSerializer();
        String jsonString = serializer.serializeToJson(componentForests);

        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> componentForestToSerialize = componentForests.get(0);
        AdvancedComponent<FloatType> randomComponent = componentForestToSerialize.getAllComponents().get(0);
        int frameNumber = randomComponent.getFrameNumber();
        IImageProvider imageProvider = randomComponent.getImageProvider();

        ComponentForestDeserializer deserializer = new ComponentForestDeserializer(testUtils.getComponentProperties(), jsonString, imageProvider);
        ComponentForest<AdvancedComponent<FloatType>> componentForestDeserialized = deserializer.buildComponentForest(imageProvider, frameNumber, Float.MIN_VALUE); /* the threshold-value is not used for the deserializing, because we are not thresholding anything; hence we set it to Float.MIN_VALUE */

        Assertions.assertEquals(componentForestToSerialize, componentForestDeserialized);
//        Path jsonFile = Files.createTempFile("", ".json");

//        for (AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> tree : componentForests) {
//            Plotting.drawComponentTree2(tree, new ArrayList<>(), tree.rootsSorted().get(0).getSourceImage());
//        }
//        List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> componentForests = getAdvancedComponentForestList(0, 2);
//        String jsonString = new Gson().toJson(componentForests);
//        AdvancedComponentPojo pojo_new = new Gson().fromJson(jsonString, AdvancedComponentPojo.class);
    }

    @Test
    public void hashCode__for_json_serialized_copy__is_equal() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getInternalComponentNode();
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);
        Assertions.assertEquals(sutComponent.hashCode(), componentDeserialized.hashCode());
    }

    @Test
    public void hashCode__for_json_serialized_copy__is_not_equal_to_default_value_of_777() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getInternalComponentNode();
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);
            Assertions.assertNotEquals(777, componentDeserialized.hashCode());
    }

    @Test
    public void value_field__for_json_serialized_copy__is_equal() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getInternalComponentNode();
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);
        Assertions.assertNotEquals(0.0, sutComponent.value().getRealDouble()); /* make sure value is not 0.0 to make the test more expressive, because it could be that in future changes the value is set to 0 by default */
        Assertions.assertEquals(sutComponent.value(), componentDeserialized.value());
    }

    @Test
    public void getChildStringIds__for_json_serialized_copy_of_leaf_component_node__returns_empty_list() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getLeafComponentNode();
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);
        Assertions.assertEquals(sutComponent.getChildrenStringIds(), componentDeserialized.getChildrenStringIds());
    }

    @Test
    public void getChildStringIds__for_json_serialized_copy_of_root_component_node_with_children__returns_correct_value() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getRootComponentNodeWithChildren();
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);
        Assertions.assertEquals(sutComponent.getChildrenStringIds(), componentDeserialized.getChildrenStringIds());
    }

    @Test
    public void getChildStringIds__for_leaf_component_node__returns_empty_list() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getLeafComponentNode();
        List<String> childStringIds = sutComponent.getChildrenStringIds();
        Assertions.assertTrue(childStringIds.isEmpty());
    }

    @Test
    public void getChildStringIds__for_root_component_node_with_children__returns_correct_value() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getRootComponentNodeWithChildren();
        List<String> childStringIds = sutComponent.getChildrenStringIds();
        String[] expected = new String[]{"HypT5T420B429L51R60H1646872133", "HypT5T430B458L49R59H-2058293790"};
        Assertions.assertEquals(Arrays.asList(expected), childStringIds);
        Assertions.assertEquals("HypT5T420B429L51R60H1646872133", childStringIds.get(0));
        Assertions.assertEquals("HypT5T430B458L49R59H-2058293790", childStringIds.get(1));
    }

    @Test
    public void getParentStringId__for_json_serialized_copy_of_internal_component_node__is_equal() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getInternalComponentNode();
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);
        Assertions.assertEquals(sutComponent.getParentStringId(), componentDeserialized.getParentStringId());
    }

    @Test
    public void getParentStringId__for_json_serialized_copy_of_root_component_node__is_equal() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getRootComponentNodeWithChildren();
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);
        Assertions.assertEquals(sutComponent.getParentStringId(), componentDeserialized.getParentStringId());
    }

    @Test
    public void getLabel__for_json_serialized_copy__is_equal() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getRootComponentNodeWithChildren();
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);
        Assertions.assertEquals(sutComponent.getLabel(), componentDeserialized.getLabel());
    }

    @Test
    public void getParentStringId__for_internal_component_node__returns_correct_value() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getInternalComponentNode();
        Assertions.assertEquals(sutComponent.getParentStringId(), "HypT5T280B395L47R59H725080955");
    }

    @Test
    public void getParentStringId__for_root_component__returns_NA() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getRootComponentNodeWithChildren();
        Assertions.assertEquals(sutComponent.getParentStringId(), "NA");
    }

    @Test
    public void getFrameNumber__for_json_serialized_copy__is_equal() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getRootComponentNodeWithChildren();
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);
        Assertions.assertEquals(sutComponent.getFrameNumber(), componentDeserialized.getFrameNumber());
    }

    @Test
    public void getStringId__for_json_serialized_copy__is_equal() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getRootComponentNodeWithChildren();
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);
        Assertions.assertEquals(sutComponent.getStringId(), componentDeserialized.getStringId());
    }

    @Test
    public void equals__for_two_different_components__returns_false() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getRootComponentNodeWithChildren();
        AdvancedComponent<FloatType> leafComponent = getLeafComponentNode();
        Assertions.assertNotEquals(sutComponent, leafComponent);
    }

    @Test
    public void equals__for_json_serialized_copy_returns__returns_true() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getRootComponentNodeWithChildren();
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);
        Assertions.assertEquals(sutComponent, componentDeserialized);
    }

    @NotNull
    private AdvancedComponent<FloatType> serializeAndDeserializeThroughJsonString(AdvancedComponent<FloatType> sutComponent) {
        AdvancedComponentPojo pojo = sutComponent.getSerializableRepresentation();
        RandomAccessibleInterval<FloatType> sourceImage = sutComponent.getSourceImage();
        IImageProvider imageProvider = sutComponent.getImageProvider();
        String jsonString = new Gson().toJson(pojo);
        AdvancedComponentPojo pojo_new = new Gson().fromJson(jsonString, AdvancedComponentPojo.class);
        AdvancedComponent<FloatType> componentDeserialized = AdvancedComponent.createFromPojo(pojo_new, testUtils.getComponentProperties(), sourceImage, imageProvider);
        return componentDeserialized;
    }

    private AdvancedComponent<FloatType> getLeafComponentNode() throws IOException {
        List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> componentForests = getAdvancedComponentForestList(5, 6);
        AdvancedComponent<FloatType> leafComponent = componentForests.get(0).getComponentWithId("HypT5T366B389L50R59H-219465477");
        Assertions.assertTrue(leafComponent.getChildren().isEmpty());
        Assertions.assertFalse(isNull(leafComponent.getParent()));
        return leafComponent;
    }

    private AdvancedComponent<FloatType> getInternalComponentNode() throws IOException {
        List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> componentForests = getAdvancedComponentForestList(5, 6);
        AdvancedComponent<FloatType> internalComponent = componentForests.get(0).getComponentWithId("HypT5T280B354L47R59H1674282099");
        Assertions.assertFalse(internalComponent.getChildren().isEmpty());
        Assertions.assertFalse(isNull(internalComponent.getParent()));
        return internalComponent;
    }

        private AdvancedComponent<FloatType> getComponent(String componentId) throws IOException {
        List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> componentForests = getAdvancedComponentForestListNew(5, 6);
        AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> forest = componentForests.get(0);
        AdvancedComponent<FloatType> component = forest.getComponentWithId(componentId);
        Assertions.assertNotNull(component);
        return component;
    }

    private AdvancedComponent<FloatType> getRootComponentNodeWithChildren() throws IOException {
        List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> componentForests = getAdvancedComponentForestList(5, 6);
        AdvancedComponent<FloatType> rootComponent = componentForests.get(0).getComponentWithId("HypT5T420B458L49R60H-1964905642");
        Assertions.assertTrue(isNull(rootComponent.getParent()));
        Assertions.assertFalse(rootComponent.getChildren().isEmpty());
        return rootComponent;
    }

    @NotNull
    private List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> getAdvancedComponentForestList(int frameIndexStart, int frameIndexStop) throws IOException {
        Path imageFilePath = testUtils.getAbsolutTestFilePath("/src/test/resources/00_probability_maps/20201119_VNG1040_AB2h_2h_1__Pos5_GL17/probability_maps__frames430-450__cropped__20201119_VNG1040_AB2h_2h_1_MMStack_Pos5_GL17__model_20210715_5b27d7aa.tif");
        return testUtils.getAdvancedComponentForestList(imageFilePath, frameIndexStart, frameIndexStop);
    }

    @NotNull
    private List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> getAdvancedComponentForestListNew(int frameIndexStart, int frameIndexStop) throws IOException {
        Path testDataFolder = testUtils.getAbsolutTestFilePath("src/test/resources/00_probability_maps/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12/frames_445-460__20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12");
        return testUtils.getComponentForestListFromDataFolder(testDataFolder, frameIndexStart, frameIndexStop, 1.0f);
    }
}
