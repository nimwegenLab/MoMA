package com.jug.datahandling;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.vdurmont.semver4j.Semver;

public class Version {
    private final transient Semver semver;

    private String versionString;

    private Version() {
        this.semver = new Semver(this.versionString);
    }

    public Version(String versionString) {
        this.versionString = versionString;
        semver = new Semver(this.versionString);
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

//    public String toJson() {
//        return new Gson().toJson(this);
//    }

//    static Version fromJson(String jsonInput) {
//        VersionSerializationClass versionSerializationClass = new Gson().fromJson(jsonInput, VersionSerializationClass.class);
//        return new Version(versionSerializationClass.versionString);
//    }

//    private class VersionSerializationClass {
//        private final String versionString;
//
//        public How can I convert JSON to a HashMap using Gson?(Version version) {
//            versionString = version.toString();
//        }
//    }
}
