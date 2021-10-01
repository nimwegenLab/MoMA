package com.jug;

import com.jug.datahandling.IImageProvider;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.ComponentTreeGenerator;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.type.numeric.real.FloatType;

/**
 * @author jug
 * Represents one growth line (well) in which Bacteria can grow, at one
 * instance in time.
 * This corresponds to one growth line micrograph. The class
 * representing an entire time
 * series (2d+t) representation of an growth line is
 * <code>Growthlane</code>.
 */
public class GrowthlaneFrame extends AbstractGrowthlaneFrame<AdvancedComponent<FloatType>> {

    private int frameIndex;
    private ComponentTreeGenerator componentTreeGenerator;

    public int getFrameIndex() {
        return frameIndex;
    }

    public GrowthlaneFrame(int frameIndex, ComponentTreeGenerator componentTreeGenerator) {
        this.frameIndex = frameIndex;
        this.componentTreeGenerator = componentTreeGenerator;
    }

    /**
     * @see AbstractGrowthlaneFrame#buildIntensityTree(net.imglib2.RandomAccessibleInterval)
     */
    @Override
    protected ComponentForest<AdvancedComponent<FloatType>> buildIntensityTree(final IImageProvider imageProvider, int frameIndex) {
        return componentTreeGenerator.buildIntensityTree(imageProvider, frameIndex);
    }
}
