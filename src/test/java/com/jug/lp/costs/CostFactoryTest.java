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
        assertEquals(1., val, delta);
    }

    @Test
    public void getCostFactorComponentExit__returns_almost_one_far_above_roi_boundary_position() {
        double verticalPosition = MoMA.GL_OFFSET_TOP - 4 * MoMA.COMPONENT_EXIT_RANGE;
        ComponentMock component = new ComponentMock(new double[]{0., verticalPosition});
        double val = CostFactory.getCostFactorComponentExit(component);
        assertEquals(0., val, delta);
    }

    @Test
    public void getComponentCostLegacy__returns_zero_at_roi_boundary_position_for_symmetric_min_and_max_component_cost(){
        CostFactory.maximumComponentCost = 0.2;
        CostFactory.minimumComponentCost = -0.2;
        double verticalPosition = MoMA.GL_OFFSET_TOP;
        ComponentMock component = new ComponentMock(new double[]{0., verticalPosition});
        float val = CostFactory.getComponentCostLegacy(component);
        assertEquals(.0, val, delta);
    }

    @Test
    public void getComponentCostLegacy__returns_almost_minimumComponentCost_far_below_roi_boundary_position() {
        CostFactory.maximumComponentCost = 1.2; /* test for value that is not symmetric to minimum value for more robust test */
        CostFactory.minimumComponentCost = -0.2;
        double verticalPosition = MoMA.GL_OFFSET_TOP + 4 * MoMA.COMPONENT_EXIT_RANGE;
        ComponentMock component = new ComponentMock(new double[]{0., verticalPosition});
        float val = CostFactory.getComponentCostLegacy(component);
        assertEquals(CostFactory.minimumComponentCost, val, delta);
    }

    @Test
    public void getComponentCostLegacy__returns_almost_maximumComponentCost_far_above_roi_boundary_position() {
        CostFactory.maximumComponentCost = 1.2; /* test for value that is not symmetric to minimum value for more robust test */
        CostFactory.minimumComponentCost = -0.2;
        double verticalPosition = MoMA.GL_OFFSET_TOP - 4 * MoMA.COMPONENT_EXIT_RANGE;
        ComponentMock component = new ComponentMock(new double[]{0., verticalPosition});
        float val = CostFactory.getComponentCostLegacy(component);
        assertEquals(CostFactory.maximumComponentCost, val, delta);
    }
}
