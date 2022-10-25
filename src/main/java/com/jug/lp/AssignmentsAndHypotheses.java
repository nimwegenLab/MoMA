package com.jug.lp;

import com.jug.exceptions.IlpSetupException;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.ComponentInterface;
import net.imglib2.type.numeric.real.FloatType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

/**
 * @author jug
 */
public class AssignmentsAndHypotheses<A extends AbstractAssignment<H>, H extends Hypothesis<AdvancedComponent<FloatType>>> {

    // -------------------------------------------------------------------------------------
    // fields
    // -------------------------------------------------------------------------------------
    /**
     * All assignments. Outer list corresponds with time-points t, inner one is
     * just a container for the assignments (could very well be a set instead of
     * a list).
     */
    private final List<List<A>> a_t;

    /**
     * All segmentation hypotheses. Outer list corresponds with time-points t,
     * inner one is just a container for the assignments (could very well be a
     * set instead of a list).
     */
    private final List<List<H>> h_t;

    /**
     * A Map from any <code>Object</code> to a segmentation hypothesis.
     * Hypotheses are used to encapsulate any kind of entity (segmentation
     * hypothesis) out there in the world. This might for example be a
     * <code>Component</code>.
     */
    private final Map<Object, H> hmap;

    // -------------------------------------------------------------------------------------
    // construction
    // -------------------------------------------------------------------------------------
    public AssignmentsAndHypotheses() {
        a_t = new ArrayList<>();
        h_t = new ArrayList<>();
        hmap = new HashMap<>();
    }

    // -------------------------------------------------------------------------------------
    // methods
    // -------------------------------------------------------------------------------------

    /**
     * Adds (appends) a new time-step.
     * This shrinks down to appending an inner <code>List</code> to
     * <code>a_t</code> and <code>h_t</code>.
     */
    private void addTimeStep() {
        a_t.add(new ArrayList<>());
        h_t.add(new ArrayList<>());
    }

    /**
     * Adds an assignment to <code>a_t</code>. If <code>a_t</code> does not
     * contain <code>t</code> time-steps this function will add the missing
     * amount.
     *
     * @param t a number denoting the time at which the given assignment
     *          should be added.
     * @param a the assignment to be added.
     */
    public void addAssignment(final int t, final A a) {
        /* TODO-MM-20220815: initialize a_t to the number of time-steps before-hand and throw here, if we try to access an out-of-bounds time-step */
//        if (t >= a_t.size()) {
//            throw new RuntimeException("Time step not available.");
//    }
        while (t >= a_t.size()) {
            addTimeStep();
        }
        a_t.get(t).add(a);
    }

    /**
     * Returns all time-points in a <code>List</code>, containing all stored
     * assignments in a <code>List</code>.
     *
     * @return <code>a_t</code>
     */
    public List<List<A>> getAllAssignments() {
        return a_t;
    }

    /**
     * Return the total number of all assignments.
     *
     * @return
     */
    public int getTotalNumberOfAssignments() {
        int numberOfAssignments = 0;
        for (List<A> assignmentsAtT : getAllAssignments()) {
            numberOfAssignments += assignmentsAtT.size();
        }
        return numberOfAssignments;
    }

    /**
     * Returns a <code>List</code> containing all assignments stored at
     * time-point t.
     * By definition those are all the assignments between t and t+1.
     *
     * @param t a number denoting the time at which the given assignment
     *          should be returned.
     * @return <code>a_t.get(t);</code>
     */
    public List<A> getAssignmentsAt(final int t) {
        if (t >= 0 && t < a_t.size()) {
            return a_t.get(t);
        } else {
            return new ArrayList<>(); // if hypotheses for time t do not exist return empty array
        }
    }

    /**
     * Adds a hypothesis to <code>h_t</code>. If <code>h_t</code> does not
     * contain <code>t</code> time-steps this function will add the missing
     * amount.
     *
     * @param t a number denoting the time at which the given assignment
     *          should be added.
     * @param h the segmentation hypothesis to be added.
     */
    public void addHypothesis(final int t, final H h) {
        while (t >= h_t.size()) {
            addTimeStep();
        }
        if (h_t.get(t).add(h)) { // TODO-MM-20191021: WARNING: this seems dangerous as it can cause silent mis-behavior by having hypotheses that are not in hmap, if h_t.get(t) returns false.
            hmap.put(h.getWrappedComponent(), h);
        } else {
            System.out.print(String.format("ERROR: Failed to add hypothesis at time-step: t=%d", t));
        }
    }

    /**
     * This method checks, if the hypothesis {@param h} with the contained component was previously already added.
     * If so, it will return that hypothesis and do nothing else. If the hypothesis does not yet exist, it will add the
     * provided hypothesis to {@link hmap} and return it.
     *
     * @param t time-step that the hypothesis belongs to.
     * @param h hypothesis that should be added or returned.
     * @return
     */
    public H getOrAddHypothesis(final int t, final H h) {
        H returnedHyp = hmap.get(h.getWrappedComponent());
        if (returnedHyp != null) { /* hypothesis with this component already exists; return it */
            return hmap.get(h.getWrappedComponent());
        }
        while (t >= h_t.size()) {
            addTimeStep();
        }
        if (h_t.get(t).add(h)) { // TODO-MM-20191021: WARNING: this seems dangerous as it can cause silent mis-behavior by having hypotheses that are not in hmap, if h_t.get(t) returns false.
            hmap.put(h.getWrappedComponent(), h); /* hypothesis does not exist; add and return it */
            return h;
        } else {
            throw new RuntimeException(String.format("ERROR: Failed to add hypothesis at time-step: t=%d", t));
//            System.out.print(String.format("ERROR: Failed to add hypothesis at time-step: t=%d", t));
//            return h;
        }
    }

    /**
     * Returns all time-points in a <code>List</code>, containing all stored
     * segmentation hypothesis in an inner <code>List</code>.
     *
     * @return <code>h_t</code>
     */
    public List<List<H>> getAllHypotheses() {
        return h_t;
    }

    /**
     * Returns a <code>List</code> containing all hypothesis stored at
     * time-point t.
     * If index {@param t} is out of bounds, we return an empty ArrayList.
     * We do this, because with the U-Net segment generation empty channels
     * there will not be any segments for empty channels - even thought the
     * {@param t} may still be valid given the range of movie frames.
     *
     * @param t a number denoting the time at which the given assignment
     *          should be returned.
     * @return <code>h_t.get(t);</code>
     */
    public List<H> getHypothesesAt(final int t) {
        if (t >= 0 && t < h_t.size()) {
            return h_t.get(t);
        } else {
            return new ArrayList<>(); // if hypotheses for time t do not exist return empty array
        }
    }

    public List<H> getLeafHypothesesAt(final int t) {
        List<H> hyps = getHypothesesAt(t);
        List<H> leaves = new ArrayList<>();
        for (H hyp : hyps) {
            if (hyp.getChildHypotheses().isEmpty()) {
                leaves.add(hyp);
            }
        }
        return leaves;
    }

    /**
     * Finds an <code>Hypothesis</code> that wraps the given <code>Object</code>
     * .
     *
     * @param something any <code>Object</code> you expect to be wrapped inside a
     *                  hypothesis.
     * @return the <code>Hypothesis</code> wrapping the given
     * <code>Object</code>, or <code>null</code> in case this object was
     * not wrapped by any of the stored hypotheses.
     */
    public H findHypothesisContaining(final Object something) {
        final H h = hmap.get(something);
        if (isNull(h)) {
            throw new IlpSetupException("No hypothesis found for component: " + ((ComponentInterface) something).getStringId());
        }
        return h;
    }

    public boolean containsKey(final Object something) {
        return hmap.containsKey(something);
    }

    /**
     * @return the number of entries in the outer lists of <code>h_t</code> and
     * <code>a_t</code>.
     */
    public int getNumberOfTimeSteps() {
        return h_t.size();
    }
}
