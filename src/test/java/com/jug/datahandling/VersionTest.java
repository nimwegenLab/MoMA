package com.jug.datahandling;

import org.junit.Test;
import org.junit.Assert;

import com.vdurmont.semver4j.Semver;

public class VersionTest {
    @Test
    public void Semver__test_1(){
        Semver ver1 = new Semver("1.2.3+1231241");
        Semver ver2 = new Semver("1.2.3+1231241");
//        System.out.println("");
        Assert.assertTrue(ver1.isEqualTo(ver2));
    }

    @Test
    public void getMajor__returns_correct_value(){
        Version sut = new Version("1.2.3");
        Assert.assertEquals(1, (int) sut.getMajor());
    }

    @Test
    public void getMinor__returns_correct_value(){
        Version sut = new Version("1.2.3");
        Assert.assertEquals(2, (int) sut.getMinor());
    }

    @Test
    public void getPatch__returns_correct_value(){
        Version sut = new Version("1.2.3");
        Assert.assertEquals(3, (int) sut.getPatch());
    }

    @Test
    public void getBuild__returns_correct_value(){
        Version sut = new Version("1.2.3+77305e1f");
        String actual = sut.getBuild();
        Assert.assertEquals("77305e1f", actual);
    }

    @Test
    public void getQualifier__returns_correct_value_1(){
        Version sut = new Version("1.2.3-beta.1+77305e1f");
        String actual = sut.getQualifier();
        Assert.assertEquals("beta.1", actual);
    }

    @Test
    public void getQualifier__returns_correct_value_2(){
        Version sut = new Version("1.2.3-alpha.2+77305e1f");
        String actual = sut.getQualifier();
        Assert.assertEquals("alpha.2", actual);
    }
}
