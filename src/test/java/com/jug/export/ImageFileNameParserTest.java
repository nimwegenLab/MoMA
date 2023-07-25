package com.jug.export;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ImageFileNameParserTest {

    @Test
    public void getPositionId__file_name_variant_1__positionId_is_correct() {
        ImageFileNameParser sut = new ImageFileNameParser();
//        Path imagePath = Paths.get("/home/micha/Documents/01_work/15_moma_notes/02_moma_development/bugfix/20230725-fix-csv-position-column-header-for-new-position-naming-scheme/wt_zwf_oxi_rep2_1_MMStack__8-Pos001_GL29.tif");
//        sut.parseFileName(imagePath.getFileName().toString());
        sut.parseFileName("wt_zwf_oxi_rep2_1_MMStack__8-Pos001_GL29.tif");
        Assertions.assertEquals("8-Pos001", sut.getPositionId());
    }

    @Test
    public void getPositionId__file_name_variant_1__glId_is_correct() {
        ImageFileNameParser sut = new ImageFileNameParser();
        sut.parseFileName("wt_zwf_oxi_rep2_1_MMStack__8-Pos001_GL29.tif");
        Assertions.assertEquals("GL29", sut.getGlId());
    }
}
