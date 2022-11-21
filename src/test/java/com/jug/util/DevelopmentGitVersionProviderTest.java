package com.jug.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class DevelopmentGitVersionProviderTest {
    @Test
    public void getVersion__returns_non_null_value(){
        DevelopmentGitVersionProvider sut = new DevelopmentGitVersionProvider();
        Assertions.assertNotNull(sut.getVersion());
    }
}
