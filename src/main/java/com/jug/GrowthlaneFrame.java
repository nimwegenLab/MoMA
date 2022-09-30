package com.jug;

import com.jug.config.ConfigurationManager;
import com.jug.datahandling.IImageProvider;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.AdvancedComponentForest;
import com.jug.util.componenttree.IComponentForestGenerator;
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
    private IComponentForestGenerator componentForestGenerator;
    private ConfigurationManager configurationManager;
    private IImageProvider imageProvider;

    public int getFrameIndex() {
        return frameIndex;
    }

    public GrowthlaneFrame(int frameIndex, IComponentForestGenerator componentForestGenerator, ConfigurationManager configurationManager, IImageProvider imageProvider) {
        this.frameIndex = frameIndex;
        this.componentForestGenerator = componentForestGenerator;
        this.configurationManager = configurationManager;
        this.imageProvider = imageProvider;
    }

    @Override
    protected AdvancedComponentForest<FloatType, AdvancedComponent<FloatType>> buildComponentForest() {
        return componentForestGenerator.buildComponentForest(imageProvider.getImgProbsAt(frameIndex), frameIndex, configurationManager.THRESHOLD_FOR_COMPONENT_SPLITTING);
    }
}
