package com.jug.export;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ImageFileNameParserTest {

    @Test
    public void testParseImageFileName() {
        ImageFileNameParser sut = new ImageFileNameParser();
        Path imagePath = Paths.get("/home/micha/Documents/01_work/15_moma_notes/02_moma_development/bugfix/20230725-fix-csv-position-column-header-for-new-position-naming-scheme/wt_zwf_oxi_rep2_1_MMStack__8-Pos001_GL29.tif");
        sut.parseFileName(imagePath.getFileName().toString());
    }
}
