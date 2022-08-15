package com.jug.datahandling;

public class VersionCompatibilityChecker {
    public boolean versionAreCompatible(String momaVersionOfRunningInstance, String momaVersionOfDataset) {
        if (momaVersionOfRunningInstance.contains("git_version_could_not_be_read") || momaVersionOfDataset.contains("git_version_could_not_be_read")) {
            return true; /* we are running in the IDE */
        }
        return momaVersionOfDataset.equals(momaVersionOfRunningInstance);
    }

    public String getErrorMessage(String momaVersionOfRunningInstance, String momaVersionOfDataset) {
        return "\tDataset version is incompatible:\n\tDataset version: " + momaVersionOfDataset + "\n\tMoMA version: " + momaVersionOfRunningInstance;
    }
}
