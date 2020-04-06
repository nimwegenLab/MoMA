package com.jug.gui.assignmentview;

import com.jug.gui.MoMAGui;
import com.jug.lp.AbstractAssignment;
import com.jug.lp.GrowthLineTrackingILP;
import com.jug.lp.Hypothesis;
import com.jug.util.OSValidator;
import gurobi.GRBException;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.numeric.real.FloatType;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author jug
 */
public class AssignmentViewer extends JTabbedPane implements ChangeListener {

	// -------------------------------------------------------------------------------------
	// statics
	// -------------------------------------------------------------------------------------
	private static final long serialVersionUID = 6588846114839723373L;

	// -------------------------------------------------------------------------------------
	// fields
	// -------------------------------------------------------------------------------------
	private AssignmentView activeAssignments;
	private AssignmentView inactiveMappingAssignments;
	private AssignmentView inactiveDivisionAssignments;
	private AssignmentView inactiveExitAssignments;
	private AssignmentView fixedAssignments;

	private int curTabIdx = 0;
	private JPanel nextHackTab;

	private HashMap< Hypothesis< Component< FloatType, ? >>, Set< AbstractAssignment< Hypothesis< Component< FloatType, ? >>> >> data = new HashMap<>();

	private final MoMAGui gui;

	// -------------------------------------------------------------------------------------
	// construction
	// -------------------------------------------------------------------------------------
	/**
     */
	public AssignmentViewer( final int height, final MoMAGui callbackGui ) {
		this.gui = callbackGui;
		this.setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 0 ) );
		buildGui( height );
	}

	// -------------------------------------------------------------------------------------
	// getters and setters
	// -------------------------------------------------------------------------------------

	// -------------------------------------------------------------------------------------
	// methods
	// -------------------------------------------------------------------------------------
	/**
	 * Builds the user interface.
	 */
	private void buildGui( final int height ) {
		activeAssignments = new AssignmentView( height, gui );
		inactiveMappingAssignments = new AssignmentView( height, gui );
		inactiveDivisionAssignments = new AssignmentView( height, gui );
		inactiveExitAssignments = new AssignmentView( height, gui );
		fixedAssignments = new AssignmentView( height, gui );

		// Hack to enable non-Mac MoMA to only use one row of tabs
		nextHackTab = new JPanel();
		final JComponent[] tabsToRoll =
				{ activeAssignments, inactiveMappingAssignments, inactiveDivisionAssignments, inactiveExitAssignments, fixedAssignments };
		final String[] namesToRoll =
				{ "OPT", "M", "D", "E", "GT" };
		final AssignmentViewer me = this;
		final ChangeListener changeListener = changeEvent -> {
            final JTabbedPane sourceTabbedPane = ( JTabbedPane ) changeEvent.getSource();
            if ( sourceTabbedPane.getSelectedComponent().equals( nextHackTab ) ) {
                final int oldIdx = curTabIdx;
                curTabIdx++;
                if ( curTabIdx >= tabsToRoll.length ) curTabIdx = 0;
                me.add( namesToRoll[ curTabIdx ], tabsToRoll[ curTabIdx ] );
                me.remove( tabsToRoll[ oldIdx ] );
                me.setSelectedIndex( 1 );
            }
        };

		activeAssignments.display( GrowthLineTrackingILP.getActiveAssignments(data) );
		inactiveMappingAssignments.display( GrowthLineTrackingILP.getAssignmentsOfType( data, GrowthLineTrackingILP.ASSIGNMENT_MAPPING) );
		inactiveDivisionAssignments.display( GrowthLineTrackingILP.getAssignmentsOfType( data, GrowthLineTrackingILP.ASSIGNMENT_DIVISION) );
		inactiveExitAssignments.display( GrowthLineTrackingILP.getAssignmentsOfType( data, GrowthLineTrackingILP.ASSIGNMENT_EXIT) );
		fixedAssignments.display( data );
		fixedAssignments.setFilterGroundTruth( true );

		if ( !OSValidator.isMac() ) {
			this.add( ">", nextHackTab );
			this.add( namesToRoll[ curTabIdx ], tabsToRoll[ curTabIdx ] );
			this.setSelectedIndex( 1 );
			this.addChangeListener( changeListener );
		} else {
			for ( int i = 0; i < tabsToRoll.length; i++ ) {
				this.add( namesToRoll[ i ], tabsToRoll[ i ] );
			}
		}
	}

	/**
	 * Draw this instance of assignmentViewer without having a fitting HashMap.
	 */
	public void display() {
		HashMap< Hypothesis< Component< FloatType, ? >>, Set< AbstractAssignment< Hypothesis< Component< FloatType, ? >>> >> emptyHashMap = new HashMap<>();
		display(emptyHashMap);
	}

	/**
	 * Receives and visualizes a new HashMap of assignments.
	 *
	 * @param hashMap
	 *            a <code>HashMap</code> containing pairs of segmentation
	 *            hypothesis at some time-point t and assignments towards t+1.
	 */
	public void display( final HashMap< Hypothesis< Component< FloatType, ? >>, Set< AbstractAssignment< Hypothesis< Component< FloatType, ? >>> >> hashMap ) {
		this.data = hashMap;
		activeAssignments.setData( GrowthLineTrackingILP.getActiveAssignments(data) );
		inactiveMappingAssignments.setData( GrowthLineTrackingILP.getAssignmentsOfType( data, GrowthLineTrackingILP.ASSIGNMENT_MAPPING) );
		inactiveDivisionAssignments.setData( GrowthLineTrackingILP.getAssignmentsOfType( data, GrowthLineTrackingILP.ASSIGNMENT_DIVISION) );
		inactiveExitAssignments.setData( GrowthLineTrackingILP.getAssignmentsOfType( data, GrowthLineTrackingILP.ASSIGNMENT_EXIT) );
		fixedAssignments.setData( data );
	}

	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged( final ChangeEvent e ) {
		if ( this.getSelectedComponent().equals( activeAssignments ) ) {
			activeAssignments.setData( GrowthLineTrackingILP.getActiveAssignments(data) );
		} else if ( this.getSelectedComponent().equals( inactiveMappingAssignments ) ) {
			inactiveMappingAssignments.setData( GrowthLineTrackingILP.getAssignmentsOfType( data, GrowthLineTrackingILP.ASSIGNMENT_MAPPING) );
		} else if ( this.getSelectedComponent().equals( inactiveDivisionAssignments ) ) {
			inactiveDivisionAssignments.setData( GrowthLineTrackingILP.getAssignmentsOfType( data, GrowthLineTrackingILP.ASSIGNMENT_DIVISION) );
		} else if ( this.getSelectedComponent().equals( inactiveExitAssignments ) ) {
			inactiveExitAssignments.setData( GrowthLineTrackingILP.getAssignmentsOfType( data, GrowthLineTrackingILP.ASSIGNMENT_EXIT ) );
		} else {
			fixedAssignments.setData( data );
		}
	}

	/**
	 * Returns the <code>AssignmentView</code> that holds all active
	 * assignments.
	 *
	 * @return
	 */
	public AssignmentView getActiveAssignmentsForHtmlExport() {
		return this.activeAssignments;
	}

}
