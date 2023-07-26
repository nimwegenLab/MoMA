package com.jug.export;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegexParserTest {
    String positionRegex1 = "([-1-9]*Pos\\d+)_";
    String glRegex1 = "_(GL[0-9]*)\\.tif";

    /**
     * This is the old file-name scheme, i.e.: [EXPERIMENTDESCRIPTION]_Pos[INT]_GL[INT].tif
     */
    String filenameVariant1 = "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif";

    /**
     * This is the old file-name scheme, i.e.: [EXPERIMENTDESCRIPTION]__[INT]-Pos[INT]_GL[INT].tif, where the integers
     * in `[INT]-Pos[INT]` are determined by the Micromanager acquisition software plugin that is used to do grid-scans.
     */
    String filenameVariant2 = "wt_zwf_oxi_rep2_1_MMStack__8-Pos001_GL29.tif";

    @Test
    public void getPositionId__for_glRegex1_and_filenameVariant1__returns_correct_match() {
        RegexParser sut = new RegexParser(positionRegex1);
        sut.parse(filenameVariant1);
        Assertions.assertEquals("Pos7", sut.getMatch());
    }

    @Test
    public void getPositionId__for_posRegex1_and_filenameVariant1__returns_correct_match() {
        RegexParser sut = new RegexParser(glRegex1);
        sut.parse(filenameVariant1);
        Assertions.assertEquals("GL12", sut.getMatch());
    }
    @Test
    public void getPositionId__for_glRegex1_and_filenameVariant2__returns_correct_match() {
        RegexParser sut = new RegexParser(positionRegex1);
        sut.parse(filenameVariant2);
        Assertions.assertEquals("8-Pos001", sut.getMatch());
    }

    @Test
    public void getPositionId__for_posRegex1_and_filenameVariant2__returns_correct_match() {
        RegexParser sut = new RegexParser(glRegex1);
        sut.parse(filenameVariant2);
        Assertions.assertEquals("GL29", sut.getMatch());
    }
}
