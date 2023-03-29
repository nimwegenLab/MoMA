package com.jug.lp.costs;

import com.jug.util.componenttree.ComponentInterface;

public interface ICostFactory {
    float getComponentCost(final ComponentInterface component);

    double calculateLogLikelihoodComponentCost(ComponentInterface component);
}
