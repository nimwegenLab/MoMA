package com.jug;

import com.jug.datahandling.FilePaths;
import com.jug.datahandling.IImageProvider;
import com.jug.gui.IDialogManager;
import com.jug.gui.progress.DialogProgress;
import com.jug.lp.GRBModel.GRBModelAdapter;
import com.jug.lp.GRBModel.GRBModelFactory;
import com.jug.lp.GrowthlaneTrackingILP;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import org.threadly.concurrent.collections.ConcurrentArrayList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

/**
 * @author jug
 */
public class Growthlane {

	// -------------------------------------------------------------------------------------
	// fields
	// -------------------------------------------------------------------------------------
	private final List<GrowthlaneFrame> frames;
	private GrowthlaneTrackingILP ilp;
	private IImageProvider imageProvider;
	private IDialogManager dialogManager;
	private FilePaths filePaths;

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
	public Growthlane(IImageProvider imageProvider, IDialogManager dialogManager, FilePaths filePaths) {
		this.imageProvider = imageProvider;
		this.dialogManager = dialogManager;
		this.filePaths = filePaths;
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

//		boolean loadModelFromDisk = false;
		GRBModelAdapter model = null;
//		modelGurobi = null;
		if (!isNull(filePaths.getGurobiMpsFilePath()))
			try {
//				String basePath = "/media/micha/T7/data_michael_mell/moma_test_data/000_development/feature/20220121-fix-loading-of-curated-datasets/dany_20200730__Pos3_GL16/output/";
//				String basePath = "/media/micha/T7/20210816_test_data_michael/Moma/MM_Testing/000_development/feature/20220121-fix-loading-of-curated-datasets/lis_20211026__Pos7_GL12/output/";
				GRBEnv env = new GRBEnv("MotherMachineILPs.log");
//				GRBModel grbModel = new GRBModel(env, basePath + "/ilpModel.mps");
				GRBModel grbModel = new GRBModel(env, filePaths.getGurobiMpsFilePath().toString());
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
		ilp = new GrowthlaneTrackingILP(this, model, MoMA.dic.getAssignmentPlausibilityTester(), MoMA.dic.getTrackingConfiguration(), MoMA.dic.getConfigurationManager(), MoMA.dic.getGitVersionProvider().getVersionString(), MoMA.dic.getCostFactory());
		if ( guiProgressReceiver != null ) {
			ilp.addProgressListener( guiProgressReceiver );
		}
		ilp.addDialogManger(this.dialogManager);
		ilp.buildILP();
		ilp.setRemoveStorageLockConstraintAfterFirstOptimization();

		if ( guiProgressReceiver != null ) {
			guiProgressReceiver.setVisible( false );
			guiProgressReceiver.dispose();
		}

		ilp.addChangeListener((e) -> fireStateChanged());
	}

	public boolean ilpIsReady() {
		if (isNull(ilp)) {
			return false;
		}
		return ilp.isReady();
	}

	private List<ChangeListener> listenerList = new ConcurrentArrayList<>(); /* MM-20220628: use concurrent array listeners so that we can remove listener-callback from with in the callback without a concurrent modification error; this seems hacky */

	public void addChangeListener(ChangeListener l) {
		listenerList.add(l);
	}

	public void removeChangeListener(ChangeListener l) {
		listenerList.remove(l);
	}

	public void fireStateChanged() {
		for (ChangeListener listener : listenerList) {
			listener.stateChanged(new ChangeEvent(this));
		}
	}
}
