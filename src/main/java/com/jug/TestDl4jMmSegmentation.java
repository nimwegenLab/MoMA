package com.jug;

import org.nd4j.imports.graphmapper.tf.TFGraphMapper;
import org.nd4j.autodiff.samediff.SDVariable;
import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.FileStatsStorage;

import com.jug.util.FloatTypeImgLoader;

import net.imagej.ops.Ops;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;


public class TestDl4jMmSegmentation{
    private static SameDiff sd;

    public static void loadModel(String file) throws Exception{
        sd = TFGraphMapper.getInstance().importGraph(new File(file));
        if (sd == null){
            throw new Exception("Error loading model : " + file);
        }
    }

//    public static INDArray predict(INDArray arr){
//        //INDArray batchedArr = Nd4j.expandDims(arr, 0);
//        arr = Nd4j.pile(arr, arr);
//
//        sd.associateArrayWithVariable(arr, sd.variables().get(0));
//        INDArray out = sd.execAndEndResult();
////        return Nd4j.get(NDArrayIndex.point(0));
//        return out;
//    }

    public static INDArray predictBatch(INDArray arr){
        sd.associateArrayWithVariable(arr, sd.variables().get(0));
        return sd.execAndEndResult();
    }

    public static INDArray predict(String filepath){
        File file = new File(filepath);
        BufferedImage img = null;
		try {
			img = ImageIO.read(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        double[] data = new double[28 * 28];
        for(int i = 0; i < 28; i++){
            for(int j = 0; j < 28; j++){
                data[i * 28 + j] = (double)img.getRGB(i, j) / 255.0;
            }
        }
        INDArray arr = Nd4j.create(data).reshape(1, 28, 28);
        arr = Nd4j.pile(arr, arr);
        sd.associateArrayWithVariable(arr, sd.variables().get(0));
        INDArray output = sd.execAndEndResult().get(NDArrayIndex.point(0));
        return output;

    }

    public static int predictionToLabel(INDArray prediction){
        return Nd4j.argMax(prediction.reshape(10)).getInt(0);
    }
    
    public static void main(String[] args){
        try {
			loadModel("/home/micha/Documents/01_work/git/dl4j_testing/dl4j_keras_model_import/src/main/resources/tf_model.pb");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        for(int i = 1; i < 11; i++){
            String imageFilename = "/home/micha/Documents/01_work/git/MM_Testing/10_20190424_hi2_hi3_med2_rplN_glu_gly/output/Pos0/GL3/20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3.tiff";
        	readImageFile(imageFilename);
        	
        	//////
            String file = "/home/micha/Documents/01_work/git/tf_java_import_testing/tf-import/mnist/images/img (%d).jpg";
            file = String.format(file, i);
            INDArray prediction = predict(file);
            int label = predictionToLabel(prediction);
            System.out.println(file + "  ====  " + label);
        }

    }
    
    private static List< Img< FloatType >> rawChannelImgs;
    private static Img< FloatType > imgRaw;
    private static INDArray imageINDarray;    
    
    public static void readImageFile(String imageFilename) {
    	System.out.println("ImageFilename: "+imageFilename);
		try {
			rawChannelImgs = FloatTypeImgLoader.loadTiffsFromFileOrFolder(imageFilename, 1, 2, 1, 1);
			imgRaw = rawChannelImgs.get( 0 );
			ConvertIpToINDArray(imgRaw);
//			ImageJFunctions.show( imgRaw );
//			final RandomAccessibleInterval<FloatType> centralArea = Views.interval( imgRaw, 0, 500 );
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static INDArray ConvertIpToINDArray(Img<FloatType> img){
    	final RandomAccess< FloatType > r = img.randomAccess();
    	int xSize = (int)img.max(0);
    	int ySize = (int)img.max(1);
    	int zSize = (int)img.max(2);
    	
    	System.out.println(xSize);
    	System.out.println(ySize);
    	System.out.println(zSize);

    	double[] data = new double[xSize * ySize];
        for(int x = 0; x < xSize; x++){ 
            for(int y = 0; y < ySize; y++){
            	r.setPosition(x,0);
            	r.setPosition(y,1);
            	r.setPosition(0,2);
            	data[x * ySize + y] = r.get().getRealFloat();
//            	data[x * ySize + y] = r.get().getRealFloat() / 255.0;
//            	System.out.println(val1);
//            	double nval = Ops.Stats.Max
//            	System.out.println(r.get());
//            	ImageJFunctions.convertFloat(imp)
//                data[i * 28 + j] = (double)img.getRGB(i, j) / 255.0;
//            	data[x * ySize + y] = ij.op().convert().float32(r.get());
            }
        }
        INDArray arr = Nd4j.create(data).reshape(1, xSize, ySize);
        arr = Nd4j.pile(arr, arr);
    	SDVariable variable = sd.variables().get(0);
        sd.associateArrayWithVariable(arr, variable);
        INDArray output = sd.execAndEndResult().get(NDArrayIndex.point(0));
//        return output;
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
