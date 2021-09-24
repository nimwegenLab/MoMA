package com.jug.util;

import com.jug.MoMA;
import com.jug.config.ConfigurationManager;
import com.jug.datahandling.IImageProvider;
import com.jug.export.MixtureModelFit;
import com.jug.lp.AssignmentPlausibilityTester;
import com.jug.util.componenttree.ComponentProperties;

/**
 * This is pseudo dependency injection container, which I use to work on getting my class dependencies and initialization
 * in order. Ideally at some point, this will be replaced with a true dependency injection frame-work.
 */
public class PseudoDic {
    private final AssignmentPlausibilityTester assignmentPlausibilityTester;
    private final ComponentProperties componentProperties;
    private ConfigurationManager configurationManager;
    private MoMA momaInstance;
    private MixtureModelFit mixtureModelFit;

    public PseudoDic(ConfigurationManager configurationManager, MoMA main) {
        this.configurationManager = configurationManager;
        this.momaInstance = main;
        assignmentPlausibilityTester = new AssignmentPlausibilityTester(ConfigurationManager.MAXIMUM_RELATIVE_SIZE_CHANGE_BETWEEN_FRAMES);
        componentProperties = new ComponentProperties();
        mixtureModelFit = new MixtureModelFit(getConfigurationManager());
    }

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
}
