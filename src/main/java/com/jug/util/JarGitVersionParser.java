package com.jug.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

/***
 * This class parses the version from Git version-information stored in the JSON file inside the JAR of build.
 */
public class JarGitVersionParser implements IVersionProvider {
    private String jsonGitInformationString;

    public JarGitVersionParser(String jsonGitInformationString) {
        this.jsonGitInformationString = jsonGitInformationString;
    }

    public String getVersionString() {
        String versionString;
        try {
            versionString = getGitVersionInfo(this.jsonGitInformationString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return versionString;
    }

    private String getGitVersionInfo(String jsonString) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonString);
        String version = rootNode.get("git.closest.tag.name").asText();
        version = version.substring(1);
        String commitId = rootNode.get("git.commit.id").asText().substring(0, 8);
        return version + "+" + commitId;
    }
}
