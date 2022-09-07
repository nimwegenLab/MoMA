package com.jug.datahandling;

import com.vdurmont.semver4j.Semver;

public class Version {
    private final Semver semver;

    public Version(String versionString) {
        semver = new Semver(versionString);
    }

    public Integer getMajor() {
        return semver.getMajor();
    }

    public Integer getMinor() {
        return semver.getMinor();
    }

    public Integer getPatch() {
        return semver.getPatch();
    }

    public String getBuild() {
        return semver.getBuild();
    }

    public String getQualifier() {
        return String.join(".", semver.getSuffixTokens());
    }

    public int compareTo(Version version_1_2_3__instance_2) {
        return semver.compareTo(version_1_2_3__instance_2.semver);
    }

    public String toString() {
        return semver.toString();
    }
}
