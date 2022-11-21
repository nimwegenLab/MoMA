package com.jug.lp;

import com.jug.config.IConfiguration;
import com.jug.datahandling.IImageProvider;
import com.jug.util.TestUtils;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import org.apache.commons.lang.NotImplementedException;
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
    private ImageProperties sut;
    private IImageProvider imageProvider;

    public ImagePropertiesTest() throws IOException {
        testUtils = new TestUtils();
        Path testDataFolder = testUtils.getAbsolutTestFilePath("src/test/resources/00_probability_maps/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12/frames_445-460__20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12");
        imageProvider = testUtils.getImageProviderFromDataFolder(testDataFolder);
        IConfiguration config = mock(IConfiguration.class);
        when(config.getBackgroundRoiWidth()).thenReturn(5L);
        sut = new ImageProperties(imageProvider, testUtils.getImglib2Utils(), config);
    }

    @Test
    public void getBackgroundIntensityMean__for_test_image_with_normally_distributed_intensities__returns_expected_value() {
        double expectedMean = 3.5;
        double expectedStd = 4.5;
        Img<FloatType> img = testUtils.getImageWithNormallyDistributedIntensities(new long[]{2000, 2000, 10}, expectedMean, expectedStd);
        IConfiguration config = mock(IConfiguration.class);
        when(config.getBackgroundRoiWidth()).thenReturn(5L);
        imageProvider = mock(IImageProvider.class);
        when(imageProvider.getChannelImg(0)).thenReturn(img);
        sut = new ImageProperties(imageProvider, testUtils.getImglib2Utils(), config);
        double actual = sut.getBackgroundIntensityMean(0);
        Assertions.assertEquals(expectedMean, actual, 0.1);
    }

    @Test
    public void getBackgroundIntensityStd__for_test_image_with_normally_distributed_intensities__returns_expected_value() {
        double expectedMean = 3.0;
        double expectedStd = 4.5;
        Img<FloatType> img = testUtils.getImageWithNormallyDistributedIntensities(new long[]{2000, 2000, 10}, expectedMean, expectedStd);
        IConfiguration config = mock(IConfiguration.class);
        when(config.getBackgroundRoiWidth()).thenReturn(5L);
        imageProvider = mock(IImageProvider.class);
        when(imageProvider.getChannelImg(0)).thenReturn(img);
        sut = new ImageProperties(imageProvider, testUtils.getImglib2Utils(), config);
        double actual = sut.getBackgroundIntensityStd(0);
        Assertions.assertEquals(expectedStd, actual, 0.1);
    }

    @Test
    public void getBackgroundIntensityMeanAtFrame__for_test_image_with_normally_distributed_intensities__returns_expected_value() {
        double expectedMean = 3.5;
        double expectedStd = 4.5;
        Img<FloatType> img = testUtils.getImageWithNormallyDistributedIntensities(new long[]{2000, 2000, 10}, expectedMean, expectedStd);
        IConfiguration config = mock(IConfiguration.class);
        when(config.getBackgroundRoiWidth()).thenReturn(5L);
        imageProvider = mock(IImageProvider.class);
        when(imageProvider.getChannelImg(0)).thenReturn(img);
        sut = new ImageProperties(imageProvider, testUtils.getImglib2Utils(), config);
        double actual = sut.getBackgroundIntensityMeanAtFrame(0, 0);
        Assertions.assertEquals(expectedMean, actual, 0.1);
    }

    @Test
    public void getBackgroundIntensityStdAtFrame__for_test_image_with_normally_distributed_intensities__returns_expected_value() {
        double expectedMean = 3.0;
        double expectedStd = 4.5;
        Img<FloatType> img = testUtils.getImageWithNormallyDistributedIntensities(new long[]{2000, 2000, 10}, expectedMean, expectedStd);
        IConfiguration config = mock(IConfiguration.class);
        when(config.getBackgroundRoiWidth()).thenReturn(5L);
        imageProvider = mock(IImageProvider.class);
        when(imageProvider.getChannelImg(0)).thenReturn(img);
        sut = new ImageProperties(imageProvider, testUtils.getImglib2Utils(), config);
        double actual = sut.getBackgroundIntensityStdAtFrame(0, 0);
        Assertions.assertEquals(expectedStd, actual, 0.1);
    }

    @Test
    public void getBackgroundIntensityMeanAtFrame__for_test_image_with_all_ones__returns_expected_value() {
        double expected = 5.0;
        Img<FloatType> img = testUtils.getImageWithValue(new long[]{100, 100, 10}, new FloatType(5.0f));
        IConfiguration config = mock(IConfiguration.class);
        when(config.getBackgroundRoiWidth()).thenReturn(5L);
        imageProvider = mock(IImageProvider.class);
        when(imageProvider.getChannelImg(0)).thenReturn(img);
        sut = new ImageProperties(imageProvider, testUtils.getImglib2Utils(), config);
        double actual = sut.getBackgroundIntensityMeanAtFrame(0, 0);
        Assertions.assertEquals(expected, actual, testUtils.getDeltaDouble());
    }

    @Test
    public void getBackgroundIntensityMean__for_test_image_with_all_ones__returns_expected_value() {
        double expected = 5.0;
        Img<FloatType> img = testUtils.getImageWithValue(new long[]{100, 100, 10}, new FloatType(5.0f));
        IConfiguration config = mock(IConfiguration.class);
        when(config.getBackgroundRoiWidth()).thenReturn(5L);
        imageProvider = mock(IImageProvider.class);
        when(imageProvider.getChannelImg(0)).thenReturn(img);
        sut = new ImageProperties(imageProvider, testUtils.getImglib2Utils(), config);
        double actual = sut.getBackgroundIntensityMean(0);
        Assertions.assertEquals(expected, actual, testUtils.getDeltaDouble());
    }

    @ParameterizedTest()
    @CsvSource({
            "0, 0, 0.4795548620473671",
            "0, 1, 0.48380217724976876",
            "0, 2, 0.4821164756061359",
            "1, 0, 6.528906751019308",
            "1, 1, 6.42421792988075",
            "1, 2, 6.428327449416144",
            "2, 0, 6.23168197127447",
            "2, 1, 6.130502912789461",
            "2, 2, 6.1214633915508045",
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

    @ParameterizedTest()
    @CsvSource({
            "0, 0",
            "0, 1",
            "1, 0",
            "1, 1",
            "2, 0",
            "2, 1",
    })
    public void getBackgroundRoiSizeAtFrame__when_called___returns_expected_value(int channelNumber, int frame) {
        long expectedBackgroundRoiSize = 6040;
        long actualBackgroundRoiSize = sut.getBackgroundRoiSizeAtFrame(channelNumber, frame);
        Assertions.assertEquals(expectedBackgroundRoiSize, actualBackgroundRoiSize);
    }

    @Test
    public void getBackgroundRoiSize__when_called___returns_expected_value() {
        long expectedBackgroundRoiSize = 96640;
        long actualBackgroundRoiSize = sut.getBackgroundRoiSize();
        Assertions.assertEquals(expectedBackgroundRoiSize, actualBackgroundRoiSize);
    }

    @ParameterizedTest()
    @CsvSource({
            "0, 0.48609346181233815",
            "1, 6.090577959132455",
            "2, 5.811560385667141",
    })
    public void getBackgroundIntensityStd__when_called_with_valid_channel_number__returns_expected_value(int channelNumber, double expectedBackgroundIntensityStd) {
        double backgroundIntensity = sut.getBackgroundIntensityStd(channelNumber);
        Assertions.assertEquals(expectedBackgroundIntensityStd, backgroundIntensity, 1e-6);
    }

    @ParameterizedTest()
    @CsvSource({
            "0, 0.20344938491543943",
            "1, 6.036146811789383",
            "2, 105.75730546357616",
    })
    public void getBackgroundIntensityMean__when_called_with_valid_channel_number__returns_expected_value(int channelNumber, double expectedMeanBackgroundIntensity) {
        double backgroundIntensity = sut.getBackgroundIntensityMean(channelNumber);
        Assertions.assertEquals(expectedMeanBackgroundIntensity, backgroundIntensity, 1e-6);
    }

    @ParameterizedTest()
    @CsvSource({
            "0, 19661.348558228066",
            "1, 583333.227891326",
            "2, 1.0220386E7",
    })
    public void getBackgroundIntensityTotal__when_called_with_valid_channel_number__returns_expected_value(int channelNumber, double expectedBackgroundIntensityTotal) {
        double backgroundIntensity = sut.getBackgroundIntensityTotal(channelNumber);
        Assertions.assertEquals(expectedBackgroundIntensityTotal, backgroundIntensity, 1e-6);
    }
}
