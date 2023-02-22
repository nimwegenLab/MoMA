package com.jug.lp;

public class AssignmentFilterFacade implements IAssignmentFilter {
    private Iterable<IAssignmentFilter> filters;

    public AssignmentFilterFacade(Iterable<IAssignmentFilter> filters) {
        this.filters = filters;
    }

    @Override
    public void evaluate(AbstractAssignment assignment) {
        filters.forEach(filter -> filter.evaluate(assignment));
    }
}
