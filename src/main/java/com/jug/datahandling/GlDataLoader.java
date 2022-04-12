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

    public boolean propertiesFileExists() {
        return mmPropertiesPath.exists();
    }


//    private Img<FloatType> processImageWithUnetOrLoadFromDisk() {
//        String checksum = unetProcessor.getModelChecksum();
//        /**
//         *  generate probability filename
//         */
//        File file = new File(configurationManager.getImagePath());
//        if(file.isDirectory()){
//            File[] list = file.listFiles();
//            file = new File(list[0].getAbsolutePath()); /* we were passed a folder, but we want the full file name, for storing the probability map with correct name */
//        }
//        String outputFolderPath = file.getParent();
//        String filename = removeExtension(file.getName());
//        String processedImageFileName = outputFolderPath + "/" + filename + "__model_" + checksum + ".tif";
//
//        /**
//         *  create or load probability maps
//         */
//        Img<FloatType> probabilityMap;
//        if (!new File(processedImageFileName).exists()) {
//            probabilityMap = unetProcessor.process(imgTemp);
//            ImagePlus tmp_image = ImageJFunctions.wrap(probabilityMap, "tmp_image");
//            IJ.saveAsTiff(tmp_image, processedImageFileName);
//        } else {
//            ImagePlus imp = IJ.openImage(processedImageFileName);
//            probabilityMap = ImageJFunctions.convertFloat(imp);
//        }
//        return probabilityMap;
//    }

//    /**
//     * Opens all tiffs in the given folder, straightens and crops images,
//     * extracts growth lines, subtracts background, builds segmentation
//     * hypothesis and a Markov random field for tracking. Finally it even solves
//     * this model using Gurobi and reads out the MAP.
//     *
//     * Note: multi-channel assumption is that filename encodes channel by
//     * containing a substring of format "_c%02d".
//     *
//     * @param path
//     *            the folder to be processed.
//     * @param minTime
//     * @param maxTime
//     * @param minChannelIdx
//     * @param numChannels
//     * @throws Exception
//     */
//    ArrayList<Img<FloatType>> rawChannelImgs;
//    Img<FloatType> imgRaw;
//    private void processDataFromFolder( final String path, final int minTime, final int maxTime, final int minChannelIdx, final int numChannels ) throws Exception {
//
//        if ( numChannels == 0 ) { throw new Exception( "At least one color channel must be loaded!" ); }
//
//        // load channels separately into Img objects
//        rawChannelImgs = FloatTypeImgLoader.loadTiffsFromFileOrFolder(path, minTime, maxTime, minChannelIdx, numChannels + minChannelIdx - 1);
//
//        imgRaw = rawChannelImgs.get(0);
//
//        restartFromGLSegmentation(this);
//
//        if ( HEADLESS ) {
//            System.out.println( "Generating Integer Linear Program(s)..." );
//            generateILPs();
//            System.out.println( " done!" );
//
//            System.out.println( "Running Integer Linear Program(s)..." );
//            runILPs();
//            System.out.println( " done!" );
//        }
//    }
}
