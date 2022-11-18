package com.jug.lp;

import com.jug.config.IConfiguration;
import com.jug.datahandling.IImageProvider;
import com.jug.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Path;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ImagePropertiesTest {
    private final TestUtils testUtils;
    private final ImageProperties sut;
    private final IImageProvider imageProvider;

    public ImagePropertiesTest() throws IOException {
        testUtils = new TestUtils();
        Path testDataFolder = testUtils.getAbsolutTestFilePath("src/test/resources/00_probability_maps/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12/frames_445-460__20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12");
        imageProvider = testUtils.getImageProviderFromDataFolder(testDataFolder);
        IConfiguration config = mock(IConfiguration.class);
        when(config.getBackgroundRoiWidth()).thenReturn(5L);
        sut = new ImageProperties(imageProvider, testUtils.getImglib2Utils(), config);
    }

    @ParameterizedTest()
    @CsvSource({
            "0, 0, 0.028209109532198063",
            "0, 1, 0.028458951602927576",
            "0, 2, 0.028359792682713877",
            "1, 0, 0.3840533382952534",
            "1, 1, 0.37789517234592646",
            "1, 2, 0.3781369087891849",
            "2, 0, 0.3665695277220276",
            "2, 1, 0.36061781839938",
            "2, 2, 0.36008608185592966",
    })
    public void getBackgroundIntensityStdAtFrame__when_called_with_valid_channel_number_and_frame__returns_expected_value(int channelNumber, int frame, double expectedBackgroundIntensityStd) {
        double backgroundIntensity = sut.getBackgroundIntensityStdAtFrame(channelNumber, frame);
        Assertions.assertEquals(expectedBackgroundIntensityStd, backgroundIntensity, 1e-6);
    }

    @ParameterizedTest()
    @CsvSource({
            "0, 0, 0.20054938643179593",
            "0, 1, 0.20252418814237183",
            "0, 2, 0.201855487359977",
            "1, 0, 9.229112520853414",
            "1, 1, 8.640238352110055",
            "1, 2, 8.544284463737974",
            "2, 0, 108.80546357615894",
            "2, 1, 108.24122516556291",
            "2, 2, 108.14188741721854",
    })
    public void getBackgroundIntensityMeanAtFrame__when_called_with_valid_channel_number_and_frame__returns_expected_value(int channelNumber, int frame, double expectedBackgroundIntensityStd) {
        double backgroundIntensity = sut.getBackgroundIntensityMeanAtFrame(channelNumber, frame);
        Assertions.assertEquals(expectedBackgroundIntensityStd, backgroundIntensity, 1e-6);
    }

    @ParameterizedTest()
    @CsvSource({
            "0, 0, 1211.3182940480474",
            "0, 1, 1223.2460963799258",
            "0, 2, 1219.207143654261",
            "1, 0, 55743.83962595463",
            "1, 1, 52187.03964674473",
            "1, 2, 51607.47816097736",
            "2, 0, 657185.0",
            "2, 1, 653777.0",
            "2, 2, 653177.0",
    })
    public void getBackgroundIntensityTotalAtFrame__when_called_with_valid_channel_number_and_frame__returns_expected_value(int channelNumber, int frame, double expectedBackgroundIntensityStd) {
        double backgroundIntensity = sut.getBackgroundIntensityTotalAtFrame(channelNumber, frame);
        Assertions.assertEquals(expectedBackgroundIntensityStd, backgroundIntensity, 1e-6);
    }

    @Test
    public void getBackgroundRoiSize__when_called___returns_expected_value() {
        long expectedBackgroundRoiSize = 102680;
        long actualBackgroundRoiSize = sut.getBackgroundRoiSize();
        Assertions.assertEquals(expectedBackgroundRoiSize, actualBackgroundRoiSize);
    }

    @ParameterizedTest()
    @CsvSource({
            "0, 24.339816352770796",
            "1, 23.712322855821302",
            "2, 5.7702834107518495",
    })
    public void getBackgroundIntensityStd__when_called_with_valid_channel_number__returns_expected_value(int channelNumber, double expectedBackgroundIntensityStd) {
        double backgroundIntensity = sut.getBackgroundIntensityStd(channelNumber);
        Assertions.assertEquals(expectedBackgroundIntensityStd, backgroundIntensity, 1e-6);
    }

    @ParameterizedTest()
    @CsvSource({
            "0, 6.2809052255378655",
            "1, 11.770502803772166",
            "2, 105.6257109466303",
    })
    public void getBackgroundIntensityMean__when_called_with_valid_channel_number__returns_expected_value(int channelNumber, double expectedMeanBackgroundIntensity) {
        double backgroundIntensity = sut.getBackgroundIntensityMean(channelNumber);
        Assertions.assertEquals(expectedMeanBackgroundIntensity, backgroundIntensity, 1e-6);
    }
}
