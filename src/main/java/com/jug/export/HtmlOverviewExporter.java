package com.jug.export;

import com.jug.gui.MoMAGui;
import com.jug.util.Util;

import javax.swing.*;
import java.io.*;

/**
 * @author jug
 */
public class HtmlOverviewExporter {

    private final MoMAGui gui;
    private final File htmlFile;
    private final String imgpath;
    private final int startFrame;
    private final int endFrame;

    public HtmlOverviewExporter(final MoMAGui gui, final File htmlFile, final String imgpath, final int startFrame, final int endFrame) {
        this.gui = gui;
        this.htmlFile = htmlFile;
        this.imgpath = imgpath;
        this.startFrame = startFrame;
        this.endFrame = endFrame;
    }

    public void run() {

        String basename = htmlFile.getName();
        final int pos = basename.lastIndexOf(".");
        if (pos > 0) {
            basename = basename.substring(0, pos);
        }

        // create folders to imgs if not exists
        final File fImgpath = new File(imgpath);
        if (!fImgpath.exists() && !fImgpath.mkdirs()) {
            JOptionPane.showMessageDialog(gui, "Saving of HTML canceled! Couldn't create dir: " + fImgpath, "Saving canceled...", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Writer out;
        try {
            out = new OutputStreamWriter(new FileOutputStream(htmlFile));

            out.write("<html>\n");
            out.write("<body>\n");
            out.write("	<table border='0' cellspacing='1' cellpadding='0'>\n");
            out.write("		<tr>\n");

            StringBuilder row1 = new StringBuilder();
            final String nextrow = "		</tr>\n			<tr>\n";
            StringBuilder row2 = new StringBuilder();
            final String row3 = "";

            for (int i = startFrame; i <= endFrame; i++) {
                gui.sliderTime.setValue(i);
                try {
                    String fn1 = String.format("/" + basename + "_gl_%02d_glf_%03d.png", gui.sliderGL.getValue(), i);
                    gui.growthLaneViewerCenter.exportScreenImage(imgpath + fn1);
//					final String fn2 = String.format( "/" + basename + "_gl_%02d_glf_%03d_segmentation.png", gui.sliderGL.getValue(), i );
//					gui.imgCanvasActiveCenter.exportSegmentationImage( imgpath + fn2 );
                    row1.append("			<th><font size='+2'>t=").append(i).append("</font></th>\n");
                    row2.append("			<td><img src='./imgs").append(fn1).append("'></td>\n");
//					row3 += "			<td><img src='./imgs" + fn2 + "'></td>\n";

                    if (i < endFrame) {
                        fn1 = String.format("/" + basename + "_gl_%02d_assmnts_%03d.png", gui.sliderGL.getValue(), i);
                        Util.saveImage(Util.getImageOf(gui.assignmentsEditorViewerUsedForHtmlExport.getActiveAssignmentsForHtmlExport(), gui.growthLaneViewerCenter.getWidth(), gui.growthLaneViewerCenter.getHeight()), imgpath + fn1);
                        row1.append("			<th></th>\n");
                        row2.append("			<td><img src='./imgs").append(fn1).append("'></td>\n"); // + "' width='10' height='" + this.imgCanvasActiveCenter.getHeight()
//						row3 += "			<td><img src='./imgs" + fn1 + "'></td>\n"; // + "' width='10' height='" + this.imgCanvasActiveCenter.getHeight()
                    }
                } catch (final IOException e) {
                    JOptionPane.showMessageDialog(gui, "Tracking imagery could not be saved entirely!", "Export Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    out.close();
                    return;
                }
            }

            out.write(row1.toString());
            out.write(nextrow);
            out.write(row2.toString());
            out.write(nextrow);
            out.write(nextrow);
            out.write(row3);

            out.write("		</tr>\n");
            out.write("	</table>\n");
            out.write("</body>\n");
            out.write("</html>\n");

            out.close();
        } catch (final FileNotFoundException e1) {
            JOptionPane.showMessageDialog(gui, "File not found!", "Error!", JOptionPane.ERROR_MESSAGE);
            e1.printStackTrace();
        } catch (final IOException e1) {
            JOptionPane.showMessageDialog(gui, "Selected file could not be written!", "Error!", JOptionPane.ERROR_MESSAGE);
            e1.printStackTrace();
        }

    }

}
