package com.jug.gui;

import bdv.util.BdvSource;
import com.indago.labeleditor.LabelEditorBdvPanel;
import com.jug.GrowthLineFrame;
import net.imagej.ImgPlus;
import net.imglib2.img.ImgView;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;

import java.util.List;

public class LabelEditorBdvPanelExtended extends LabelEditorBdvPanel {

    private List<BdvSource> sources;

    public void setScreenImage(GrowthLineFrame glf, IntervalView<FloatType> viewImgLeftActive) {
        this.init(new ImgPlus(ImgView.wrap(viewImgLeftActive, new ArrayImgFactory<>(viewImgLeftActive.firstElement()))), null);
        this.sources = this.bdvGetSources();
        for (BdvSource source : sources){
            source.setDisplayRange(0, 1);
        }
    }
}
