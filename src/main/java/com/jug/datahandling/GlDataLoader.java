package com.jug.datahandling;

import java.io.File;

/***
 * This class handles the loading of previously curated GLs. It helps in loading the files needed for restoring the
 * previous curation result.
 */
public class GlDataLoader {
    private final String glDataPath;
    private File mmPropertiesPath;

    public GlDataLoader(File mmPropertiesPath) {
        this.mmPropertiesPath = mmPropertiesPath;
        this.glDataPath = mmPropertiesPath.getParent();
    }

    public boolean requiredDataExists() {
        if (!mmPropertiesPath.exists()) {
            return false;
        }
        uNetModelPath =
    }
}
