package com.jug.lp;

import com.jug.config.IConfiguration;
import com.jug.config.IFluorescenceAssignmentFilterConfiguration;

public class AssignmentFilterFactory {
    private final IFluorescenceAssignmentFilterConfiguration configuration;
    private final ImageProperties imageProperties;

    public AssignmentFilterFactory(IFluorescenceAssignmentFilterConfiguration configuration, ImageProperties imageProperties) {
        this.configuration = configuration;
        this.imageProperties = imageProperties;
    }

    public IAssignmentFilter getAssignmentFilter() {
        if (!configuration.getFilterAssignmentsUsingFluorescenceFeatureFlag()) {
            return new DummyAssignmentFilter();
        }
        return new AssignmentFilterUsingFluorescencePerFrame(imageProperties, configuration);
    }
}
