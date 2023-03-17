package com.jug;

import com.jug.config.IConfiguration;
import com.jug.datahandling.IGlExportFilePathSetter;
import com.jug.datahandling.IGlExportFilePathGetter;
import com.jug.export.CellTrackBuilder;
import com.jug.export.SegmentRecord;
import com.jug.gui.IDialogManager;
import com.jug.gui.progress.DialogProgress;
import com.jug.lp.EnterAssignment;
import com.jug.lp.GRBModel.GRBModelAdapter;
import com.jug.lp.GRBModel.GRBModelFactory;
import com.jug.lp.GrowthlaneTrackingILP;
import com.jug.lp.Hypothesis;
import com.jug.lp.IAssignmentFilter;
import com.jug.util.componenttree.AdvancedComponent;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import org.apache.commons.lang.NotImplementedException;
import org.threadly.concurrent.collections.ConcurrentArrayList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

/**
 * @author jug
 */
public class Growthlane {
	private final List<GrowthlaneFrame> frames;
	private final IDialogManager dialogManager;
	private final IConfiguration configurationManager;
	private final IGlExportFilePathGetter glFileManager;
	private final IGlExportFilePathSetter exportFilePathSetter;
	private GrowthlaneTrackingILP ilp;

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
	public Growthlane(IDialogManager dialogManager, IConfiguration configurationManager, IGlExportFilePathGetter glFileManager, IGlExportFilePathSetter exportFilePathSetter) {
		this.dialogManager = Objects.requireNonNull(dialogManager);
		this.configurationManager = Objects.requireNonNull(configurationManager);
		this.glFileManager = Objects.requireNonNull(glFileManager);
		this.exportFilePathSetter = Objects.requireNonNull(exportFilePathSetter);
		this.frames = new ConcurrentArrayList<>();
	}

	// -------------------------------------------------------------------------------------
	// methods
	// -------------------------------------------------------------------------------------
	/**
	 * @return the number of frames (time-steps) in this <code>Growthlane</code>
	 */
	public int numberOfFrames() {
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
	 * @return returns GrowthlaneFrame instance belonging to timeStep
	 */
	public GrowthlaneFrame get(final int timeStep) {
		try {
			return this.getFrames().get(timeStep);
		} catch (IndexOutOfBoundsException err) {
			return null;
		}
	}

	public boolean isLoadedFromDisk() {
		return glFileManager.gurobiMpsFileExists();
	}

	/**
	 * Builds up the ILP used to find the MAP-mapping.
	 */
	public void generateILP( final DialogProgress guiProgressReceiver ) {
		if ( guiProgressReceiver != null ) {
			guiProgressReceiver.setVisible( true );
		}

		GRBEnv grbEnv;
		GRBModelAdapter model;
		try {
			grbEnv = new GRBEnv(glFileManager.getGurobiEnvironmentLogFilePath().toString());
		} catch (GRBException e) {
			throw new RuntimeException(e);
		}
		if (isLoadedFromDisk())
			try {
				GRBModel grbModel = new GRBModel(grbEnv, glFileManager.getGurobiMpsFilePath().toString());
				model = new GRBModelAdapter(grbModel);
			} catch (GRBException e) {
				throw new RuntimeException(e);
			}
		else {
			model = GRBModelFactory.getModel();
		}

		IAssignmentFilter assignmentFilter = MoMA.dic.getAssignmentFilterFactory().getAssignmentFilter();
		ilp = new GrowthlaneTrackingILP(this,
				model,
				MoMA.dic.getAssignmentPlausibilityTester(),
				configurationManager,
				MoMA.dic.getVersionProvider().getVersion().toString(),
				MoMA.dic.getCostFactory(),
				isLoadedFromDisk(),
				MoMA.dic.getGurobiCallbackFactory(),
				MoMA.dic.getGurobiProgressDialogFactory(),
				assignmentFilter,
				MoMA.dic);
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

	private final List<ChangeListener> listenerList = new ConcurrentArrayList<>(); /* MM-20220628: use concurrent array listeners so that we can remove listener-callback from within the callback without a concurrent modification error; this seems hacky */

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
		MoMA.dic.getComponentForestTimer().start();
		int numberOfFrames = getFrames().size();
		getFrames().parallelStream().forEach((glf) -> {
			int currentFrame = glf.getFrameIndex() + 1;
			glf.generateSimpleSegmentationHypotheses();
			System.out.print("Frame: " + currentFrame + "/" + numberOfFrames + "\n");
		});
		MoMA.dic.getComponentForestTimer().stop();
		MoMA.dic.getComponentForestTimer().printExecutionTime("Timer result for generating components");
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

			Vector<ValuePair<Integer, Hypothesis<AdvancedComponent<FloatType>>>> listOfStartingHypotheses =
					firstGLF.getSortedActiveHypsAndPos();

//			Vector<ValuePair<Integer, Hypothesis<AdvancedComponent<FloatType>>>> listOfEnteringHypotheses = getListOfEnteringHypotheses();

			trackBuilder.buildSegmentTracks(listOfStartingHypotheses,
					firstGLF,
					firstGLF.getParent().getIlp(),
					getTimeStepMaximum());
			return trackBuilder.getStartingPoints();
		}
		catch (GRBException grbException){
			throw new RuntimeException("Could not get track starting points, because the Gurobi model failed during querying.", grbException);
		}
	}

	private Vector<ValuePair<Integer, Hypothesis<AdvancedComponent<FloatType>>>> getListOfEnteringHypotheses() {
		List<EnterAssignment> listOfActiveEnterAssignment = ilp.getAllEnterAssignments().stream().filter(a -> a.isActive()).collect(Collectors.toList());
		throw new NotImplementedException("This method will determine hypotheses that entered the GL during the measurement. It is not yet implemented.");
	}

	public IGlExportFilePathGetter getExportPaths() {
		return glFileManager;
	}

	public void setOutputPath(Path outputPath) {
		exportFilePathSetter.setOutputPath(outputPath);
	}
}
