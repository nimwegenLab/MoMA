package com.jug.util;

import org.junit.Test;
import org.junit.Assert;

public class DevelopmentGitVersionProviderTest {
    @Test
    public void getVersionString__returns_a_value(){
        DevelopmentGitVersionProvider sut = new DevelopmentGitVersionProvider();
        String actual = sut.getVersionString();
        Assert.assertNotNull(actual);
    }
}
