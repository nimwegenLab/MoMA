package com.jug;

import com.jug.util.componenttree.FilteredMserTreeGenerator;
import com.jug.util.componenttree.SimpleComponent;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.type.numeric.real.FloatType;

/**
 * @author jug
 * Represents one growth line (well) in which Bacteria can grow, at one
 * instance in time.
 * This corresponds to one growth line micrograph. The class
 * representing an entire time
 * series (2d+t) representation of an growth line is
 * <code>GrowthLine</code>.
 */
public class GrowthLineFrame extends AbstractGrowthLineFrame<SimpleComponent<FloatType>> {

    /**
     * @see com.jug.AbstractGrowthLineFrame#buildIntensityTree(net.imglib2.RandomAccessibleInterval)
     */
    @Override
    protected ComponentForest<SimpleComponent<FloatType>> buildIntensityTree(final RandomAccessibleInterval<FloatType> raiFkt) {
        return new FilteredMserTreeGenerator().buildIntensityTree(raiFkt);
    }
}
