package com.jug.lp.costs;

import com.jug.config.ConfigurationManager;
import com.jug.mocks.ConfigMock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CostFactoryTest {
    private CostFactory costFactory;
    double delta = 0.01;

    int GL_OFFSET_TOP;
    float COMPONENT_EXIT_RANGE;

    public CostFactoryTest() {
        costFactory = new CostFactory(new ConfigMock());
        GL_OFFSET_TOP = 65;
        COMPONENT_EXIT_RANGE = 50;
    }

    @Test
    public void getCostFactorComponentExit__returns_05_at_roi_boundary_position() {
        double verticalPosition = GL_OFFSET_TOP;
        ComponentMock component = new ComponentMock(new double[]{0., verticalPosition});
        double val = costFactory.getCostFactorComponentExit(component);
        assertEquals(0.5, val, delta);
    }

    @Test
    public void getCostFactorComponentExit__returns_almost_zero_far_below_roi_boundary_position() {
        double verticalPosition = GL_OFFSET_TOP + 4 * COMPONENT_EXIT_RANGE;
        ComponentMock component = new ComponentMock(new double[]{0., verticalPosition});
        double val = costFactory.getCostFactorComponentExit(component);
        assertEquals(1., val, delta);
    }

    @Test
    public void getCostFactorComponentExit__returns_almost_one_far_above_roi_boundary_position() {
        double verticalPosition = GL_OFFSET_TOP - 4 * COMPONENT_EXIT_RANGE;
        ComponentMock component = new ComponentMock(new double[]{0., verticalPosition});
        double val = costFactory.getCostFactorComponentExit(component);
        assertEquals(0., val, delta);
    }

    @Test
    public void getComponentCostLegacy__returns_zero_at_roi_boundary_position_for_symmetric_min_and_max_component_cost(){
        costFactory.maximumComponentCost = 0.2;
        costFactory.minimumComponentCost = -0.2;
        double verticalPosition = GL_OFFSET_TOP;
        ComponentMock component = new ComponentMock(new double[]{0., verticalPosition});
        float val = costFactory.getComponentCostLegacy(component);
        assertEquals(.0, val, delta);
    }

    @Test
    public void getComponentCostLegacy__returns_almost_minimumComponentCost_far_below_roi_boundary_position() {
        costFactory.maximumComponentCost = 1.2; /* test for value that is not symmetric to minimum value for more robust test */
        costFactory.minimumComponentCost = -0.2;
        double verticalPosition = GL_OFFSET_TOP + 4 * COMPONENT_EXIT_RANGE;
        ComponentMock component = new ComponentMock(new double[]{0., verticalPosition});
        float val = costFactory.getComponentCostLegacy(component);
        assertEquals(costFactory.minimumComponentCost, val, delta);
    }

    @Test
    public void getComponentCostLegacy__returns_almost_maximumComponentCost_far_above_roi_boundary_position() {
        costFactory.maximumComponentCost = 1.2; /* test for value that is not symmetric to minimum value for more robust test */
        costFactory.minimumComponentCost = -0.2;
        double verticalPosition = GL_OFFSET_TOP - 4 * COMPONENT_EXIT_RANGE;
        ComponentMock component = new ComponentMock(new double[]{0., verticalPosition});
        float val = costFactory.getComponentCostLegacy(component);
        assertEquals(costFactory.maximumComponentCost, val, delta);
    }
}
