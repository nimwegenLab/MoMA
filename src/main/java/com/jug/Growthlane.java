package com.jug;

import com.jug.config.IConfiguration;
import com.jug.datahandling.GlFileManager;
import com.jug.export.CellTrackBuilder;
import com.jug.export.SegmentRecord;
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
import java.util.List;

import static java.util.Objects.isNull;

/**
 * @author jug
 */
public class Growthlane {
	private final List<GrowthlaneFrame> frames;
	private GrowthlaneTrackingILP ilp;
	private IDialogManager dialogManager;
	private IConfiguration configurationManager;
	private GlFileManager glFileManager;

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
	public Growthlane(IDialogManager dialogManager, IConfiguration configurationManager, GlFileManager glFileManager) {
		this.dialogManager = dialogManager;
		this.configurationManager = configurationManager;
		this.glFileManager = glFileManager;
		this.frames = new ConcurrentArrayList<>();
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

		GRBModelAdapter model = null;
		if (!isNull(glFileManager.getGurobiMpsFilePath()))
			try {
				GRBEnv env = new GRBEnv("MotherMachineILPs.log");
				GRBModel grbModel = new GRBModel(env, glFileManager.getGurobiMpsFilePath().toString());
				model = new GRBModelAdapter(grbModel);
			} catch (GRBException e) {
				e.printStackTrace();
			}
		else {
			model = GRBModelFactory.getModel();
		}

		ilp = new GrowthlaneTrackingILP(MoMA.dic.getGuiFrame(), this, model, MoMA.dic.getAssignmentPlausibilityTester(), configurationManager, MoMA.dic.getGitVersionProvider().getVersionString(), MoMA.dic.getCostFactory());
		if (guiProgressReceiver != null) {
			ilp.addProgressListener(guiProgressReceiver);
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

	public void generateSegmentationHypotheses() {
		int numberOfFrames = getFrames().size();
		getFrames().parallelStream().forEach((glf) -> {
			int currentFrame = glf.getFrameIndex() + 1;
			glf.generateSimpleSegmentationHypotheses();
			System.out.print("Frame: " + currentFrame + "/" + numberOfFrames + "\n");
		});
	}

	public int getTimeStepMaximum() {
		return getFrames().size() - 1;
	}

	private GrowthlaneFrame getFirstGrowthlaneFrame() {
		return getFrames().get(0);
	}

	public List<SegmentRecord> getCellTrackStartingPoints() {
		try {
			GrowthlaneFrame firstGLF = getFirstGrowthlaneFrame();
			CellTrackBuilder trackBuilder = new CellTrackBuilder();
			trackBuilder.buildSegmentTracks(firstGLF.getSortedActiveHypsAndPos(),
					firstGLF,
					firstGLF.getParent().getIlp(),
					getTimeStepMaximum());
			List<SegmentRecord> startingPoints = trackBuilder.getStartingPoints();
			return startingPoints;
		}
		catch (GRBException grbException){
			throw new RuntimeException("Could not get track starting points, because the Gurobi model failed during querying.", grbException);
		}
	}
}
