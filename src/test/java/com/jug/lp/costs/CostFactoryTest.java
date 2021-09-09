package com.jug.lp.costs;

import com.jug.MoMA;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CostFactoryTest {
    double delta = 0.01;

    @Test
    public void getCostFactorComponentExit__returns_05_at_roi_boundary_position() {
        double verticalPosition = MoMA.GL_OFFSET_TOP;
        ComponentMock component = new ComponentMock(new double[]{0., verticalPosition});
        double val = CostFactory.getCostFactorComponentExit(component);
        assertEquals(0.5, val, delta);
    }

    @Test
    public void getCostFactorComponentExit__returns_almost_zero_far_below_roi_boundary_position() {
        double verticalPosition = MoMA.GL_OFFSET_TOP + 4 * MoMA.COMPONENT_EXIT_RANGE;
        ComponentMock component = new ComponentMock(new double[]{0., verticalPosition});
        double val = CostFactory.getCostFactorComponentExit(component);
        assertEquals(.0, val, delta);
    }

    @Test
    public void getCostFactorComponentExit__returns_almost_one_far_above_roi_boundary_position() {
        double verticalPosition = MoMA.GL_OFFSET_TOP - 4 * MoMA.COMPONENT_EXIT_RANGE;
        ComponentMock component = new ComponentMock(new double[]{0., verticalPosition});
        double val = CostFactory.getCostFactorComponentExit(component);
        assertEquals(1., val, delta);
    }

    @Test
    public void getComponentCost__returns_zero_at_roi_boundary_position(){
        double verticalPosition = MoMA.GL_OFFSET_TOP;
        ComponentMock component = new ComponentMock(new double[]{0., verticalPosition});
        float val = CostFactory.getComponentCost(component);
        assertEquals(.0, val, delta);
    }

    @Test
    public void getComponentCost__returns_almost_minus_02_far_below_roi_boundary_position() {
        double verticalPosition = MoMA.GL_OFFSET_TOP + 4 * MoMA.COMPONENT_EXIT_RANGE;
        ComponentMock component = new ComponentMock(new double[]{0., verticalPosition});
        float val = CostFactory.getComponentCost(component);
        assertEquals(-.2, val, delta);
    }

    @Test
    public void getComponentCost__returns_almost_02_far_above_roi_boundary_position() {
        double verticalPosition = MoMA.GL_OFFSET_TOP - 4 * MoMA.COMPONENT_EXIT_RANGE;
        ComponentMock component = new ComponentMock(new double[]{0., verticalPosition});
        float val = CostFactory.getComponentCost(component);
        assertEquals(.2, val, delta);
    }
}
