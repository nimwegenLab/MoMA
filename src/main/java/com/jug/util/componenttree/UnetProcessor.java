package com.jug.util.componenttree;

import com.jug.util.Hash;
import de.csbdresden.csbdeep.commands.GenericNetwork;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imglib2.FinalInterval;
import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.jetbrains.annotations.NotNull;
import org.scijava.Context;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * Responsible for processing the input image using U-Net to produce probability maps of where cells are located in the
 * image. It reads a trained model from disk and uses CSBDeep to run the model on an image stack with the method
 * {@link #process(Img<FloatType>)}.
 */
public class UnetProcessor {
    private String modelFile = "";
    private long model_input_width;
    private final CommandService commandService;
    private final DatasetService datasetService;

    public UnetProcessor(Context context){
        model_input_width = 32;  // TODO-MM-20210723: This should be a user-parameter under in the segmentation section of the config editor.
        commandService = context.service(CommandService.class);
        datasetService = context.service(DatasetService.class);
    }

    public String getModelChecksum() {
        return calculateModelChecksum(modelFile);
    }

    public void setModelFilePath(String modelFilePath) {
        modelFile = modelFilePath;
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
            FinalInterval roiForNetworkProcessing = getRoiForUnetProcessing(inputImage);
            IntervalView<FloatType> newImg = getReshapedImageForProcessing(inputImage, roiForNetworkProcessing);
            Dataset dataset = datasetService.create(Views.zeroMin(newImg)); // WHY DO WE NEED ZEROMIN HERE?!
            final CommandModule module = commandService.run(
                    GenericNetwork.class, false,
                    "input", dataset,
                    "modelFile", modelFile,
                    "normalizeInput", false,
                    "blockMultiple", 8,
                    "nTiles", 1,
                    "showProgressDialog", true).get();
            Img<FloatType> processedImage = (Img<FloatType>) module.getOutput("output");
            return reshapeProcessedImageToOriginalSize(inputImage, roiForNetworkProcessing, processedImage);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Reshape the input image to fit the shape of the input layer of the U-Net.
     *
     * @param originalImage image with the original shape
     * @param roiForNetworkProcessing ROI with the original shape.
     * @param processedImage
     * @return
     */
    private Img<FloatType> reshapeProcessedImageToOriginalSize(Img<FloatType> originalImage, FinalInterval roiForNetworkProcessing, Img<FloatType> processedImage) {
        final Img<FloatType> outputImg = originalImage.factory().create(originalImage);
        ExtendedRandomAccessibleInterval extendedImage = Views.extend(outputImg, new OutOfBoundsConstantValueFactory(new FloatType(0.0f)));
        IntervalView<FloatType> roiImgInterval = Views.zeroMin(Views.interval(extendedImage, roiForNetworkProcessing));
        LoopBuilder.setImages( processedImage, roiImgInterval ).forEachPixel( (in, out ) -> out.set( in ) );
        return outputImg;
    }

    /**
     * Reshape the output image that is obtained from U-Net to the original image size.
     *
     * @param img image to reshape.
     * @param roiForNetworkProcessing ROI with the original shape.
     * @return reshaped image.
     */
    @NotNull
    private IntervalView<FloatType> getReshapedImageForProcessing(Img<FloatType> img, FinalInterval roiForNetworkProcessing) {
        ExtendedRandomAccessibleInterval extendedImage = Views.extend(img, new OutOfBoundsConstantValueFactory(new FloatType(0.0f)));
        return Views.interval(extendedImage, roiForNetworkProcessing);
    }

    /**
     * Returns the ROI of the image that will be used for image processing.
     * @param img
     * @return
     */
    @NotNull
    private FinalInterval getRoiForUnetProcessing(Img<FloatType> img) {
        long start_index_horz = img.dimension(0)/2 - model_input_width/2;
        long end_index_horz = start_index_horz + model_input_width - 1;
        return new FinalInterval(
                new long[]{start_index_horz, img.min(1), 0},
                new long[]{end_index_horz, img.max(1), img.max(2)}
        );
    }
}
