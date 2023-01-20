package com.jug.datahandling;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class VersionCompatibilityChecker {
    private static HashMap<String, Set<String>> datasetCompatibility;
    static {
        datasetCompatibility = new HashMap<>(); /* this maps the MoMA version (the map key) to compatible dataset-version (the mapped set) */
        datasetCompatibility.put(
                "0.9",
                new HashSet() {{
                    add("0.9");
                    add("0.7");
                }});
    }

    public Set<String> getCompatibleDatasetVersions(String momaVersion) throws NoVersionFoundException {
        if (!datasetCompatibility.containsKey(momaVersion)) {
            throw new NoVersionFoundException();
        }
        return datasetCompatibility.get(momaVersion);
    }

    public boolean versionAreCompatible(Version momaVersionOfRunningInstance, Version momaVersionOfDataset) {
        String versionString = getVersionString(momaVersionOfRunningInstance);
        Set<String> compatibleDataVersions;
        try {
            compatibleDataVersions = getCompatibleDatasetVersions(versionString);
        } catch (NoVersionFoundException e) {
            return false;
        }
        String datasetVersionString = getVersionString(momaVersionOfDataset);
        boolean versionIsCompatible = compatibleDataVersions.contains(datasetVersionString);
        return versionIsCompatible;
    }

    private String getVersionString(Version version) {
        return String.format("%d.%d", version.getMajor(), version.getMinor());
    }

    public String getErrorMessage(Version momaVersionOfRunningInstance, Version momaVersionOfDataset) {
        return "\tDataset version is incompatible:\n\tDataset version: " + momaVersionOfDataset.toString() + "\n\tMoMA version: " + momaVersionOfRunningInstance.toString();
    }

    class NoVersionFoundException extends Exception { }
}
