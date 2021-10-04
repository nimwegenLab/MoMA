package com.jug.export;

import java.io.File;
import java.util.List;

public interface ResultExporterInterface {
    void export(File outputFolder, List<SegmentRecord> cellTrackStartingPoints);
}
