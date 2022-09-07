package com.jug.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jug.datahandling.Version;
import org.apache.commons.lang.NotImplementedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GitVersionProvider {
    public String getVersionString() {
        String versionString;
        try {
            versionString = getGitVersionInfo();
        } catch (IOException e) {
            return "git_version_could_not_be_read";
        }
        return versionString;
//        throw new NotImplementedException();
//        return readGitVersion();
    }

    public Version getVersion() {
        String versionString;
        try {
            versionString = getGitVersionInfo();
            return new Version(versionString);
        } catch (IOException e) {
            return new Version("0.0.0-dev");
        }
    }

    private String getGitVersionInfo() throws IOException {
        String jsonString = readGitVersion();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonString);
        String version = rootNode.get("git.closest.tag.name").asText();
        version = version.substring(1);
        String commitId = rootNode.get("git.commit.id").asText().substring(0, 8);
        return version + "+" + commitId;
    }

    private String readGitVersion() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream intputStream = classLoader.getResourceAsStream("git.properties");
        try {
            return readFromInputStream(intputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return "VERSION COULD NOT BE RETRIEVED";
        } catch (NullPointerException e){
            return "VERSION COULD NOT BE RETRIEVED";
        }
    }

    private String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }
}
