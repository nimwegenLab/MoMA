package com.jug.util.componenttree;

import com.jug.MoMA;
import de.csbdresden.csbdeep.commands.GenericNetwork;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ops.OpService;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
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
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

/**
 * Responsible for processing the input image using U-Net to produce probability maps of where cells are located in the
 * image. It reads a trained model from disk and uses CSBDeep to run the model on an image stack with the method
 * {@link #process(Img<FloatType>)}.
 */
public class UnetProcessor {
    private String modelFile = "";
    private long model_input_width;
    private long model_input_height;
    private OpService ops;
    private final Context context;
    private final CommandService commandService;
    private final DatasetService datasetService;

    public UnetProcessor(){
        model_input_width = 32;
        model_input_height = 512;

        modelFile = getModelFilePath();
        System.out.println("Model file: " + modelFile);

        context = new Context();
        ops = context.service(OpService.class);
        commandService = context.service(CommandService.class);
        datasetService = context.service(DatasetService.class);
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

    /**
     * Load and run the Tensor-Flow network on the images to create probability maps
     * of the cell-location.

     * @param inputImage input image
     * @return processed image (probability map)
     */
    public Img<FloatType> process(Img<FloatType> inputImage) {
        try {
            inputImage = (Img)normalizeToPercentiles(inputImage, 0.4, 99.4);
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
                new long[]{start_index_horz, img.dimension(1) - model_input_height, 0},
                new long[]{end_index_horz, img.dimension(1) - 1, img.dimension(2) - 1}
        );
    }

    /**
     * Normalize intensity range of each image based on the provided percentile values of the *whole image stack*.
     *
     * @param image input image
     * @param lowerPercentile lower percentile determining the lower value of the intensity range
     * @param upperPercentile upper percentile determining the lower value of the intensity range
     * @return normalized image
     */
    private Iterable<FloatType> normalizeToPercentiles(Img<FloatType> image, double lowerPercentile, double upperPercentile) {
        int dim = 2;
        long limit = image.dimension(dim);

        float min_percentile = ops.stats().percentile(image, lowerPercentile).getRealFloat();
        float max_percentile = ops.stats().percentile(image, upperPercentile).getRealFloat();

        for(int i=0; i<limit; i++){
            RandomAccessibleInterval<FloatType> view = Views.hyperSlice( image, dim, i );

            float intensityDifference = max_percentile - min_percentile;
            ((Iterable<FloatType>) view).forEach(t -> {
                final float val = t.getRealFloat();
                if (val < min_percentile) t.set(0);
                else if (val > max_percentile) t.set(1.0f);
                else t.set((val - min_percentile) / intensityDifference);
            });
        }
        return image;
    }
}
