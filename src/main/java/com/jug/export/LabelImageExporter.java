package com.jug.export;

import com.moma.auxiliary.Plotting;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.converter.Converters;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;
import net.imagej.ops.morphology.thin.ThinGuoHall;
import net.imagej.ops.OpService;
import org.scijava.Context;

import java.io.File;
import java.util.ArrayList;

/***
 * This class is used to export an image stack with component masks.
 * The cellId is used to set the pixel value of the corresponding mask of that cell.
 */

public class LabelImageExporter {
    private final long horizontalSize;
    private final long verticalSize;
    private final ArrayList<RandomAccessibleInterval<IntType>> componentLevelImageStack = new ArrayList<>();
    private final ArrayImgFactory<IntType> imageFactory = new ArrayImgFactory<>(new IntType());
    private final OpService ops;
    private final Context context;

    public LabelImageExporter(long horizontalSize, long verticalSize) {
        this.horizontalSize = horizontalSize;
        this.verticalSize = verticalSize;
        context = new Context();
        ops = context.service(OpService.class);
    }

    /***
     * Draw component mask with cellId as pixel value to image frame at frameIndex.
     *
     * @param component
     * @param cellId
     * @param frameIndex
     */
    public void addComponentMaskToImage(final Component<?, ?> component, int cellId, int frameIndex) {
        RandomAccessibleInterval<IntType> image = getFrame(frameIndex);
        IntType pixelValue = new IntType(cellId);
        Plotting.drawComponentMask(component, pixelValue, image);
    }

    /***
     * Returns image with frameId. If the image does not yet exist, it will be created and added to
     * componentLevelImageStack before returning it.
     *
     * @param frameIndex the index of the desired image frame
     * @return
     */
    private RandomAccessibleInterval getFrame(int frameIndex) {
        try {
            return componentLevelImageStack.get(frameIndex);
        } catch (IndexOutOfBoundsException err) {
            RandomAccessibleInterval<IntType> componentLevelImage = imageFactory.create(horizontalSize, verticalSize);
            componentLevelImageStack.add(frameIndex, componentLevelImage);
            return componentLevelImage;
        }
    }

    /***
     * Save label image stack to file.
     *
     * @param file
     */
    public void saveLabelImage(File file) {
        RandomAccessibleInterval<IntType> imageStack = Views.stack(componentLevelImageStack);;
        final ImagePlus imagePlus = ImageJFunctions.wrap(imageStack, "export");
        IJ.save(imagePlus, file.getPath());
    }

    public void saveThinnedImages(File file){
        RandomAccessibleInterval<IntType> res1 = componentLevelImageStack.get(0);
//        IterableInterval<IntType> re2 = Views.iterable(res1);
        int threshold = 0;

//        IntType tmp = new IntType();
//        tmp.getInteger()
        RandomAccessibleInterval< BitType > mask = Converters.convert( res1, (i, o ) -> o.set(i.getInteger() > threshold), new BitType() );

        Cursor<BitType> pixel = Views.iterable(mask).cursor();
        RandomAccessibleInterval frame0thinned = ops.morphology().thinGuoHall(mask);
        final ImagePlus frame0thinnedPlus = ImageJFunctions.wrap(frame0thinned, "export");
        IJ.save(frame0thinnedPlus, file.getPath());


    }
}
