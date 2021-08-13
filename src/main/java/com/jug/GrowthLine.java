package com.jug;

import com.jug.gui.progress.DialogProgress;
import com.jug.lp.GrowthLineTrackingILP;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jug
 */
public class GrowthLine {

	// -------------------------------------------------------------------------------------
	// fields
	// -------------------------------------------------------------------------------------
	private final List< GrowthLineFrame > frames;
	private GrowthLineTrackingILP ilp; //<

	// Hypothesis< Component< FloatType, ? > >,
	// AbstractAssignment< Hypothesis< Component< FloatType, ? > > > > ilp;

	// -------------------------------------------------------------------------------------
	// setters and getters
	// -------------------------------------------------------------------------------------
	/**
	 * @return the frames
	 */
	public List< GrowthLineFrame > getFrames() {
		return frames;
	}

	/**
	 * @return the ILP
	 */
	public GrowthLineTrackingILP getIlp() {
		return ilp;
	}

	// -------------------------------------------------------------------------------------
	// constructors
	// -------------------------------------------------------------------------------------
	public GrowthLine() {
		this.frames = new ArrayList<>();
	}

	// -------------------------------------------------------------------------------------
	// methods
	// -------------------------------------------------------------------------------------
	/**
	 * @return the number of frames (time-steps) in this <code>GrowthLine</code>
	 */
	public int size() {
		return frames.size();
	}

	/**
	 * @param frame
	 *            the GrowthLineFrame to be appended as last frame
     */
	public void add(final GrowthLineFrame frame ) {
		frame.setParent( this );
        frames.add(frame);
    }

	/**
	 * @param frame
	 *            the GrowthLineFrame to be prepended as first frame
	 * @return true, if add was successful.
	 */
	public void prepand( final GrowthLineFrame frame ) {
		frame.setParent( this );
		frames.add( 0, frame );
	}

	/**
	 * @return
	 */
	public GrowthLineFrame get(final int i) {
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

		ilp = new GrowthLineTrackingILP( this );
		if ( guiProgressReceiver != null ) {
			ilp.addProgressListener( guiProgressReceiver );
		}
		ilp.buildILP();

		if ( guiProgressReceiver != null ) {
			guiProgressReceiver.setVisible( false );
			guiProgressReceiver.dispose();
		}
	}
}
