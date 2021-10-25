package com.jug.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.NotImplementedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GitVersionProvider {
    public GitVersionProvider() {
    }

    public String getVersionString() {
        String versionString;
        try {
            versionString = getGitVersionInfo();
        } catch (IOException e) {
            return "Error: Json Git Version could not be parsed";
        }
        return versionString;
//        throw new NotImplementedException();
//        return readGitVersion();
    }

    private String getGitVersionInfo() throws IOException {
        String jsonString = readGitVersion();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonString);
        String version = rootNode.get("git.closest.tag.name").asText();
        version = version.substring(1);
        String commitId = rootNode.get("git.commit.id").asText().substring(0, 8);
        return version + "-" + commitId;
    }

    private String readGitVersion() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream intputStream = classLoader.getResourceAsStream("git.properties");
        try {
            return readFromInputStream(intputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return "Version information could not be retrieved";
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
