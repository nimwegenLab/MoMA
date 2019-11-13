package com.jug.lp;

import com.jug.GrowthLine;
import com.jug.GrowthLineFrame;
import com.jug.util.ComponentTreeUtils;
import com.jug.util.componenttree.SimpleComponent;
import com.moma.auxiliary.Plotting;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.numeric.real.FloatType;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HypothesesAndAssignmentsSanityChecker {
    private GrowthLine gl;
    private AssignmentsAndHypotheses<AbstractAssignment<Hypothesis<Component<FloatType, ?>>>, Hypothesis<Component<FloatType, ?>>> nodes;

    public HypothesesAndAssignmentsSanityChecker(GrowthLine gl, AssignmentsAndHypotheses< AbstractAssignment< Hypothesis<Component< FloatType, ? >> >, Hypothesis< Component< FloatType, ? > > > nodes){
        this.gl = gl;
        this.nodes = nodes;
    }

    public void checkIfAllComponentsHaveCorrespondingHypothesis() {
        for (int t = 1; t < gl.size(); t++) {
            allHypothesisForComponentsExistAtTime(t);

            if(t == 32){
                final GrowthLineFrame glf = gl.getFrames().get(t);
                Plotting.drawComponentTree2(glf.getComponentTree(), new ArrayList<>());
            }
        }
    }

    private void allHypothesisForComponentsExistAtTime(int t) {
        Consumer<Pair<List<SimpleComponent<FloatType>>, Integer>> levelComponentsConsumer = (levelComponentsListAndLevel) -> {
            List<SimpleComponent<FloatType>> componentsOfLevel = levelComponentsListAndLevel.getValue0();
            {
                for (SimpleComponent<FloatType> component : componentsOfLevel) {
                    Hypothesis<?> res = nodes.findHypothesisContaining(component);
                    if (res == null) {
                        try {
                            throw new Exception("Found component without corresponding hypothesis!");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        final GrowthLineFrame glf = gl.getFrames().get(t);
        ComponentTreeUtils.doForEachComponentInTreeLevel(glf.getComponentTree(), levelComponentsConsumer);
    }


}
