package com.jug.mocks;

import com.jug.lp.AbstractAssignment;
import com.jug.lp.Hypothesis;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.ComponentInterface;
import net.imglib2.type.numeric.real.FloatType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockUtils {
    public static AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> getAssignmentMock(int numberOfTargetHyptheses) {
        AbstractAssignment assignmentMock = mock(AbstractAssignment.class);
        List<Hypothesis> hypothesisList = new ArrayList<>();
        List<ComponentInterface> componentList = new ArrayList<>();
        for (int i = 0; i < numberOfTargetHyptheses; i++) {
            Hypothesis hyp = getHypothesisMock();
            hypothesisList.add(hyp);
            componentList.add(hyp.getWrappedComponent());
        }
        when(assignmentMock.getTargetHypotheses()).thenReturn(hypothesisList);
        when(assignmentMock.getTargetComponent(anyInt())).thenAnswer(invocationOnMock -> hypothesisList.get(invocationOnMock.getArgument(0)).getWrappedComponent());
        return assignmentMock;
    }

    @NotNull
    public static Hypothesis getHypothesisMock() {
        Hypothesis hypothesis = mock(Hypothesis.class);
        AdvancedComponent componentMock = mock(AdvancedComponent.class);
        when(hypothesis.getWrappedComponent()).thenReturn(componentMock);
        return hypothesis;
    }
}
