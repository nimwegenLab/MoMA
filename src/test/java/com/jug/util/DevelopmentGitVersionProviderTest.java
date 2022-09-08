package com.jug.util;

import org.junit.Test;
import org.junit.Assert;

public class DevelopmentGitVersionProviderTest {
    @Test
    public void getVersion__returns_non_null_value(){
        DevelopmentGitVersionProvider sut = new DevelopmentGitVersionProvider();
        Assert.assertNotNull(sut.getVersion());
    }
}
