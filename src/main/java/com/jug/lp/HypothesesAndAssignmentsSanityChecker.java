package com.jug.lp;

import com.jug.Growthlane;
import com.jug.GrowthlaneFrame;
import com.jug.util.ComponentTreeUtils;
import com.jug.util.componenttree.AdvancedComponent;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.type.numeric.real.FloatType;
import org.javatuples.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class HypothesesAndAssignmentsSanityChecker {
    private final Growthlane gl;
    private final AssignmentsAndHypotheses<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>, Hypothesis<AdvancedComponent<FloatType>>> nodes;
    private final HypothesisNeighborhoods<Hypothesis<AdvancedComponent<FloatType>>, AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> edgeSets;

    public HypothesesAndAssignmentsSanityChecker(Growthlane gl,
                                                 AssignmentsAndHypotheses<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>, Hypothesis<AdvancedComponent<FloatType>>> nodes,
                                                 HypothesisNeighborhoods<Hypothesis<AdvancedComponent<FloatType>>, AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> edgeSets) {
        this.gl = gl;
        this.nodes = nodes;
        this.edgeSets = edgeSets;
    }

    public void checkIfAllComponentsHaveCorrespondingHypothesis() {
        for (int t = 1; t < gl.size(); t++) {
            allHypothesisForComponentsExistAtTime(t);
        }
    }

    private void allHypothesisForComponentsExistAtTime(int t) {
        Consumer<Pair<List<AdvancedComponent<FloatType>>, Integer>> levelComponentsConsumer = (levelComponentsListAndLevel) -> {
            List<AdvancedComponent<FloatType>> componentsOfLevel = levelComponentsListAndLevel.getValue0();
            {
                for (AdvancedComponent<FloatType> component : componentsOfLevel) {
                    Hypothesis<?> wrappingHypothesis = nodes.findHypothesisContaining(component);
                    assert (wrappingHypothesis != null) : "ERROR: Found component without corresponding hypothesis!";
                }
            }
        };
        final GrowthlaneFrame glf = gl.getFrames().get(t);
        ComponentTreeUtils.doForEachComponentInTreeLevel(glf.getComponentTree(), levelComponentsConsumer);
    }

    public void checkIfAllComponentsHaveMappingAssignmentsBetweenThem() {
        for (int t = 1; t < gl.size(); t++) {
            allMappingAssignmentsForComponentsWithExistingHypothesesExistAtTime(t);
        }
    }

    private void allMappingAssignmentsForComponentsWithExistingHypothesesExistAtTime(int t) {
        if (t + 1 >= gl.getFrames().size())
            return;
        ComponentForest<AdvancedComponent<FloatType>> sourceComponentTree = gl.getFrames().get(t).getComponentTree();
        ComponentForest<AdvancedComponent<FloatType>> targetComponentTree = gl.getFrames().get(t + 1).getComponentTree();
        List<AdvancedComponent<FloatType>> allSourceComponents = ComponentTreeUtils.getListOfNodes(sourceComponentTree);
        List<AdvancedComponent<FloatType>> allTargetComponents = ComponentTreeUtils.getListOfNodes(targetComponentTree);

        for (AdvancedComponent<FloatType> sourceComponent : allSourceComponents) {
            Hypothesis<?> wrappingHypothesis = nodes.findHypothesisContaining(sourceComponent);
            assert (wrappingHypothesis != null) : "ERROR: Found component without corresponding hypothesis!";
            if (wrappingHypothesis != null) {
                Set<MappingAssignment> assignments = edgeSets.getRightAssignmentsOfType((Hypothesis<AdvancedComponent<FloatType>>) wrappingHypothesis, MappingAssignment.class);
                Set<AdvancedComponent<FloatType>> assignmentTargetComponents = new HashSet<>();
                for (MappingAssignment assignment : assignments) {
                    assignmentTargetComponents.add(assignment.getDestinationHypothesis().getWrappedComponent());
                }
                for (AdvancedComponent<FloatType> component : allTargetComponents) {
                    assert (assignmentTargetComponents.contains(component)) : String.format("ERROR at t=%d: Found component, which misses an incoming mapping-assignment.", t);
                }
            }
        }
    }
}
