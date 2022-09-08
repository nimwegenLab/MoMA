package com.jug.datahandling;

public class VersionCompatibilityChecker {
    public boolean versionAreCompatible(Version momaVersionOfRunningInstance, Version momaVersionOfDataset) {
        return momaVersionOfDataset.getMinor() == momaVersionOfRunningInstance.getMinor(); /* TODO: for the moment we enforce that the dataset-version must be of same minor-version, because MoMA is not yet stable */
    }

    public String getErrorMessage(Version momaVersionOfRunningInstance, Version momaVersionOfDataset) {
        return "\tDataset version is incompatible:\n\tDataset version: " + momaVersionOfDataset.toString() + "\n\tMoMA version: " + momaVersionOfRunningInstance.toString();
    }
}
