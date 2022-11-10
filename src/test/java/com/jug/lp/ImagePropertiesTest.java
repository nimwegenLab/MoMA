package com.jug.lp;

import com.jug.datahandling.IImageProvider;
import com.jug.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

public class ImagePropertiesTest {
    private final TestUtils testUtils;
    private final ImageProperties sut;

    public ImagePropertiesTest() {
        testUtils = new TestUtils();
        sut = new ImageProperties(testUtils.getImglib2Utils());
    }

    @Test
    public void getBackgroundIntensityMean__when_called__returns_expected_value() throws IOException {
        Path testDataFolder = testUtils.getAbsolutTestFilePath("src/test/resources/00_probability_maps/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12/frames_445-460__20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12");
        IImageProvider imageProvider = testUtils.getImageProviderFromDataFolder(testDataFolder);
        double expectedMeanBackgroundIntensity = 11.770502803772166;
        int channelNumber = 1;

        double backgroundIntensity = sut.getBackgroundIntensityMean(imageProvider, channelNumber);

        Assert.assertEquals(expectedMeanBackgroundIntensity, backgroundIntensity, 1e-6);
    }
}
