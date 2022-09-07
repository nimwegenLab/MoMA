package com.jug.util;

import org.junit.Test;
import org.junit.Assert;

public class JarGitVersionParserTest {
    @Test
    public void getVersion__returns_correct_version() {
        JarGitVersionParser sut = new JarGitVersionParser(getJsonJarGitVersionString());
        String versionString = sut.getVersionString();
        Assert.assertEquals("0.6.0-beta4-bbf9f630", versionString);
    }

    private String getJsonJarGitVersionString(){
        return new String("{\n" +
                "    \"git.branch\": \"feature/20220905-implement-version-checking\",\n" +
                "    \"git.build.host\": \"ierbert2\",\n" +
                "    \"git.build.time\": \"20220907-114736\",\n" +
                "    \"git.build.user.email\": \"michael.mell@unibas.ch\",\n" +
                "    \"git.build.user.name\": \"Michael Mell\",\n" +
                "    \"git.build.version\": \"${git.closest.tag.name}.${git.commit.time}.${git.commit.id.abbrev}\",\n" +
                "    \"git.closest.tag.commit.count\": \"17\",\n" +
                "    \"git.closest.tag.name\": \"v0.6.0-beta4\",\n" +
                "    \"git.commit.author.time\": \"20220907-114525\",\n" +
                "    \"git.commit.committer.time\": \"20220907-114525\",\n" +
                "    \"git.commit.id\": \"bbf9f6302eb3e3abafd44f9665dea56b30f54f0b\",\n" +
                "    \"git.commit.id.abbrev\": \"bbf9f63\",\n" +
                "    \"git.commit.id.describe\": \"v0.6.0-beta4-17-gbbf9f63\",\n" +
                "    \"git.commit.id.describe-short\": \"v0.6.0-beta4-17\",\n" +
                "    \"git.commit.message.full\": \"Refactor: Split the reading and parsing of the Git information in the JAR into two classes, so that we can test the parsing and version retrieval in a unit-test.\",\n" +
                "    \"git.commit.message.short\": \"Refactor: Split the reading and parsing of the Git information in the JAR into two classes, so that we can test the parsing and version retrieval in a unit-test.\",\n" +
                "    \"git.commit.time\": \"20220907-114525\",\n" +
                "    \"git.commit.user.email\": \"michael.mell@unibas.ch\",\n" +
                "    \"git.commit.user.name\": \"Michael Mell\",\n" +
                "    \"git.dirty\": \"false\",\n" +
                "    \"git.local.branch.ahead\": \"NO_REMOTE\",\n" +
                "    \"git.local.branch.behind\": \"NO_REMOTE\",\n" +
                "    \"git.remote.origin.url\": \"git@github.com:michaelmell/MoMA.git\",\n" +
                "    \"git.tags\": \"\",\n" +
                "    \"git.total.commit.count\": \"2048\"\n" +
                "}");
    }
}
