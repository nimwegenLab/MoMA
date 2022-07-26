package com.jug.datahandling;

import com.jug.Growthlane;
import com.jug.GrowthlaneFrame;
import com.jug.config.ConfigurationManager;
import com.jug.gui.IDialogManager;
import com.jug.util.componenttree.ComponentForestGenerator;
import com.jug.util.componenttree.UnetProcessor;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/***
 * This class handles the loading of previously curated GLs. It helps in loading the files needed for restoring the
 * previous curation result.
 */
public class GlDataLoader {
    private UnetProcessor unetProcessor;
    private ConfigurationManager configurationManager;
    private IImageProvider imageProvider;
    private ComponentForestGenerator componentForestGenerator;
    private IDialogManager dialogManager;
    private GlFileManager glFileManager;

    public GlDataLoader(UnetProcessor unetProcessor,
                        ConfigurationManager configurationManager,
                        IImageProvider imageProvider,
                        ComponentForestGenerator componentForestGenerator,
                        IDialogManager dialogManager,
                        GlFileManager glFileManager) {
        this.unetProcessor = unetProcessor;
        this.configurationManager = configurationManager;
        this.imageProvider = imageProvider;
        this.componentForestGenerator = componentForestGenerator;
        this.dialogManager = dialogManager;
        this.glFileManager = glFileManager;
    }

    /**
     * Contains all Growthlanes found in the given data.
     */
    private List<Growthlane> growthlanes;

    /**
     * @return the growthlanes
     */
    public List<Growthlane> getGrowthlanes() {
        return growthlanes;
    }

    /**
     * @param growthlanes
     *            the growthlanes to set
     */
    private void setGrowthlanes(final List<Growthlane> growthlanes) {
        this.growthlanes = growthlanes;
    }

    /**
     * NOTE: This method is kept to be compatible with down-stream code.
     * Write the centers of the growth line given in 'imgTemp'. Since it is centered
     * in the image, we set the coordinates to the center of the image. Note, that
     * this method is a legacy artifact. Legacy-Moma was able to treat full-frames with
     * multiple GL inside an image by detecting them. This now no longer necessary after
     * doing the preprocessing, so that we can simplify this method, the way we did.
     */
    private void addGrowthlanes() {
        setGrowthlanes(new ArrayList<>());
        getGrowthlanes().add(getGrowthlane());
    }

    @NotNull
    private Growthlane getGrowthlane() {
        Growthlane newGl = new Growthlane(dialogManager, configurationManager, glFileManager);
        for (long frameIdx = 0; frameIdx < imageProvider.getImgRaw().dimension(2); frameIdx++) {
            GrowthlaneFrame currentFrame = new GrowthlaneFrame((int) frameIdx, componentForestGenerator, configurationManager, imageProvider);
            final IntervalView<FloatType> ivFrame = Views.hyperSlice(imageProvider.getImgRaw(), 2, frameIdx);
            currentFrame.setImage(ImgView.wrap(ivFrame, new ArrayImgFactory(new FloatType())));
            newGl.add(currentFrame);
        }
        return newGl;
    }

    /**
     * Iterates over all found Growthlanes and evokes
     * Growthlane.findGapHypotheses(Img). Note that this function always uses
     * the image data in 'imgTemp'.
     */
    private void generateSegmentationHypothesesForAllGls() {
        imageProvider.setImgProbs(getProbabilityImage());
        for ( final Growthlane gl : getGrowthlanes() ) {
            gl.generateSegmentationHypotheses();
        }
    }

    /**
     * Allows one to restart by GL segmentation. This is e.g. needed after top
     * or bottom offsets are altered, which invalidates all analysis run so far.
     */
    public void restartFromGLSegmentation() {
        System.out.print( "Searching for Growthlanes..." );
        addGrowthlanes();
        System.out.println( " done!" );

        System.out.println( "Generating Segmentation Hypotheses..." );
        generateSegmentationHypothesesForAllGls();
        System.out.println( " done!" );
    }

    /**
     * Creates and triggers filling of mmILP, containing all
     * optimization-related structures used to compute the optimal tracking.
     */
    public void generateILPs() {
        for ( final Growthlane gl : getGrowthlanes() ) {
            gl.generateILP( null );
        }
    }

    /**
     * Runs all the generated ILPs.
     */
    public void runILPs() {
        int i = 0;
        for ( final Growthlane gl : getGrowthlanes() ) {
            System.out.println( " > > > > > Starting LP for GL# " + i + " < < < < < " );
            gl.getIlp().run();
            i++;
        }
    }

    private Img<FloatType> getProbabilityImage() {
        String processedImageFileName = glFileManager.getProbabilityImageFilePath();

        /**
         *  create or load probability maps
         */
        Img<FloatType> probabilityMap;
        if (new File(processedImageFileName).exists() & configurationManager.getIsReloading()) {
            ImagePlus imp = IJ.openImage(processedImageFileName);
            probabilityMap = ImageJFunctions.convertFloat(imp);
        } else {
            probabilityMap = unetProcessor.process(imageProvider.getImgRaw());
            glFileManager.saveProbabilityImage(probabilityMap);
        }
        return probabilityMap;
    }
}
