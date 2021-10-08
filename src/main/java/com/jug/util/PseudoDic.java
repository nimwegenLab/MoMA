package com.jug.util;

import com.jug.MoMA;
import com.jug.config.ConfigurationManager;
import com.jug.datahandling.IImageProvider;
import com.jug.export.GroundTruthFramesExporter;
import com.jug.export.MixtureModelFit;
import com.jug.lp.AssignmentPlausibilityTester;
import com.jug.util.componenttree.ComponentProperties;
import com.jug.util.componenttree.ComponentTreeGenerator;
import com.jug.util.componenttree.RecursiveComponentWatershedder;
import com.jug.util.imglib2.Imglib2Utils;
import net.imagej.ops.OpService;
import org.scijava.Context;

/**
 * This is pseudo dependency injection container, which I use to work on getting my class dependencies and initialization
 * in order. Ideally at some point, this will be replaced with a true dependency injection frame-work.
 */
public class PseudoDic {
    public static Context context;
    public static OpService ops;
    private final AssignmentPlausibilityTester assignmentPlausibilityTester;
    private final ComponentProperties componentProperties;
    private final ConfigurationManager configurationManager;
    private final MoMA momaInstance;
    private final MixtureModelFit mixtureModelFit;
    private final ComponentTreeGenerator componentTreeGenerator;
    private final Imglib2Utils imglib2utils;
    private final GroundTruthFramesExporter groundTruthFramesExporter;
    private final RecursiveComponentWatershedder recursiveComponentWatershedder;

    public PseudoDic(ConfigurationManager configurationManager, MoMA main) {
        context = new Context();
        ops = context.service(OpService.class);
        imglib2utils = new Imglib2Utils(ops);
        recursiveComponentWatershedder = new RecursiveComponentWatershedder(ops);
        componentProperties = new ComponentProperties(ops, imglib2utils);
        componentTreeGenerator = new ComponentTreeGenerator(recursiveComponentWatershedder, componentProperties);
        this.configurationManager = configurationManager;
        this.momaInstance = main;
        assignmentPlausibilityTester = new AssignmentPlausibilityTester(ConfigurationManager.MAXIMUM_GROWTH_RATE);
        mixtureModelFit = new MixtureModelFit(getConfigurationManager());
        groundTruthFramesExporter = new GroundTruthFramesExporter(() -> MoMA.getDefaultFilenameDecoration()); /* we pass a supplier here, because at this point in the instantiation MoMA.getDefaultFilenameDecoration() still Null; once instantiation is clean up, this should not be necessary anymore */
    }

    public Context getSciJavaContext() { return context; }

    public OpService getImageJOpService() { return ops; }

    public AssignmentPlausibilityTester getAssignmentPlausibilityTester() {
        return assignmentPlausibilityTester;
    }

    public ComponentProperties getComponentProperties() {
        return componentProperties;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public MixtureModelFit getMixtureModelFit() {
        return mixtureModelFit;
    }

    public IImageProvider getImageProvider() {
        return momaInstance;
    }

    public MoMA getMomaInstance() {
        return momaInstance;
    }

    public ComponentTreeGenerator getComponentTreeGenerator() { return componentTreeGenerator; }

    public Imglib2Utils getImglib2utils() { return imglib2utils; }

    public GroundTruthFramesExporter getGroundTruthFramesExporter() { return groundTruthFramesExporter; }
}
