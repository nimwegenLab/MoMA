package com.jug.lp;

import com.jug.datahandling.IImageProvider;
import com.jug.util.TestUtils;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class AssignmentFluorescenceFilterTest {
    private final TestUtils testUtils;

    public AssignmentFluorescenceFilterTest() {
        testUtils = new TestUtils();
    }

    public static void main(String[] args) throws IOException {
        AssignmentFluorescenceFilterTest tests = new AssignmentFluorescenceFilterTest();
        tests.test1();
    }

    @Test
    public void test1() throws IOException {
        Path testDataFolder = testUtils.getAbsolutTestFilePath("src/test/resources/00_probability_maps/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12/frames_445-460__20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12");
        IImageProvider imageProvider = testUtils.getImageProviderFromDataFolder(testDataFolder);
        List<Img<FloatType>> imgs = imageProvider.getRawChannelImgs();
        testUtils.show(imgs.get(0));
        System.out.println("stop");
    }
}
