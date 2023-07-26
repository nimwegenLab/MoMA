package com.jug.export;

import com.jug.config.ConfigurationManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class TestDefaultPositionAndGrowthlaneIdRegex {
    String positionRegex1 = ConfigurationManager.getPositionIdRegex();
    String glRegex1 = ConfigurationManager.getGrowthlaneIdRegex();

    /**
     * This is the old file-name scheme, i.e.: [EXPERIMENTDESCRIPTION]_Pos[INT]_GL[INT].tif
     */
    String filenameVariant1 = "20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif";

    /**
     * This is the old file-name scheme, i.e.: [EXPERIMENTDESCRIPTION]__[INT]-Pos[INT]_GL[INT].tif, where the integers
     * in `[INT]-Pos[INT]` are determined by the Micromanager acquisition software plugin that is used to do grid-scans.
     */
    String filenameVariant2 = "wt_zwf_oxi_rep2_1_MMStack__8-Pos001_GL29.tif";

    @ParameterizedTest
    @CsvSource({"20211026_VNG1040_AB6min_2h_1_MMStack_Pos0_GL12.tif,Pos0",
                "20211026_VNG1040_AB6min_2h_1_MMStack_Pos09_GL12.tif,Pos09",
                "20211026_VNG1040_AB6min_2h_1_MMStack_Pos999_GL12.tif,Pos999",
                "wt_zwf_oxi_rep2_1_MMStack__0-Pos001_GL29.tif,0-Pos001",
                "wt_zwf_oxi_rep2_1_MMStack__9-Pos001_GL29.tif,9-Pos001",
                "wt_zwf_oxi_rep2_1_MMStack__9-Pos01_GL29.tif,9-Pos01",
    })
    public void test_position_regex_various_filenames_and_regex_match_to_expected_output(String filename, String expected) {
        RegexParser sut = new RegexParser(positionRegex1);
        sut.parse(filename);
        Assertions.assertEquals(expected, sut.getMatch());
    }

    @ParameterizedTest
    @CsvSource({
            " 20211026_VNG1040_AB6min_2h_1_MMStack_Pos0_GL12.tif,GL12",
            " 20211026_VNG1040_AB6min_2h_1_MMStack_Pos09_GL0.tif,GL0",
            " 20211026_VNG1040_AB6min_2h_1_MMStack_Pos09_GL9.tif,GL9",
            " 20211026_VNG1040_AB6min_2h_1_MMStack_Pos999_GL12.tif,GL12",
            "20211026_VNG1040_AB6min_2h_1_MMStack_Pos09_GL09.tif,GL09",
            "wt_zwf_oxi_rep2_1_MMStack__0-Pos001_GL0.tif,GL0",
            "wt_zwf_oxi_rep2_1_MMStack__9-Pos001_GL01.tif,GL01",
            " wt_zwf_oxi_rep2_1_MMStack__9-Pos01_GL99.tif,GL99",
            " wt_zwf_oxi_rep2_1_MMStack__9-Pos01_GL999.tif,GL999",
    })
    public void test_growthlane_regex_various_filenames_and_regex_match_to_expected_output(String filename, String expected) {
        RegexParser sut = new RegexParser(glRegex1);
        sut.parse(filename);
        Assertions.assertEquals(expected, sut.getMatch());
    }
}
