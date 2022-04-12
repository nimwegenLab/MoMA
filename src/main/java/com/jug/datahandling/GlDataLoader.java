package com.jug.datahandling;

import com.jug.util.componenttree.UnetProcessor;
import org.apache.commons.lang.NotImplementedException;

import java.io.File;

/***
 * This class handles the loading of previously curated GLs. It helps in loading the files needed for restoring the
 * previous curation result.
 */
public class GlDataLoader {
    private final String glDataPath;
    private File mmPropertiesPath;

    public GlDataLoader(File mmPropertiesPath, UnetProcessor unetProcessor) {
        this.mmPropertiesPath = mmPropertiesPath;
        this.glDataPath = mmPropertiesPath.getParent();
    }

    public boolean requiredDataExists() {
        if (!mmPropertiesPath.exists()) {
            return false;
        }
//        uNetModelPath =
        throw new NotImplementedException();
    }
}
