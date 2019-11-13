package com.jug.lp;

import com.jug.GrowthLine;
import com.jug.GrowthLineFrame;
import com.jug.util.ComponentTreeUtils;
import com.jug.util.componenttree.SimpleComponent;
import com.moma.auxiliary.Plotting;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.type.numeric.real.FloatType;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class HypothesesAndAssignmentsSanityChecker {
    private GrowthLine gl;
    private AssignmentsAndHypotheses<AbstractAssignment<Hypothesis<Component<FloatType, ?>>>, Hypothesis<Component<FloatType, ?>>> nodes;
    private HypothesisNeighborhoods<Hypothesis<Component<FloatType, ?>>, AbstractAssignment<Hypothesis<Component<FloatType, ?>>>> edgeSets;

    public HypothesesAndAssignmentsSanityChecker(GrowthLine gl,
                                                 AssignmentsAndHypotheses< AbstractAssignment< Hypothesis<Component< FloatType, ? >> >, Hypothesis< Component< FloatType, ? > > > nodes,
                                                 HypothesisNeighborhoods< Hypothesis< Component< FloatType, ? > >, AbstractAssignment< Hypothesis< Component< FloatType, ? > > > > edgeSets){
        this.gl = gl;
        this.nodes = nodes;
        this.edgeSets = edgeSets;
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
                    Hypothesis<?> wrappingHypothesis = nodes.findHypothesisContaining(component);
                    assert (wrappingHypothesis != null): "ERROR: Found component without corresponding hypothesis!";
                }
            }
        };
        final GrowthLineFrame glf = gl.getFrames().get(t);
        ComponentTreeUtils.doForEachComponentInTreeLevel(glf.getComponentTree(), levelComponentsConsumer);
    }

    public void checkIfAllComponentsMappingAssignmentsBetweenThem() {
        for (int t = 1; t < gl.size(); t++) {
            allMappingAssignmentsForComponentsExistAtTime(t);
        }
    }

    private void allMappingAssignmentsForComponentsExistAtTime(int t){
        ComponentForest<SimpleComponent<FloatType>> sourceComponentTree = gl.getFrames().get(t).getComponentTree();
        ComponentForest<SimpleComponent<FloatType>> targetComponentTree = gl.getFrames().get(t).getComponentTree();
        List<SimpleComponent<FloatType>> allSourceComponents = ComponentTreeUtils.getListOfNodes(sourceComponentTree);
        List<SimpleComponent<FloatType>> allTargetComponents = ComponentTreeUtils.getListOfNodes(targetComponentTree);

        for(SimpleComponent<FloatType> targetComponent : allSourceComponents){
            Hypothesis<?> wrappingHypothesis = nodes.findHypothesisContaining(targetComponent);
            assert (wrappingHypothesis != null): "ERROR: Found component without corresponding hypothesis!";
            Set<MappingAssignment> assignments = edgeSets.getRightAssignmentsOfType((Hypothesis<Component<FloatType, ?>>) wrappingHypothesis, MappingAssignment.class);
            Set<Component<FloatType, ?>> assignmentTargetComponents = new HashSet<>();
            for(MappingAssignment assignment : assignments){
                assignmentTargetComponents.add(assignment.getDestinationHypothesis().getWrappedComponent());
            }
            for(SimpleComponent<FloatType> component : allTargetComponents){
                assert(assignmentTargetComponents.contains(component)): "ERROR: Found component, which misses an incoming mapping-assignment.";
            }
        }
    }
}
