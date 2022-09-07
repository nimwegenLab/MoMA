package com.jug.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/***
 * This class gets a version directly from Git to show a version when running from the IDE during development.
 */
public class DevelopmentGitVersionProvider implements IVersionProvider {
    @Override
    public String getVersionString() {
        try {
            String var = readGitVersion();
            return var;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String readGitVersion() throws IOException
    {
        String command = "git describe --abbrev=0 --tags";
        Process p = Runtime.getRuntime().exec(command);
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        return input.readLine();
    }
}
