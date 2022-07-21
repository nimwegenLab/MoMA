package com.jug.datahandling;

public class VersionCompatibilityChecker {
    public boolean versionAreCompatible(String momaVersionOfRunningInstance, String momaVersionOfDataset) {
        return momaVersionOfDataset.equals(momaVersionOfRunningInstance);
    }

    public String getErrorMessage(String momaVersionOfRunningInstance, String momaVersionOfDataset){
        return "\tDataset version is incompatible:\n\tDataset version: " + momaVersionOfDataset + "\n\tMoMA version: " + momaVersionOfRunningInstance;
    }
}
