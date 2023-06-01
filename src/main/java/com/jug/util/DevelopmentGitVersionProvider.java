package com.jug.util;

import com.jug.datahandling.Version;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/***
 * This class gets a version directly from Git to show a version when running from the IDE during development.
 */
public class DevelopmentGitVersionProvider implements IVersionProvider {
    public boolean canReadGitVersionInformation() {
        try {
            readGitVersion();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private String getVersionString() {
        try {
            String var = formatVersionString(readGitVersion());
            return var;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Version getVersion() {
        return new Version(getVersionString());
    }

    public String formatVersionString(String versionString){
        versionString = versionString.substring(1);
        String[] p = versionString.split("-");
        if(versionString.contains("dirty")){
            if(p.length == 3){
                return p[0]+"-"+p[1]+"-"+p[2]; /* versionString has format: "v0.6.0-beta6-dirty" */
            } else if (p.length == 4) {
                return p[0]+"-"+p[1]+"-"+p[3]+"+"+p[2]; /* versionString has format: "v0.7.0-1-g2509542b-dirty" */
            } else if (p.length == 5) {
                return p[0]+"-"+p[1]+"-"+p[2]+"-"+p[4]+"+"+p[3]; /* versionString has format: "v0.6.0-beta5-1-g6a457d3f-dirty" */
            } else {
                throw new RuntimeException("Could not build version string. 'versionString' is of unknown format: " + versionString);
            }
        } else {
            if(p.length == 2) {
                return p[0] + "-" + p[1]; /* versionString has format: "v0.6.0-beta6" */
            }
            else if(p.length == 3) {
                return p[0] + "-" + p[1] + "+" + p[2]; /* versionString has form: "v0.7.0-4-g45bcea65" */
            } else if(p.length == 4) {
                return p[0] + "-" + p[1] + "-" + p[2] + "+" + p[3]; /* versionString has form: "v0.6.0-beta5-1-g6a457d3f" */
            } else {
                throw new RuntimeException("Could not build version string. 'versionString' is of unknown format: " + versionString);
            }
        }
    }

    public String readGitVersion() throws IOException
    {
//        String command = "git describe --abbrev=0 --tags";
        String command = "git describe --abbrev=8 --dirty"; /* describes the closest *annotated* tag*/
        Process p = Runtime.getRuntime().exec(command);
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        return input.readLine();
    }
}
