package com.jug.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class DevelopmentGitVersionProviderTest {
    @Test
    void formatVersionString__returns_correct_version_string_1() {
        DevelopmentGitVersionProvider sut = new DevelopmentGitVersionProvider();
        String actual = sut.formatVersionString("v0.6.0-beta6-dirty");
        Assertions.assertEquals("0.6.0-beta6-dirty", actual);
    }

    @Test
    void formatVersionString__returns_correct_version_string_2() {
        DevelopmentGitVersionProvider sut = new DevelopmentGitVersionProvider();
        String actual = sut.formatVersionString("v0.7.0-1-g2509542b-dirty");
        Assertions.assertEquals("0.7.0-1-dirty+g2509542b", actual);
    }

    @Test
    void formatVersionString__returns_correct_version_string_3() {
        DevelopmentGitVersionProvider sut = new DevelopmentGitVersionProvider();
        String actual = sut.formatVersionString("v0.6.0-beta5-1-g6a457d3f-dirty");
        Assertions.assertEquals("0.6.0-beta5-1-dirty+g6a457d3f", actual);
    }

    @Test
    void formatVersionString__returns_correct_version_string_4() {
        DevelopmentGitVersionProvider sut = new DevelopmentGitVersionProvider();
        String actual = sut.formatVersionString("v0.6.0-beta6");
        Assertions.assertEquals("0.6.0-beta6", actual);
    }

    @Test
    void formatVersionString__returns_correct_version_string_5() {
        DevelopmentGitVersionProvider sut = new DevelopmentGitVersionProvider();
        String actual = sut.formatVersionString("v0.7.0-4+g45bcea65");
        Assertions.assertEquals("0.7.0-4+g45bcea65", actual);
    }

    @Test
    void formatVersionString__returns_correct_version_string_6() {
        DevelopmentGitVersionProvider sut = new DevelopmentGitVersionProvider();
        String actual = sut.formatVersionString("v0.6.0-beta5-1-g6a457d3f");
        Assertions.assertEquals("0.6.0-beta5-1+g6a457d3f", actual);
    }
}
