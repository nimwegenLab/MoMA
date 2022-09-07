package com.jug.datahandling;

import org.junit.Test;
import org.junit.Assert;

public class VersionCompatibilityCheckerTest {
    @Test
    public void versionAreCompatible__for_same_versions__returns_true() {
        VersionCompatibilityChecker sut = new VersionCompatibilityChecker();
        boolean res = sut.versionAreCompatible("v0.6.0-beta4-625ef450", "v0.6.0-beta4-625ef450");
        Assert.assertTrue(res);
    }

    @Test
    public void versionAreCompatible__for_different_build_string__returns_true() {
        VersionCompatibilityChecker sut = new VersionCompatibilityChecker();
        boolean res = sut.versionAreCompatible("v0.6.0-beta4-625ef450", "v0.6.0-beta4-1234567");
        Assert.assertTrue(res);
    }
}
