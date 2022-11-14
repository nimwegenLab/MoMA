package com.jug.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.jug.fijiplugins.Utilities;

import ij.IJ;
import ij.ImagePlus;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: November 2016
 */
public class FloatTypeImgLoaderTest {

    @Test
    public void testGetTimeAndChannelFromFilename() {
        String filename1 = "example_t001_c00005.tif";
        Assertions.assertEquals(1, FloatTypeImgLoader.getTimeFromFilename(filename1));
        Assertions.assertEquals(5, FloatTypeImgLoader.getChannelFromFilename(filename1));

        String filename2 = "example_test_case_t003_c00004.tif";
        Assertions.assertEquals(3, FloatTypeImgLoader.getTimeFromFilename(filename2));
        Assertions.assertEquals(4, FloatTypeImgLoader.getChannelFromFilename(filename2));
    }

    @Test
    public void testLoadingSequenceFromFolder() throws FileNotFoundException {
        String filename = new File("").getAbsolutePath() + "/src/test/resources/sequence_as_file_8bit.tif";
        String foldername = resaveTifAsTifSequence(filename);

        System.out.print(foldername);

        ArrayList<Img<FloatType>> list = FloatTypeImgLoader.loadTiffsFromFileOrFolder(foldername, 1, 5, 1, 2);

        // check number of channels
        Assertions.assertEquals(2, list.size());

        // check dimensionality
        Assertions.assertEquals(3, list.get(0).numDimensions());

        // check width
        Assertions.assertEquals(0, list.get(0).min(0));
        Assertions.assertEquals(2, list.get(0).max(0));

        // check height
        Assertions.assertEquals(0, list.get(0).min(1));
        Assertions.assertEquals(3, list.get(0).max(1));

        // check frames
        Assertions.assertEquals(0, list.get(0).min(2));
        Assertions.assertEquals(5, list.get(0).max(2));
        // note: in the implementation of FloatTypeImgLoader.loadMMTiffSequence it is documented that the last slice
        // is duplicated. That's why here the reference for nuber of slices is in fact 6, even though the dataset just
        // contains 5 slices...
    }


    @Test
    public void testLoadingSequenceFromFile() throws FileNotFoundException {
        String filename = new File("").getAbsolutePath() + "/src/test/resources/sequence_as_file_8bit.tif";

        System.out.print(filename);

        ArrayList<Img<FloatType>> list = FloatTypeImgLoader.loadTiffsFromFileOrFolder(filename, 1, 5, 1, 2);

        // check number of channels
        Assertions.assertEquals(2, list.size());

        // check dimensionality
        Assertions.assertEquals(3, list.get(0).numDimensions());

        // check width
        Assertions.assertEquals(0, list.get(0).min(0));
        Assertions.assertEquals(2, list.get(0).max(0));

        // check height
        Assertions.assertEquals(0, list.get(0).min(1));
        Assertions.assertEquals(3, list.get(0).max(1));

        // check frames
        Assertions.assertEquals(0, list.get(0).min(2));
        Assertions.assertEquals(5, list.get(0).max(2));
        // note: in the implementation of FloatTypeImgLoader.loadMMTiffSequence it is documented that the last slice
        // is duplicated. That's why here the reference for nuber of slices is in fact 6, even though the dataset just
        // contains 5 slices...

    }

    @Test
    public void testIfLoadingFilesAndFoldersResultsInEqualImages()throws FileNotFoundException
    {
        float tolerance = 0.001f;


        String filename8bit = new File("").getAbsolutePath() + "/src/test/resources/sequence_as_file_8bit.tif";
        ArrayList<Img<FloatType>> listFile8Bit = FloatTypeImgLoader.loadTiffsFromFileOrFolder(filename8bit, 1, 5, 1, 2);
        String foldername8bit = resaveTifAsTifSequence(filename8bit);
        ArrayList<Img<FloatType>> listFolder8Bit = FloatTypeImgLoader.loadTiffsFromFileOrFolder(foldername8bit, 1, 5, 1, 2);


        String filename32bit = new File("").getAbsolutePath() + "/src/test/resources/sequence_as_file_8bit.tif";
        ArrayList<Img<FloatType>> listFile32Bit = FloatTypeImgLoader.loadTiffsFromFileOrFolder(filename32bit, 1, 5, 1, 2);
        String foldername32bit = resaveTifAsTifSequence(filename32bit);
        ArrayList<Img<FloatType>> listFolder32Bit = FloatTypeImgLoader.loadTiffsFromFileOrFolder(foldername32bit, 1, 5, 1, 2);

        Assertions.assertEquals(listFile8Bit.size(), listFile32Bit.size());
        Assertions.assertEquals(listFile8Bit.size(), listFolder8Bit.size());
        Assertions.assertEquals(listFile8Bit.size(), listFolder32Bit.size());

        // do for every channel...
        for (int i = 0; i < listFolder8Bit.size(); i++) {
            imagesAreEqual(listFolder8Bit.get(i), listFolder32Bit.get(i), tolerance);
            imagesAreEqual(listFile8Bit.get(i), listFile32Bit.get(i), tolerance);
            imagesAreEqual(listFile8Bit.get(i), listFolder8Bit.get(i), tolerance);
            imagesAreEqual(listFile8Bit.get(i), listFolder32Bit.get(i), tolerance);
        }
    }

    @Test
    public void testIfLoadingRealisticDataWorks()throws FileNotFoundException
    {
        String fileDatasetFilename = new File("").getAbsolutePath() + "/src/test/resources/realisticDataExample.tif";
        String folderDatasetFilename = resaveTifAsTifSequence(fileDatasetFilename);

        ArrayList<Img<FloatType>> fileDataset = FloatTypeImgLoader.loadTiffsFromFileOrFolder(fileDatasetFilename, -1, -1, 1, 2);
        ArrayList<Img<FloatType>> folderDataset = FloatTypeImgLoader.loadTiffsFromFileOrFolder(folderDatasetFilename, -1, -1, 1, 2);

        System.out.println("size: " + fileDataset.size());
        System.out.println("size: " + folderDataset.size());

        // check number of channels
        Assertions.assertEquals(2, fileDataset.size());
        Assertions.assertEquals(2, folderDataset.size());

        // check dimensionality
        Assertions.assertEquals(3, fileDataset.get(0).numDimensions());
        Assertions.assertEquals(3, folderDataset.get(0).numDimensions());

        // check width
        Assertions.assertEquals(0, fileDataset.get(0).min(0));
        Assertions.assertEquals(100, fileDataset.get(0).max(0));
        Assertions.assertEquals(0, folderDataset.get(0).min(0));
        Assertions.assertEquals(100, folderDataset.get(0).max(0));

        // check height
        Assertions.assertEquals(0, fileDataset.get(0).min(1));
        Assertions.assertEquals(444, fileDataset.get(0).max(1));
        Assertions.assertEquals(0, folderDataset.get(0).min(1));
        Assertions.assertEquals(444, folderDataset.get(0).max(1));

        // check frames
        Assertions.assertEquals(0, fileDataset.get(0).min(2));
        Assertions.assertEquals(40, fileDataset.get(0).max(2));
        Assertions.assertEquals(0, folderDataset.get(0).min(2));
        Assertions.assertEquals(40, folderDataset.get(0).max(2));
    }







    private void imagesAreEqual(Img<FloatType> a, Img<FloatType> b, float tolerance) {
        // check references
        Assertions.assertFalse("Both images not null", (a == null || b == null));
        Assertions.assertFalse("Both images have equal dimensions", (a.numDimensions() != b.numDimensions()));

        // check dimensions
        for (int d = 0; d < a.numDimensions(); d++) {
            Assertions.assertFalse("minimum position " + d + " equals", (a.min(d) != b.min(d)));
            Assertions.assertFalse("maximum position " + d + " equals", (a.max(d) != b.max(d)));
        }

        Cursor<FloatType> curA = a.cursor();
        Cursor<FloatType> curB = b.cursor();

        // Note: the order of pixels in the following code is not garanteed. However, as far as I know, it works always.
        while (curA.hasNext() && curB.hasNext()) {
            Assertions.assertEquals("pixels equal", curA.next().get(), curB.next().get(), tolerance);
        }

        // this should never be asserted, we just checked min and max positions...
        Assertions.assertFalse("Cursors add at same point", (curA.hasNext() || curB.hasNext()));
    }

    private String resaveTifAsTifSequence(String tifFilename)
    {
        ImagePlus imp = IJ.openImage(tifFilename);

        // -----------------------------------------------
        // resave tif as sequence of tifs (slice by slice)
        String tempFolder = IJ.getDirectory("temp");
        String suffix = "moma";
        int count = 0;
        while (new File(tempFolder + suffix).exists()) {
            count++;
            suffix = "moma" + count;
        }
        String targetFolder = tempFolder + suffix + "/";
        Utilities.ensureFolderExists(targetFolder);
        IJ.run(imp, "Image Sequence... ", "format=TIFF digits=4 save=[" + targetFolder + "]");

        return targetFolder;
    }
}
