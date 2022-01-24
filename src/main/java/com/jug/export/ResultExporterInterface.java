package com.jug.export;

import gurobi.GRBException;

import java.io.File;
import java.util.List;

public interface ResultExporterInterface {
    void export(ResultExporterData resultData) throws GRBException;
}
