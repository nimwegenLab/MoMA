package com.jug.util.componenttree;

import de.csbdresden.csbdeep.commands.GenericNetwork;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ops.OpService;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.jetbrains.annotations.NotNull;
import org.scijava.Context;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;

import java.util.concurrent.ExecutionException;

/**
 * Responsible for processing the input image using U-Net to produce probability maps of where cells are located in the
 * image.
 */
public class UnetProcessor {
    private final String modelFile;
    private long model_input_width;
    private long model_input_height;
    private OpService ops;
    private final Context context;
    private final CommandService commandService;
    private final DatasetService datasetService;

    public UnetProcessor(){
        model_input_width = 32;
        model_input_height = 512;
//        modelFile = "/home/micha/Documents/01_work/DeepLearning/Moma_Deep_Learning/DeepLearningMoM/model_export/reformated_model_20180706_GW296_glycerol37_1_MMStack/model.zip";
//        modelFile = "/home/micha/Documents/01_work/DeepLearning/Moma_Deep_Learning/DeepLearningMoM/model_export/2019-07-11_first_test/test_2/tensorflow_model_reformatted/tensorflow_model/model.zip";
//        modelFile = "/home/micha/Documents/01_work/DeepLearning/00_deep_moma/02_model_training/00_phase_contrast_unet_segmentation/model/tensorflow_model_csbdeep.zip";
//        modelFile = "/home/micha/Documents/01_work/DeepLearning/00_deep_moma/02_model_training/00_phase_contrast_unet_segmentation/model/models/20190805-132335_c44e6f01/tensorflow_model_csbdeep.zip";
//        modelFile = "/home/micha/Documents/01_work/DeepLearning/00_deep_moma/02_model_training/00_phase_contrast_unet_segmentation/model/models/20190805-135715_5547a42c/tensorflow_model_csbdeep.zip";
//        modelFile = "/home/micha/Documents/01_work/DeepLearning/00_deep_moma/02_model_training/00_phase_contrast_unet_segmentation/model/models/20190805-154947/tensorflow_model_csbdeep.zip";
//        modelFile = "/home/micha/Documents/01_work/DeepLearning/00_deep_moma/02_model_training/00_phase_contrast_unet_segmentation/model/tensorflow_model_csbdeep.zip";
//        modelFile = "/home/micha/Documents/01_work/DeepLearning/00_deep_moma/02_model_training/00_phase_contrast_unet_segmentation/model/models/20190807-113655_d57e9849/tensorflow_model_csbdeep_512x64.zip";
//        modelFile = "/home/micha/Documents/01_work/DeepLearning/00_deep_moma/02_model_training/00_phase_contrast_unet_segmentation/model/models/20190807-120902_52411e55/tensorflow_model_csbdeep_512x64.zip";
//        modelFile = "/home/micha/Documents/01_work/DeepLearning/00_deep_moma/02_model_training/00_phase_contrast_unet_segmentation/model/models/20190814-113528_5f72bf24/tensorflow_model.zip";
//        modelFile = "/home/micha/Documents/01_work/DeepLearning/00_deep_moma/02_model_training/00_phase_contrast_unet_segmentation/model/models/20190903-221815_12e36b0f/tensorflow_model.zip";
//        modelFile = "/home/micha/Documents/01_work/DeepLearning/00_deep_moma/02_model_training/00_phase_contrast_unet_segmentation/model/models/20191022-114023_c20dd212/tensorflow_model.zip";
        modelFile = "/home/micha/Documents/01_work/DeepLearning/00_deep_moma/02_model_training/00_phase_contrast_unet_segmentation/model/models/20191023-105641_56b34e1d_0fa4003b/tensorflow_model.zip";
//        modelFile = "/home/micha/Documents/01_work/DeepLearning/00_deep_moma/02_model_training/00_phase_contrast_unet_segmentation/model/tensorflow_model_csbdeep.zip";
//        modelFile = "/home/micha/Documents/01_work/DeepLearning/00_deep_moma/02_model_training/01_fluorescence_unet_segmentation/model/tensorflow_model.zip";

        context = new Context();
        ops = context.service(OpService.class);
        commandService = context.service(CommandService.class);
        datasetService = context.service(DatasetService.class);
    }
    
    /**
     * Load and run the Tensor-Flow network on the images to create probability maps
     * of the cell-location.
     */
    public Img<FloatType> runNetwork(Img<FloatType> img) {
        try {
            img = (Img)normalizeToPercentiles(img, 0.4, 99.4);
            FinalInterval roiForNetworkProcessing = getRoiForUnetProcessing(img);
            IntervalView<FloatType> newImg = getReshapedImageForProcessing(img, roiForNetworkProcessing);
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
            final Img<FloatType> outputImg = reshapeProcessedImageToOriginalSize(img, roiForNetworkProcessing, processedImage);
            ImageJFunctions.show(outputImg, "Processed Image");
            return outputImg;

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Img<FloatType> reshapeProcessedImageToOriginalSize(Img<FloatType> img, FinalInterval roiForNetworkProcessing, Img<FloatType> tmp) {
        final Img<FloatType> outputImg = img.factory().create(img);
        IntervalView<FloatType> roiImgInterval = Views.zeroMin(Views.interval(outputImg, roiForNetworkProcessing));
        LoopBuilder.setImages( tmp, roiImgInterval ).forEachPixel( (in, out ) -> out.set( in ) );
        return outputImg;
    }

    @NotNull
    private IntervalView<FloatType> getReshapedImageForProcessing(Img<FloatType> img, FinalInterval roiForNetworkProcessing) {
        return Views.interval(img, roiForNetworkProcessing);
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

    private Iterable<FloatType> normalizeToPercentiles(Img<FloatType> image, double lower_percentile, double upper_percentile) {
        int dim = 2;
        long limit = image.dimension(dim);
        for(int i=0; i<limit; i++){

            RandomAccessibleInterval<FloatType> view = Views.hyperSlice( image, dim, i );

            float min_percentile = ops.stats().percentile((Iterable<FloatType>) view, lower_percentile).getRealFloat();
            float max_percentile = ops.stats().percentile((Iterable<FloatType>) view, upper_percentile).getRealFloat();
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
