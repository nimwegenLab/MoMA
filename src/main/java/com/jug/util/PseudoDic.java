package com.jug.util;

import com.jug.MoMA;
import com.jug.config.ConfigurationManager;
import com.jug.datahandling.IImageProvider;
import com.jug.export.MixtureModelFit;
import com.jug.lp.AssignmentPlausibilityTester;
import com.jug.util.componenttree.ComponentProperties;
import com.jug.util.componenttree.ComponentTreeGenerator;
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

    public PseudoDic(ConfigurationManager configurationManager, MoMA main) {
        context = new Context();
        ops = context.service(OpService.class);
        componentTreeGenerator = new ComponentTreeGenerator(ops);
        this.configurationManager = configurationManager;
        this.momaInstance = main;
        assignmentPlausibilityTester = new AssignmentPlausibilityTester(ConfigurationManager.MAXIMUM_RELATIVE_SIZE_CHANGE_BETWEEN_FRAMES);
        Imglib2Utils imglib2utils = new Imglib2Utils(ops);
        componentProperties = new ComponentProperties(ops, imglib2utils);
        mixtureModelFit = new MixtureModelFit(getConfigurationManager());
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
}
