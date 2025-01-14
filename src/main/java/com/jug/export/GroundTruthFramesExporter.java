package com.jug.export;

import com.jug.Growthlane;
import com.jug.datahandling.IGlExportFilePathGetter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Exporter for the list of frames that were selected as ground truth frames.
 * This list is written as a CSV. It also contains paths to the input-image and
 * the image with the ground-truth masks.
 */
public class GroundTruthFramesExporter implements ResultExporterInterface {
    private final List<Integer> listOfFrameNumbers;
    public GroundTruthFramesExporter() {
        listOfFrameNumbers = new ArrayList<>();
    }

    /**
     * Add frame to list of ground-truth frames.
     *
     * @param frameNumber
     */
    public void addFrame(Integer frameNumber) {
        if (!containsFrame(frameNumber)) {
            listOfFrameNumbers.add(frameNumber);
        }
    }

    /**
     * Remove frame from list of ground-truth frames.
     *
     * @param frameNumber
     */
    public void removeFrame(Integer frameNumber) {
        if (containsFrame(frameNumber)) {
            listOfFrameNumbers.remove(frameNumber);
        }
    }

    /**
     * Write list of ground-truth frames to CSV file.
     * The CSV file also contains the paths to the input-image and the image with ground-truth cell-masks.
     *
     * @param resultData
     */
    @Override
    public void export(Growthlane gl, IGlExportFilePathGetter exportFilePaths) {
        if (listOfFrameNumbers.isEmpty()) { /* there are no ground truth frames specified, so we do not do anything */
            return;
        }
        listOfFrameNumbers.sort((x, y) -> {
            if (x > y) {
                return 1;
            } else if (x < y) {
                return -1;
            } else {
                return 0;
            }
        });
        ResultTable resultTable = new ResultTable(",");
        ResultTableColumn<Integer> timeStepColumn = resultTable.addColumn(new ResultTableColumn<>("time_step"));

        for (int frameNumber : listOfFrameNumbers) {
            timeStepColumn.addValue(frameNumber);
        }

        File outputFile = exportFilePaths.getGroundTruthFrameListFilePath().toFile();
        String path = outputFile.getAbsolutePath();
        OutputStreamWriter out = null;
        try {
            out = new OutputStreamWriter(new FileOutputStream(path));
            writeImagePaths(out, exportFilePaths.getInputImagePath().toString(), exportFilePaths.getCellMaskImageFilePath().toString());
            resultTable.writeTable(out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write paths to the input-image and the image containing the ground-truth cell-masks.
     *
     * @param out
     * @param inputImagePath
     * @param cellMaskImagePath
     * @throws IOException
     */
    private void writeImagePaths(OutputStreamWriter out, String inputImagePath, String cellMaskImagePath) throws IOException {
        out.write(String.format("input_image=\"%s\"\n", inputImagePath));
        out.write(String.format("ground_truth_mask_image=\"%s\"\n", cellMaskImagePath));
        out.write("\n");
    }

    /**
     * Return, whether the frame is already in the list.
     *
     * @param frameNumber
     * @return
     */
    public boolean containsFrame(int frameNumber) {
        return listOfFrameNumbers.contains(frameNumber);
    }
}
