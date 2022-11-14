package com.jug.datahandling;

import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assertions;

public class VersionTest {
    @Test
    public void getMajor__returns_correct_value(){
        Version sut = new Version("1.2.3");
        Assertions.assertEquals(1, (int) sut.getMajor());
    }

    @Test
    public void getMinor__returns_correct_value(){
        Version sut = new Version("1.2.3");
        Assertions.assertEquals(2, (int) sut.getMinor());
    }

    @Test
    public void getPatch__returns_correct_value(){
        Version sut = new Version("1.2.3");
        Assertions.assertEquals(3, (int) sut.getPatch());
    }

    @Test
    public void getBuild__returns_correct_value(){
        Version sut = new Version("1.2.3+77305e1f");
        String actual = sut.getBuild();
        Assertions.assertEquals("77305e1f", actual);
    }

    @Test
    public void getQualifier__returns_correct_value_1(){
        Version sut = new Version("1.2.3-beta.1+77305e1f");
        String actual = sut.getQualifier();
        Assertions.assertEquals("beta.1", actual);
    }

    @Test
    public void getQualifier__returns_correct_value_2(){
        Version sut = new Version("1.2.3-alpha.2+77305e1f");
        String actual = sut.getQualifier();
        Assertions.assertEquals("alpha.2", actual);
    }

    @Test
    public void compareTo__for_same_versions__returns_equality(){
        Version version_1_2_3__instance_1 = new Version("1.2.3+77305e1f");
        Version version_1_2_3__instance_2 = new Version("1.2.3+77305e1f");
        Assertions.assertEquals(0, version_1_2_3__instance_1.compareTo(version_1_2_3__instance_2));
    }

    @Test
    public void compareTo__for_same_versions_with_and_without_build_id__returns_equality(){
        Version version_1_2_3 = new Version("1.2.3");
        Version version_1_2_3__with_build_id = new Version("1.2.3+77305e1f");
        Assertions.assertEquals(0, version_1_2_3.compareTo(version_1_2_3__with_build_id));
    }

    @Test
    public void compareTo__for_version_with_qualifier__returns_lower_version(){
        Version version_1_2_3 = new Version("1.2.3");
        Version version_1_2_3__with_qualifier = new Version("1.2.3-beta.1");
        Assertions.assertTrue(version_1_2_3.compareTo(version_1_2_3__with_qualifier) > 0);
        Assertions.assertTrue(version_1_2_3__with_qualifier.compareTo(version_1_2_3) < 0);
    }

    @Test
    public void toString__returns_original_version_string(){
        String expected = "1.2.3-beta.1-dirty+77305e1f";
        Version version = new Version(expected);
        Assertions.assertEquals(expected, version.toString());
    }

    @Test
    public void round_trip_conversion_through_json_gives_same_result() {
        String expected = "1.2.3-beta.1-dirty+77305e1f";
        Version version = new Version(expected);
        Version versionDeserialized = Version.fromJson(version.toJson());
        Assertions.assertEquals(0, version.compareTo(versionDeserialized));
    }
}
