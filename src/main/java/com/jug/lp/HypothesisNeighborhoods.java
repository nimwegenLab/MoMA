package com.jug.lp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 * The main purpose of this class is to manage and update the assignment
 * neighborhoods $A_{>>b_i^t}$ and $A_{b_i^t>>}$.
 *
 * @author jug
 */
class HypothesisNeighborhoods< H extends Hypothesis< ? >, A extends AbstractAssignment< H > > {

	// -------------------------------------------------------------------------------------
	// fields
	// -------------------------------------------------------------------------------------
	/**
	 * This structure corresponds to $A_{b_i->}$ for some time-point.
	 */
	private final HashMap< H, Set< A > > rightNeighborhoods;

	/**
	 * This structure corresponds to $A_{->b_i}$ for some time-point.
	 */
	private final HashMap< H, Set< A > > leftNeighborhoods;

	// -------------------------------------------------------------------------------------
	// construction
	// -------------------------------------------------------------------------------------
	public HypothesisNeighborhoods() {
		rightNeighborhoods = new HashMap<>();
		leftNeighborhoods = new HashMap<>();
	}

	// -------------------------------------------------------------------------------------
	// getters and setters
	// -------------------------------------------------------------------------------------
	/**
	 * Gets the leftNeighborhood of a hypothesis <code>h</code>.
	 *
	 * @param h
	 *            a hypothesis of type <code>H</code>.
	 * @return a set of assignments of type <code>A</code>, or <code>null</code>
	 *         if such a neighborhood does not exist here.
	 */
	public Set< A > getLeftNeighborhood( final H h ) {
		return leftNeighborhoods.get( h );
	}

	/**
	 * Gets the rightNeighborhood of a hypothesis <code>h</code>.
	 *
	 * @param h
	 *            a hypothesis of type <code>H</code>.
	 * @return a set of assignments of type <code>A</code>, or <code>null</code>
	 *         if such a neighborhood does not exist here.
	 */
	public Set< A > getRightNeighborhood( final H h ) {
		return rightNeighborhoods.get( h );
	}

	/**
	 * Gets right-assignments of {@param h}, which are of type {@param assignmentType}.
	 *
	 * @param h              hypothesis for which to get the assignments/
	 * @param assignmentType type of assignments that will be returned.
	 * @param <T>
	 * @return
	 */
	public <T extends AbstractAssignment> Set<T> getRightAssignmentsOfType(final H h, Class<T> assignmentType) {
		return getAssignmentsOfType(getRightNeighborhood(h), assignmentType);
	}

	/**
	 * Get all assignments of a given type from set of abstract assignments.
	 *
	 * @param allAssignments set of assignments from which we want to get the assignments of desired type.
	 * @param assignmentType type of the assignment
	 * @param <T>            the type of assignments we want to get
	 * @return set of assignments of the desired type
	 */
	public <T extends AbstractAssignment> Set<T> getAssignmentsOfType(final Iterable<A> allAssignments, Class<T> assignmentType) {
		Set<T> assignmentsOfType = new HashSet<>();
		for (A assignment : allAssignments) {
			if (assignmentType.isAssignableFrom(assignment.getClass())) assignmentsOfType.add((T) assignment);
		}
		return assignmentsOfType;
	}

	// -------------------------------------------------------------------------------------
	// methods
	// -------------------------------------------------------------------------------------
	/**
	 * Returns whether or not a given segmentation hypothesis has a
	 * left-neighborhood stored here.
	 *
	 * @param h
	 *            a hypothesis of type <code>H</code>.
	 * @return true, if such a neighborhood exists (might be empty though), or
	 *         false if it does not.
	 */
    private boolean hasLeftNeighborhoods(final H h) {
		return getLeftNeighborhood( h ) != null;
	}

	/**
	 * Returns whether or not a given segmentation hypothesis has a
	 * right-neighborhood stored here.
	 *
	 * @param h
	 *            a hypothesis of type <code>H</code>.
	 * @return true, if such a neighborhood exists (might be empty though), or
	 *         false if it does not.
	 */
    private boolean hasRightNeighborhoods(final H h) {
		return getRightNeighborhood( h ) != null;
	}

	/**
	 * Returns whether or not a given segmentation hypothesis has either a right
	 * or a left-neighborhood.
	 *
	 * @param h
	 *            a hypothesis of type <code>H</code>.
	 * @return true, if such a neighborhood exists (might be empty though), or
	 *         false if it does not.
	 */
	public boolean hasNeighborhoods( final H h ) {
		return hasLeftNeighborhoods( h ) && hasRightNeighborhoods( h );
	}

	/**
	 * Adds an assignment to the left-neighborhood of a segmentation hypothesis.
	 *
	 * @param h
	 *            a hypothesis of type <code>H</code>.
	 * @param a
	 *            an assignment of type <code>A</code>.
	 * @return true, if the assignment could be stored.
	 */
	public boolean addToLeftNeighborhood ( final H h, final A a ) {
		if ( ! hasLeftNeighborhoods( h ) ) {
			leftNeighborhoods.put( h, new HashSet<>() );
		}
		return getLeftNeighborhood( h ).add( a );
	}

	/**
	 * Adds an assignment to the right-neighborhood of a segmentation
	 * hypothesis.
	 *
	 * @param h
	 *            a hypothesis of type <code>H</code>.
	 * @param a
	 *            an assignment of type <code>A</code>.
	 * @return true, if the assignment could be stored.
	 */
	public boolean addToRightNeighborhood( final H h, final A a ) {
		if ( !hasRightNeighborhoods( h ) ) {
			rightNeighborhoods.put( h, new HashSet<>() );
		}
		return getRightNeighborhood( h ).add( a );
	}

}
