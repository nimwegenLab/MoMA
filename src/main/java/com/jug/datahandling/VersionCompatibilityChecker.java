package com.jug.datahandling;

public class VersionCompatibilityChecker {
    public boolean versionStringsAreIdentical(String momaVersionOfRunningInstance, String momaVersionOfDataset) {
        if (momaVersionOfRunningInstance.contains("git_version_could_not_be_read") || momaVersionOfDataset.contains("git_version_could_not_be_read")) {
            return true; /* we are running in the IDE */
        }
        return momaVersionOfDataset.equals(momaVersionOfRunningInstance);
    }

    public boolean versionAreCompatible(Version momaVersionOfRunningInstance, Version momaVersionOfDataset) {
        return momaVersionOfDataset.getMinor() == momaVersionOfRunningInstance.getMinor(); /* TODO: for the moment we enforce that the dataset-version must be of same minor-version, because MoMA is not yet stable */
    }

    public String getErrorMessage(String momaVersionOfRunningInstance, String momaVersionOfDataset) {
        return "\tDataset version is incompatible:\n\tDataset version: " + momaVersionOfDataset + "\n\tMoMA version: " + momaVersionOfRunningInstance;
    }
}
