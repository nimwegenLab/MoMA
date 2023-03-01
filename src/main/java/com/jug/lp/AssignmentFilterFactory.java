package com.jug.lp;

import com.jug.config.IConfiguration;
import com.jug.config.IFluorescenceAssignmentFilterConfiguration;

import java.util.ArrayList;
import java.util.List;

public class AssignmentFilterFactory {
    private final IFluorescenceAssignmentFilterConfiguration configuration;
    private final ImageProperties imageProperties;

    public AssignmentFilterFactory(IFluorescenceAssignmentFilterConfiguration configuration, ImageProperties imageProperties) {
        this.configuration = configuration;
        this.imageProperties = imageProperties;
    }

    public IAssignmentFilter getAssignmentFilter() {
        List<IAssignmentFilter> filters = new ArrayList<>();
        if (configuration.getFilterAssignmentsUsingFluorescenceFeatureFlag()) {
            filters.add(new AssignmentFilterUsingFluorescencePerFrame(configuration));
        }

        if (filters.isEmpty()) {
            return new DummyAssignmentFilter();
        } else {
            return new AssignmentFilterFacade(filters);
        }
    }
}
