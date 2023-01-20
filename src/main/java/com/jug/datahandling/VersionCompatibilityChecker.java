package com.jug.datahandling;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class VersionCompatibilityChecker {
    private static HashMap<Version, Set<Version>> versionCompatibility;
    static {
        versionCompatibility = new HashMap<>();

        versionCompatibility.put(
                new Version("0.9"),
                new HashSet() {{
                    add(new Version("0.9"));
                    add(new Version("0.7"));
                }});
    }

    public boolean versionAreCompatible(Version momaVersionOfRunningInstance, Version momaVersionOfDataset) {
        return momaVersionOfDataset.getMinor() == momaVersionOfRunningInstance.getMinor(); /* TODO: for the moment we enforce that the dataset-version must be of same minor-version, because MoMA is not yet stable */
    }

    public String getErrorMessage(Version momaVersionOfRunningInstance, Version momaVersionOfDataset) {
        return "\tDataset version is incompatible:\n\tDataset version: " + momaVersionOfDataset.toString() + "\n\tMoMA version: " + momaVersionOfRunningInstance.toString();
    }
}
