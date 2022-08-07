package com.jug.export;

import com.jug.Growthlane;
import com.jug.datahandling.IGlExportFilePathGetter;
import com.jug.gui.MoMAGui;
import gurobi.GRBException;

import java.io.File;
import java.util.Objects;

public class HtmlOverviewExporter implements ResultExporterInterface {
    private MoMAGui gui;

    public HtmlOverviewExporter(MoMAGui gui) {
        this.gui = Objects.requireNonNull(gui, "gui is null");
    }

    @Override
    public void export(Growthlane gl, IGlExportFilePathGetter exportFilePaths) throws GRBException {
        File htmlFileToSaveTo = exportFilePaths.getHtmlIndexFilePath();
        String imgpath = exportFilePaths.getHtmlImageDirectoryPath().toString();
        final HtmlOverviewExporterWriter htmlWriter = new HtmlOverviewExporterWriter(gui, htmlFileToSaveTo, imgpath, 0, gl.getTimeStepMaximum());
        htmlWriter.run();
    }
}
