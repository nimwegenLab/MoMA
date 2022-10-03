package com.jug.lp;

import gurobi.GRBCallback;

public abstract class GurobiCallbackAbstract extends GRBCallback {
    abstract protected void callback();

    public abstract double getLatestGap();
}
