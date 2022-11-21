package com.jug.lp;

import com.jug.config.IConfiguration;

public class AssignmentFilterFactory {
    private final IConfiguration configuration;
    private final ImageProperties imageProperties;

    public AssignmentFilterFactory(IConfiguration configuration, ImageProperties imageProperties) {
        this.configuration = configuration;
        this.imageProperties = imageProperties;
    }

    public IAssignmentFilter getAssignmentFilter() {
        if (!configuration.getFilterAssignmentsUsingFluorescenceFeatureFlag()) {
            return new DummyAssignmentFilter();
        }
        return new AssignmentFilterUsingFluorescencePerFrame(imageProperties,
                configuration.getFluorescentAssignmentFilterChannel(),
                configuration.getFluorescentAssignmentFilterNumberOfSigmas());
    }
}
