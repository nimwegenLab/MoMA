package com.jug;

import com.jug.datahandling.IImageProvider;
import com.jug.gui.IDialogManager;
import com.jug.gui.progress.DialogProgress;
import com.jug.lp.GRBModel.GRBModelAdapter;
import com.jug.lp.GRBModel.GRBModelFactory;
import com.jug.lp.GrowthlaneTrackingILP;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jug
 */
public class Growthlane {

	// -------------------------------------------------------------------------------------
	// fields
	// -------------------------------------------------------------------------------------
	private final List<GrowthlaneFrame> frames;
	private GrowthlaneTrackingILP ilp; //<
	private IImageProvider imageProvider;
	private IDialogManager dialogManager;

	// Hypothesis< Component< FloatType, ? > >,
	// AbstractAssignment< Hypothesis< Component< FloatType, ? > > > > ilp;

	// -------------------------------------------------------------------------------------
	// setters and getters
	// -------------------------------------------------------------------------------------
	/**
	 * @return the frames
	 */
	public List<GrowthlaneFrame> getFrames() {
		return frames;
	}

	/**
	 * @return the ILP
	 */
	public GrowthlaneTrackingILP getIlp() {
		return ilp;
	}

	// -------------------------------------------------------------------------------------
	// constructors
	// -------------------------------------------------------------------------------------
	public Growthlane(IImageProvider imageProvider, IDialogManager dialogManager) {
		this.imageProvider = imageProvider;
		this.dialogManager = dialogManager;
		this.frames = new ArrayList<>();
	}

	// -------------------------------------------------------------------------------------
	// methods
	// -------------------------------------------------------------------------------------
	/**
	 * @return the number of frames (time-steps) in this <code>Growthlane</code>
	 */
	public int size() {
		return frames.size();
	}

	/**
	 * @param frame
	 *            the GrowthlaneFrame to be appended as last frame
     */
	public void add(final GrowthlaneFrame frame ) {
		frame.setParent( this );
        frames.add(frame);
    }

	/**
	 * @return
	 */
	public GrowthlaneFrame get(final int i) {
		try {
			return this.getFrames().get(i);
		} catch (IndexOutOfBoundsException err) {
			return null;
		}
	}

	/**
	 * Builds up the ILP used to find the MAP-mapping.
	 */
	public void generateILP( final DialogProgress guiProgressReceiver ) {
		if ( guiProgressReceiver != null ) {
			guiProgressReceiver.setVisible( true );
		}

		boolean loadModelFromDisk = true;
		GRBModelAdapter model = null;
//		modelGurobi = null;
		if (loadModelFromDisk)
			try {
				String basePath = "/media/micha/T7/data_michael_mell/moma_test_data/000_development/feature/20220121-fix-loading-of-curated-datasets/dany_20200730__Pos3_GL16/output/";
//				String basePath = "/media/micha/T7/20210816_test_data_michael/Moma/MM_Testing/000_development/feature/20220121-fix-loading-of-curated-datasets/lis_20211026__Pos7_GL12/output/";
				GRBEnv env = new GRBEnv("MotherMachineILPs.log");
				GRBModel grbModel = new GRBModel(env, basePath + "/ilpModel.mps");
				model = new GRBModelAdapter(grbModel);
			} catch (GRBException e) {
				e.printStackTrace();
			}
		else {
			model = GRBModelFactory.getModel();
		}

//		try {
////			model.read("/media/micha/T7/20210816_test_data_michael/Moma/MM_Testing/000_moma_benchmarking/other_test_data/dany_20200730__Pos3_GL16/output/ilpModel.lp");
//			model.read("/media/micha/T7/20210816_test_data_michael/Moma/MM_Testing/000_moma_benchmarking/other_test_data/dany_20200730__Pos3_GL16/output/ilpModel.mps");
//		} catch (GRBException e) {
//			e.printStackTrace();
//		}
		ilp = new GrowthlaneTrackingILP(this, model, imageProvider, MoMA.dic.getAssignmentPlausibilityTester(), MoMA.dic.getTrackingConfiguration(), MoMA.dic.getGitVersionProvider().getVersionString());
		if ( guiProgressReceiver != null ) {
			ilp.addProgressListener( guiProgressReceiver );
		}
		ilp.addDialogManger(this.dialogManager);
		ilp.buildILP();

		if ( guiProgressReceiver != null ) {
			guiProgressReceiver.setVisible( false );
			guiProgressReceiver.dispose();
		}
	}
}
