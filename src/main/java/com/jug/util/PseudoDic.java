package com.jug.util;

import com.jug.config.ConfigurationManager;
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
    private MixtureModelFit mixtureModelFit;

    public PseudoDic(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
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
}
