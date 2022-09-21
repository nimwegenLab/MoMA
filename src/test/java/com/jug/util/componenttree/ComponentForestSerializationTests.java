package com.jug.util.componenttree;

import com.google.gson.Gson;
import com.jug.util.TestUtils;
import net.imglib2.type.numeric.real.FloatType;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import scala.NotImplementedError;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.isNull;

public class ComponentForestSerializationTests {

    private final TestUtils testUtils;

    public ComponentForestSerializationTests() {
        testUtils = new TestUtils();
    }

//    public static void main(String... args) throws IOException, InterruptedException {
//        new ComponentForestSerializationTests().serializing_and_deserializing_component_yields_equal_component();
//    }

    @Test
    public void isequal__for_json_serialized_copy_of_list_of_component_trees__is_true() throws IOException {
        List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> componentForests = getAdvancedComponentForests(0, 5);
        ComponentForestSerializer sut = new ComponentForestSerializer();
        String jsonString = sut.serializeToJson(componentForests);
        throw new NotImplementedError();
//        Path jsonFile = Files.createTempFile("", ".json");

//        for (AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> tree : componentForests) {
//            Plotting.drawComponentTree2(tree, new ArrayList<>(), tree.rootsSorted().get(0).getSourceImage());
//        }
//        List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> componentForests = getAdvancedComponentForests(0, 2);
//        String jsonString = new Gson().toJson(componentForests);
//        AdvancedComponentPojo pojo_new = new Gson().fromJson(jsonString, AdvancedComponentPojo.class);
    }

    @Test
    public void hashCode__for_json_serialized_copy__is_equal() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getInternalComponentNode();
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);
        Assert.assertEquals(sutComponent.hashCode(), componentDeserialized.hashCode());
    }

    @Test
    public void hashCode__for_json_serialized_copy__is_not_equal_to_default_value_of_777() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getInternalComponentNode();
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);
        Assert.assertNotEquals(777, componentDeserialized.hashCode());
    }

    @Test
    public void value_field__for_json_serialized_copy__is_equal() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getInternalComponentNode();
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);
        Assert.assertNotEquals(0.0, sutComponent.value().getRealDouble()); /* make sure value is not 0.0 to make the test more expressive, because it could be that in future changes the value is set to 0 by default */
        Assert.assertEquals(sutComponent.value(), componentDeserialized.value());
    }

    @Test
    public void getChildStringIds__for_json_serialized_copy_of_leaf_component_node__returns_empty_list() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getLeafComponentNode();
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);
        Assert.assertEquals(sutComponent.getChildrenStringIds(), componentDeserialized.getChildrenStringIds());
    }

    @Test
    public void getChildStringIds__for_json_serialized_copy_of_root_component_node_with_children__returns_correct_value() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getRootComponentNodeWithChildren();
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);
        Assert.assertEquals(sutComponent.getChildrenStringIds(), componentDeserialized.getChildrenStringIds());
    }

    @Test
    public void getChildStringIds__for_leaf_component_node__returns_empty_list() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getLeafComponentNode();
        List<String> childStringIds = sutComponent.getChildrenStringIds();
        Assert.assertTrue(childStringIds.isEmpty());
    }

    @Test
    public void getChildStringIds__for_root_component_node_with_children__returns_correct_value() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getRootComponentNodeWithChildren();
        List<String> childStringIds = sutComponent.getChildrenStringIds();
        String[] expected = new String[]{"HypT5T420B429L51R60H1646872133", "HypT5T430B458L49R59H-2058293790"};
        Assert.assertEquals(Arrays.asList(expected), childStringIds);
        Assert.assertEquals("HypT5T420B429L51R60H1646872133", childStringIds.get(0));
        Assert.assertEquals("HypT5T430B458L49R59H-2058293790", childStringIds.get(1));
    }

    @Test
    public void getParentStringId__for_json_serialized_copy_of_internal_component_node__is_equal() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getInternalComponentNode();
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);
        Assert.assertEquals(sutComponent.getParentStringId(), componentDeserialized.getParentStringId());
    }

    @Test
    public void getParentStringId__for_json_serialized_copy_of_root_component_node__is_equal() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getRootComponentNodeWithChildren();
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);
        Assert.assertEquals(sutComponent.getParentStringId(), componentDeserialized.getParentStringId());
    }

    @Test
    public void getLabel__for_json_serialized_copy__is_equal() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getRootComponentNodeWithChildren();
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);
        Assert.assertEquals(sutComponent.getLabel(), componentDeserialized.getLabel());
    }

    @Test
    public void getParentStringId__for_internal_component_node__returns_correct_value() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getInternalComponentNode();
        Assert.assertEquals(sutComponent.getParentStringId(), "HypT5T280B395L47R59H725080955");
    }

    @Test
    public void getParentStringId__for_root_component__returns_NA() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getRootComponentNodeWithChildren();
        Assert.assertEquals(sutComponent.getParentStringId(), "NA");
    }

    @Test
    public void getFrameNumber__for_json_serialized_copy__is_equal() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getRootComponentNodeWithChildren();
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);
        Assert.assertEquals(sutComponent.getFrameNumber(), componentDeserialized.getFrameNumber());
    }

    @Test
    public void getStringId__for_json_serialized_copy__is_equal() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getRootComponentNodeWithChildren();
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);
        Assert.assertEquals(sutComponent.getStringId(), componentDeserialized.getStringId());
    }

    @Test
    public void equals__for_two_different_components__returns_false() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getRootComponentNodeWithChildren();
        AdvancedComponent<FloatType> leafComponent = getLeafComponentNode();
        Assert.assertNotEquals(sutComponent, leafComponent);
    }

    @Test
    public void equals__for_json_serialized_copy_returns__returns_true() throws IOException {
        AdvancedComponent<FloatType> sutComponent = getRootComponentNodeWithChildren();
        AdvancedComponent<FloatType> componentDeserialized = serializeAndDeserializeThroughJsonString(sutComponent);
        Assert.assertEquals(sutComponent, componentDeserialized);
    }

    @NotNull
    private AdvancedComponent<FloatType> serializeAndDeserializeThroughJsonString(AdvancedComponent<FloatType> sutComponent) {
        AdvancedComponentPojo pojo = sutComponent.getSerializableRepresentation();
        String jsonString = new Gson().toJson(pojo);
        AdvancedComponentPojo pojo_new = new Gson().fromJson(jsonString, AdvancedComponentPojo.class);
        AdvancedComponent<FloatType> componentDeserialized = AdvancedComponent.createFromPojo(pojo_new, testUtils.getComponentProperties());
        return componentDeserialized;
    }

    private AdvancedComponent<FloatType> getLeafComponentNode() throws IOException {
        List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> componentForests = getAdvancedComponentForests(5, 6);
        AdvancedComponent<FloatType> sutComponent = componentForests.get(0).getAllComponents().get(17);
        Assert.assertTrue(sutComponent.getChildren().isEmpty());
        Assert.assertFalse(isNull(sutComponent.getParent()));
        return sutComponent;
    }

    private AdvancedComponent<FloatType> getInternalComponentNode() throws IOException {
        List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> componentForests = getAdvancedComponentForests(5, 6);
        AdvancedComponent<FloatType> sutComponent = componentForests.get(0).getAllComponents().get(12);
        Assert.assertFalse(sutComponent.getChildren().isEmpty());
        Assert.assertFalse(isNull(sutComponent.getParent()));
        return sutComponent;
    }

    private AdvancedComponent<FloatType> getRootComponentNodeWithChildren() throws IOException {
        List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> componentForests = getAdvancedComponentForests(5, 6);
        AdvancedComponent<FloatType> sutComponent = componentForests.get(0).getAllComponents().get(2);
        Assert.assertTrue(isNull(sutComponent.getParent()));
        Assert.assertFalse(sutComponent.getChildren().isEmpty());
        return sutComponent;
    }

    @NotNull
    private List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> getAdvancedComponentForests(int frameIndexStart, int frameIndexStop) throws IOException {
        String imageFile = new File("").getAbsolutePath() + "/src/test/resources/00_probability_maps/20201119_VNG1040_AB2h_2h_1__Pos5_GL17/probability_maps__frames430-450__cropped__20201119_VNG1040_AB2h_2h_1_MMStack_Pos5_GL17__model_20210715_5b27d7aa.tif";

        List<AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>> componentForests = new ArrayList<>();
        for (int frameIndex = frameIndexStart; frameIndex < frameIndexStop; frameIndex++) {
            componentForests.add((AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>>) testUtils.getComponentTreeFromProbabilityImage(imageFile, frameIndex, 1.0f));
        }
        return componentForests;
    }
}
