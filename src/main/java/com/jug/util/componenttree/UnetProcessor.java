package com.jug.util.componenttree;

import com.jug.MoMA;
import com.jug.util.Hash;
import de.csbdresden.csbdeep.commands.GenericNetwork;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ops.OpService;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.scijava.Context;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

/**
 * Responsible for processing the input image using U-Net to produce probability maps of where cells are located in the
 * image. It reads a trained model from disk and uses CSBDeep to run the model on an image stack with the method
 * {@link #process(Img<FloatType>)}.
 */
public class UnetProcessor {
    private String modelFile = "";
    private OpService ops;
    private final Context context;
    private final CommandService commandService;
    private final DatasetService datasetService;

    public UnetProcessor(){
        modelFile = getModelFilePath();
        System.out.println("Model file: " + modelFile);
        context = new Context();
        ops = context.service(OpService.class);
        commandService = context.service(CommandService.class);
        datasetService = context.service(DatasetService.class);
    }

    public String getModelChecksum() {
        return calculateModelChecksum(modelFile);
    }

    private String getModelFilePath() {
        try {
            String mainClassParentPath = new File(MoMA.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getParent();
            return mainClassParentPath + "/unet_models/current_tensorflow_model.zip";

        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String calculateModelChecksum(String modelFile) {
        byte[] hash = Hash.SHA256.checksum(new File(modelFile));
        return Hash.toHex(hash).toLowerCase();
    }

    /**
     * Load and run the Tensor-Flow network on the images to create probability maps
     * of the cell-location.

     * @param inputImage input image
     * @return processed image (probability map)
     */

    public Img<FloatType> process(Img<FloatType> inputImage) {
        try {
            Dataset dataset = datasetService.create(Views.zeroMin(inputImage)); // WHY DO WE NEED ZEROMIN HERE?!
            final CommandModule module = commandService.run(
                    GenericNetwork.class, false,
                    "input", dataset,
                    "modelFile", modelFile,
                    "normalizeInput", false,
                    "blockMultiple", 8,
                    "nTiles", 1,
                    "showProgressDialog", true).get();
            Img<FloatType> processedImage = (Img<FloatType>) module.getOutput("output");
            return processedImage;

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }
}
