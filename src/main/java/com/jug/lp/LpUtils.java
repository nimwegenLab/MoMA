package com.jug.lp;

import com.jug.util.ComponentTreeUtils;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.type.numeric.real.FloatType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jug
 */
class LpUtils {

    /**
     * Builds and returns the set Hup (given as a List). Hup is defines as the
     * setup all Hypothesis in hyps that strictly above the Hypothesis hyp (in
     * image space).
     * Hup: corresponds to Aup in Jug-paper, eq. 8.
     *
     * @param hyp  the reference hypothesis.
     * @param hyps the set of all hypothesis of interest.
     * @return a List of all Hypothesis from hyps that lie above the
     * segmentation hypothesis hyp.
     */
    public static List<Hypothesis<AdvancedComponent<FloatType>>> getHup(
            final Hypothesis<AdvancedComponent<FloatType>> hyp,
            final List<Hypothesis<AdvancedComponent<FloatType>>> hyps
    ) {
        final List<Hypothesis<AdvancedComponent<FloatType>>> Hup = new ArrayList<>();
        for (final Hypothesis<AdvancedComponent<FloatType>> candidate : hyps) {
            if (ComponentTreeUtils.isAbove(candidate, hyp)) {
                Hup.add(candidate);
            }
        }
        return Hup;
    }
}
