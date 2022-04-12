package com.jug.datahandling;

import com.jug.config.ConfigurationManager;
import com.jug.util.FloatTypeImgLoader;
import com.jug.util.componenttree.UnetProcessor;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;

import java.io.File;
import java.util.ArrayList;

import static org.apache.commons.io.FilenameUtils.removeExtension;

/***
 * This class handles the loading of previously curated GLs. It helps in loading the files needed for restoring the
 * previous curation result.
 */
public class GlDataLoader {
    private final String glDataPath;
    private UnetProcessor unetProcessor;
    private ConfigurationManager configurationManager;
    private File mmPropertiesPath;

    public GlDataLoader(File mmPropertiesPath, UnetProcessor unetProcessor, ConfigurationManager configurationManager) {
        this.mmPropertiesPath = mmPropertiesPath;
        this.glDataPath = mmPropertiesPath.getParent();
        this.unetProcessor = unetProcessor;
        this.configurationManager = configurationManager;
    }
}
