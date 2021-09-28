package com.jug.lp;

import com.jug.Growthlane;
import com.jug.export.FactorGraphFileBuilder_PASCAL;
import com.jug.export.FactorGraphFileBuilder_PAUL;
import com.jug.export.FactorGraphFileBuilder_SCALAR;
import gurobi.GRB;
import gurobi.GRBException;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.type.numeric.real.FloatType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class FactorGraphExporter {
    private Growthlane gl;
    private GrowthlaneTrackingILP ilp;
    private final AssignmentsAndHypotheses<AbstractAssignment<Hypothesis<Component<FloatType, ?>>>, Hypothesis<Component<FloatType, ?>>> nodes;
    private final HypothesisNeighborhoods<Hypothesis<Component<FloatType, ?>>, AbstractAssignment<Hypothesis<Component<FloatType, ?>>>> edgeSets;

    public FactorGraphExporter(Growthlane gl) {
        this.ilp = ilp;
        this.gl = ilp.getGrowthlane();
        this.nodes = ilp.getNodes();
        this.edgeSets = ilp.getEdgeSets();
    }

    /**
     * Writes the FactorGraph corresponding to the optimization problem of the
     * given growth-line into a file (format as requested by Bogdan&Paul).
     * Format is an extension of
     * http://www.cs.huji.ac.il/project/PASCAL/fileFormat.php.
     *
     * @throws IOException
     */
    public void exportFG_PASCAL( final File file ) {
        final FactorGraphFileBuilder_PASCAL fgFile = new FactorGraphFileBuilder_PASCAL();

        // FIRST RUN: set all varId's
        for ( int t = 0; t < nodes.getNumberOfTimeSteps(); t++ ) {

            final List< AbstractAssignment< Hypothesis<Component<FloatType, ? >> > > assmts_t = nodes.getAssignmentsAt( t );
            for ( final AbstractAssignment< Hypothesis< Component< FloatType, ? > > > assmt : assmts_t ) {

                // variables for assignments
                final int var_id = fgFile.addVar( 2 );
                assmt.setVarId( var_id );

                // unaries associated to assignments
                if ( assmt.getType() == GrowthlaneTrackingILP.ASSIGNMENT_MAPPING ) {
                    final MappingAssignment ma = ( MappingAssignment ) assmt;
                } else if ( assmt.getType() == GrowthlaneTrackingILP.ASSIGNMENT_DIVISION ) {
                    final DivisionAssignment da = ( DivisionAssignment ) assmt;
                } else if ( assmt.getType() == GrowthlaneTrackingILP.ASSIGNMENT_EXIT ) {
                    final ExitAssignment ea = ( ExitAssignment ) assmt;
                }
                fgFile.addFkt( assmt.getVarIdx() );
                fgFile.addFactor( 0f, assmt.getCost() );
            }
        }
        // SECOND RUN: export all the rest (now that we have the right varId's).
        fgFile.addConstraintComment( "--- EXIT CONSTRAINTS -----------------------------" );
        for ( int t = 0; t < nodes.getNumberOfTimeSteps(); t++ ) {

            final List< AbstractAssignment< Hypothesis< Component< FloatType, ? > > > > assmts_t = nodes.getAssignmentsAt( t );
            for ( final AbstractAssignment< Hypothesis< Component< FloatType, ? > > > assmt : assmts_t ) {

                fgFile.addConstraints( assmt.getConstraintsToSave_PASCAL() );
            }
        }
        fgFile.addConstraintComment( "--- UNIQUENESS CONSTRAINTS FOR PATHS -------------" );
        fgFile.addConstraints( getPathBlockingConstraints_PASCAL() );
        fgFile.addConstraintComment( "--- CONTINUATION CONSTRAINTS ---------------------" );
        fgFile.addConstraints( getExplainationContinuityConstraints_PASCAL() );

        // WRITE FILE
        fgFile.write( file );
    }

    /**
     * Writes the FactorGraph corresponding to the optimization problem of the
     * given growth-line into a file (format as the one requested by Jan and
     * SCALAR).
     *
     * @throws IOException
     */
    public void exportFG_SCALAR_style( final File file ) {
        // Here I collect all the lines I will eventually write into the FG-file...
        final FactorGraphFileBuilder_SCALAR fgFile = new FactorGraphFileBuilder_SCALAR();

        // FIRST RUN: we export all variables and set varId's for second run...
        for ( int t = 0; t < nodes.getNumberOfTimeSteps(); t++ ) {
            // TODO puke!
            final int regionId = ( t + 1 ) / 2;

            fgFile.addVarComment( "=== VAR-SECTION :: TimePoint t=" + ( t + 1 ) + " ================" );
            fgFile.addVarComment( "--- VAR-SECTION :: Assignment-variables ---------------" );

            fgFile.addFktComment( "=== FKT-SECTION :: TimePoint t=" + ( t + 1 ) + " ================" );
            fgFile.addFktComment( "--- FKT-SECTION :: Unary (Segmentation) Costs ---------" );

            fgFile.addFactorComment( "=== FAC-SECTION :: TimePoint t=" + ( t + 1 ) + " ================" );
            fgFile.addFactorComment( "--- FAC-SECTION :: Unary (Segmentation) Factors -------" );

            final List< AbstractAssignment< Hypothesis< Component< FloatType, ? > > > > assmts_t = nodes.getAssignmentsAt( t );
            for ( final AbstractAssignment< Hypothesis< Component< FloatType, ? > > > assmt : assmts_t ) {
                final int var_id = fgFile.addVar( 2 );
                assmt.setVarId( var_id );

                float cost = 0.0f;
                if ( assmt.getType() == GrowthlaneTrackingILP.ASSIGNMENT_MAPPING ) {
                    fgFile.addVarComment( "- - MAPPING (var: " + var_id + ") - - - - - " );
                    fgFile.addFktComment( "- - MAPPING (var: " + var_id + ") - - - - - " );
                    final MappingAssignment ma = ( MappingAssignment ) assmt;
                    cost = ma.getSourceHypothesis().getCost() + ma.getDestinationHypothesis().getCost();
                } else if ( assmt.getType() == GrowthlaneTrackingILP.ASSIGNMENT_DIVISION ) {
                    fgFile.addVarComment( "- - DIVISION (var: " + var_id + ") - - - - - " );
                    fgFile.addFktComment( "- - DIVISION (var: " + var_id + ") - - - - - " );
                    final DivisionAssignment da = ( DivisionAssignment ) assmt;
                    cost = da.getSourceHypothesis().getCost() + da.getUpperDesinationHypothesis().getCost() + da
                            .getLowerDesinationHypothesis()
                            .getCost();
                } else if ( assmt.getType() == GrowthlaneTrackingILP.ASSIGNMENT_EXIT ) {
                    fgFile.addVarComment( "- - EXIT (var: " + var_id + ") - - - - - " );
                    fgFile.addFktComment( "- - EXIT (var: " + var_id + ") - - - - - " );
                    final ExitAssignment ea = ( ExitAssignment ) assmt;
                    cost = ea.getAssociatedHypothesis().getCost();
                }

                final int fkt_id = fgFile.addFkt( String.format( "table 1 2 0 %f", cost ) );
                fgFile.addFactor( fkt_id, var_id, regionId );
            }
        }
        // SECOND RUN: export all the rest (now that we have the right varId's).
        for ( int t = 0; t < nodes.getNumberOfTimeSteps(); t++ ) {
            // TODO puke!
            final int regionId = ( t + 1 ) / 2;

            fgFile.addFktComment( "=== FKT-SECTION :: TimePoint t=" + ( t + 1 ) + " ================" );
            fgFile.addFktComment( "--- FKT-SECTION :: Assignment Constraints (HUP-stuff for EXITs) -------------" );

            fgFile.addFactorComment( "=== FAC-SECTION :: TimePoint t=" + ( t + 1 ) + " ================" );
            fgFile.addFactorComment( "--- FAC-SECTION :: Assignment Factors ----------------" );

            final List< AbstractAssignment< Hypothesis< Component< FloatType, ? > > > > assmts_t = nodes.getAssignmentsAt( t );
            for ( final AbstractAssignment< Hypothesis< Component< FloatType, ? > > > assmt : assmts_t ) {
                final List< Integer > regionIds = new ArrayList<>();
                regionIds.add(regionId);
                assmt.addFunctionsAndFactors( fgFile, regionIds );
            }

            // NOTE: last time-point does not get Path-Blocking or Explanation-Continuity-Constraints!
            if ( t == nodes.getNumberOfTimeSteps() - 1 ) continue;

            fgFile.addFktComment( "--- FKT-SECTION :: Path-Blocking Constraints ------------" );
            fgFile.addFactorComment( "--- FAC-SECTION :: Path-Blocking Constraints ------------" );

            final ComponentForest< ? > ct = gl.get( t ).getComponentTree();
            recursivelyAddPathBlockingConstraints( ct, t, fgFile );

            if ( t > 0 && t < nodes.getNumberOfTimeSteps() ) {
                fgFile.addFktComment( "--- FKT-SECTION :: Explanation-Continuity Constraints ------" );
                fgFile.addFactorComment( "--- FAC-SECTION :: Explanation-Continuity Constraints ------" );

                for ( final Hypothesis< Component< FloatType, ? > > hyp : nodes.getHypothesesAt( t ) ) {
                    final List< Integer > varIds = new ArrayList<>();
                    final List< Integer > coeffs = new ArrayList<>();

                    if ( edgeSets.getLeftNeighborhood( hyp ) != null ) {
                        for ( final AbstractAssignment< Hypothesis< Component< FloatType, ? > > > a_j : edgeSets.getLeftNeighborhood( hyp ) ) {
                            //expr.addTerm( 1.0, a_j.getGRBVar() );
                            coeffs.add(1);
                            varIds.add(a_j.getVarIdx());
                        }
                    }
                    if ( edgeSets.getRightNeighborhood( hyp ) != null ) {
                        for ( final AbstractAssignment< Hypothesis< Component< FloatType, ? > > > a_j : edgeSets.getRightNeighborhood( hyp ) ) {
                            //expr.addTerm( -1.0, a_j.getGRBVar() );
                            coeffs.add(-1);
                            varIds.add(a_j.getVarIdx());
                        }
                    }

                    // add the constraint for this hypothesis
                    //model.addConstr( expr, GRB.EQUAL, 0.0, "ecc_" + eccId );
                    final int fkt_id = fgFile.addConstraintFkt( coeffs, "==", 0 );
                    fgFile.addFactor( fkt_id, varIds, regionId );
                }
            }
        }

        // WRITE FILE
        fgFile.write( file );
    }

    private < C extends Component< ?, C > > void recursivelyAddPathBlockingConstraints( final ComponentForest< C > ct, final int t, final FactorGraphFileBuilder_SCALAR fgFile ) {
        for ( final C ctRoot : ct.roots() ) {
            // And call the function adding all the path-blocking-constraints...
            recursivelyAddPathBlockingConstraints( ctRoot, t, fgFile );
        }
    }


    /**
     *
     * @param ctNode
     * @param t
     */
    private < C extends Component< ?, C > > void recursivelyAddPathBlockingConstraints( final C ctNode, final int t, final FactorGraphFileBuilder_SCALAR fgFile ) {

        // if ctNode is a leave node -> add constraint (by going up the list of
        // parents and building up the constraint)
        if ( ctNode.getChildren().size() == 0 ) {
            final List< Integer > varIds = new ArrayList<>();
            final List< Integer > coeffs = new ArrayList<>();

            C runnerNode = ctNode;

            // final GRBLinExpr exprR = new GRBLinExpr();
            while ( runnerNode != null ) {
                @SuppressWarnings( "unchecked" )
                final Hypothesis< Component< FloatType, ? > > hypothesis = ( Hypothesis< Component< FloatType, ? >> ) nodes.findHypothesisContaining( runnerNode );
                if ( hypothesis == null ) {
                    System.err.println( "WARNING: Hypothesis for a CTN was not found in GrowthlaneTrackingILP -- this is an indication for some design problem of the system!" );
                }

                if ( edgeSets.getRightNeighborhood( hypothesis ) != null ) {
                    for ( final AbstractAssignment< Hypothesis< Component< FloatType, ? >>> a : edgeSets.getRightNeighborhood( hypothesis ) ) {
                        // exprR.addTerm( 1.0, a.getGRBVar() );
                        coeffs.add(1);
//						varIds.add( new Integer( a.getVarIdx() ) );
                    }
                }
                runnerNode = runnerNode.getParent();
            }
            // model.addConstr( exprR, GRB.LESS_EQUAL, 1.0, name );
            final int fkt_id = fgFile.addConstraintFkt( coeffs, "<=", 1 );
            // TODO puke!
//			fgFile.addFactor( fkt_id, varIds, ( t + 1 ) / 2 );
        } else {
            // if ctNode is a inner node -> recursion
            for ( final C ctChild : ctNode.getChildren() ) {
                recursivelyAddPathBlockingConstraints( ctChild, t, fgFile );
            }
        }
    }

    private List< String > getPathBlockingConstraints_PASCAL() {
        final ArrayList< String > ret = new ArrayList<>();

        // For each time-point
        for ( int t = 0; t < gl.size(); t++ ) {
            // Get the full component tree
            final ComponentForest< ? > ct = gl.get( t ).getComponentTree();
            // And call the function adding all the path-blocking-constraints...
            recursivelyAddPathBlockingConstraints( ret, ct, t );
        }

        return ret;
    }

    private < C extends Component< ?, C > > void recursivelyAddPathBlockingConstraints(
            final List< String > constraints,
            final ComponentForest< C > ct,
            final int t ) {
        for ( final C ctRoot : ct.roots() ) {
            // And call the function adding all the path-blocking-constraints...
            recursivelyAddPathBlockingConstraints( constraints, ctRoot, t );
        }
    }

    private < C extends Component< ?, C > > void recursivelyAddPathBlockingConstraints(
            final List< String > constraints,
            final C ctNode,
            final int t ) {

        // if ctNode is a leave node -> add constraint (by going up the list of
        // parents and building up the constraint)
        if ( ctNode.getChildren().size() == 0 ) {
            C runnerNode = ctNode;

            StringBuilder constraint = new StringBuilder();
            while ( runnerNode != null ) {
                @SuppressWarnings( "unchecked" )
                final Hypothesis< Component< FloatType, ? > > hypothesis =
                        ( Hypothesis< Component< FloatType, ? > > ) nodes.findHypothesisContaining( runnerNode );
                if ( hypothesis == null ) {
                    System.err.println(
                            "WARNING: Hypothesis for a CTN was not found in GrowthlaneTrackingILP -- this is an indication for some design problem of the system!" );
                }

                if ( edgeSets.getRightNeighborhood( hypothesis ) != null ) {
                    for ( final AbstractAssignment< Hypothesis< Component< FloatType, ? > > > a : edgeSets.getRightNeighborhood( hypothesis ) ) {
                        constraint.append(String.format("(%d,1)+", a.getVarIdx()));
                    }
                }
                runnerNode = runnerNode.getParent();
            }
            if ( constraint.length() > 0 ) {
                constraint = new StringBuilder(constraint.substring(0, constraint.length() - 1));
                constraint.append(" <= 1");
                constraints.add(constraint.toString());
            }
        } else {
            // if ctNode is a inner node -> recursion
            for ( final C ctChild : ctNode.getChildren() ) {
                recursivelyAddPathBlockingConstraints( constraints, ctChild, t );
            }
        }
    }

    private List< String > getExplainationContinuityConstraints_PASCAL() {
        final ArrayList< String > ret = new ArrayList<>();

        // For each time-point
        for ( int t = 1; t < gl.size() - 1; t++ ) { // !!! sparing out the border !!!

            for ( final Hypothesis< Component< FloatType, ? > > hyp : nodes.getHypothesesAt( t ) ) {
                StringBuilder constraint = new StringBuilder();

                if ( edgeSets.getLeftNeighborhood( hyp ) != null ) {
                    for ( final AbstractAssignment< Hypothesis< Component< FloatType, ? > > > a_j : edgeSets.getLeftNeighborhood( hyp ) ) {
                        constraint.append(String.format("(%d,1)+", a_j.getVarIdx()));
                    }
                }
                if ( constraint.length() > 0 ) {
                    constraint = new StringBuilder(constraint.substring(0, constraint.length() - 1)); //remove last '+' sign
                }
                if ( edgeSets.getRightNeighborhood( hyp ) != null ) {
                    for ( final AbstractAssignment< Hypothesis< Component< FloatType, ? > > > a_j : edgeSets.getRightNeighborhood( hyp ) ) {
                        constraint.append(String.format("-(%d,1)", a_j.getVarIdx()));
                    }
                }

                constraint.append(" == 0");
                ret.add(constraint.toString());
            }
        }
        return ret;
    }

    /**
     * Stores the tracking problem according to the format designed with Paul
     * Swoboda (IST).
     * See also: https://docs.google.com/document/d/1f_L3PF8WQZdLZsQZb7xb_Z7GwZ9RN1_yotGeWjb-ihU/edit
     *
     * @param file
     */
    public void exportFG_PAUL( final File file ) {

        FactorGraphFileBuilder_PAUL fgFile;
        try {
            fgFile = new FactorGraphFileBuilder_PAUL( ilp.model.get( GRB.DoubleAttr.ObjVal ) );
            System.out.println( "Exporting also LP file (since model is optimized)." );
            ilp.model.write( file.getPath() + ".lp" );
        } catch ( final GRBException e ) {
            fgFile = new FactorGraphFileBuilder_PAUL();
        }

        // HYPOTHESES SECTION
        for ( int t = 0; t < nodes.getNumberOfTimeSteps(); t++ ) {

            fgFile.markNextTimepoint();

            final List< Hypothesis< Component< FloatType, ? > > > hyps_t = nodes.getAllHypotheses().get( t );
            for ( final Hypothesis< Component< FloatType, ? > > hyp : hyps_t ) {

                // variables for assignments
                final int hyp_id = fgFile.addHyp( ilp, hyp );
            }

            // Get the full component tree
            final ComponentForest< ? > ct = gl.get( t ).getComponentTree();
            // And call the function adding all the path-blocking-constraints...
            for ( final Component< ?, ? > ctRoot : ct.roots() ) {
                // And call the function adding all the path-blocking-constraints...
                recursivelyAddPathBlockingHypotheses( fgFile, ctRoot, t );
            }
        }

        // HYPOTHESES SECTION
        fgFile.addLine( "\n# ASSIGNMENTS ASSIGNMENTS ASSIGNMENTS ASSIGNMENTS ASSIGNMENTS ASSIGNMENTS ASSIGNMENTS" );

        fgFile.addLine( "\n# MAPPINGS" );
        for ( int t = 0; t < nodes.getNumberOfTimeSteps(); t++ ) {
            final List< Hypothesis< Component< FloatType, ? > > > hyps_t = nodes.getAllHypotheses().get( t );
            for ( final Hypothesis< Component< FloatType, ? > > hyp : hyps_t ) {
                final HashMap< Hypothesis< Component< FloatType, ? > >, Set< AbstractAssignment< Hypothesis< Component< FloatType, ? > > > >> mapRightNeighbors =
                        ilp.getAllRightAssignmentsThatStartFromOptimalHypothesesAt( t );
                final Set< AbstractAssignment< Hypothesis< Component< FloatType, ? > > > > assmnts = mapRightNeighbors.get( hyp );
                if ( assmnts != null ) {
                    for ( final AbstractAssignment< Hypothesis< Component< FloatType, ? > > > assmnt : assmnts ) {
                        if ( assmnt instanceof MappingAssignment ) {
                            fgFile.addMapping( ilp, t, ( MappingAssignment ) assmnt );
                        }
                    }
                }
            }
        }

        fgFile.addLine( "\n# DIVISIONS" );
        for ( int t = 0; t < nodes.getNumberOfTimeSteps(); t++ ) {
            final List< Hypothesis< Component< FloatType, ? > > > hyps_t = nodes.getAllHypotheses().get( t );
            for ( final Hypothesis< Component< FloatType, ? > > hyp : hyps_t ) {
                final HashMap< Hypothesis< Component< FloatType, ? > >, Set< AbstractAssignment< Hypothesis< Component< FloatType, ? > > > > > mapRightNeighbors =
                        ilp.getAllRightAssignmentsThatStartFromOptimalHypothesesAt( t );
                final Set< AbstractAssignment< Hypothesis< Component< FloatType, ? > > > > assmnts = mapRightNeighbors.get( hyp );
                if ( assmnts != null ) {
                    for ( final AbstractAssignment< Hypothesis< Component< FloatType, ? > > > assmnt : assmnts ) {
                        if ( assmnt instanceof DivisionAssignment ) {
                            fgFile.addDivision( ilp, t, ( DivisionAssignment ) assmnt );
                        }
                    }
                }
            }
        }

        // WRITE FILE
        fgFile.write( file );
    }


    private void recursivelyAddPathBlockingHypotheses(
            final FactorGraphFileBuilder_PAUL fgFile,
            final Component< ?, ? > ctNode,
            final int t ) {

        // if ctNode is a leave node -> add constraint (by going up the list of
        // parents and building up the constraint)
        if ( ctNode.getChildren().size() == 0 ) {
            Component< ?, ? > runnerNode = ctNode;

            final List< Hypothesis< Component< FloatType, ? > > > hyps = new ArrayList<>();
            while ( runnerNode != null ) {
                @SuppressWarnings( "unchecked" )
                final Hypothesis< Component< FloatType, ? > > hypothesis =
                        ( Hypothesis< Component< FloatType, ? > > ) nodes.findHypothesisContaining( runnerNode );
                if ( hypothesis == null ) {
                    System.err.println(
                            "A WARNING: Hypothesis for a CTN was not found in GrowthlaneTrackingILP -- this is an indication for some design problem of the system!" );
                } else {
                    hyps.add( hypothesis );
                }

                runnerNode = runnerNode.getParent();
            }
            // Add the Exclusion Constraint (finally)
            fgFile.addPathBlockingConstraint( hyps );
        } else {
            // if ctNode is a inner node -> recursion
            for ( final Component< ?, ? > ctChild : ctNode.getChildren() ) {
                recursivelyAddPathBlockingHypotheses( fgFile, ctChild, t );
            }
        }
    }

}
