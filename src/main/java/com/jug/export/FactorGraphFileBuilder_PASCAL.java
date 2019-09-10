/**
 *
 */
package com.jug.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author jug
 */
public class FactorGraphFileBuilder_PASCAL {

	private int next_var_id = 0;
	private int next_fkt_id = 0;
	private int next_fac_id = 0;
	private int next_con_id = 0;

	private final List< String > var_comment_lines = new ArrayList<>();
	private String  var_line = "";
	private final List< String > fkt_lines = new ArrayList<>();
	private final List< String > fac_lines = new ArrayList<>();
	private final List< String > constraint_lines = new ArrayList<>();

	/**
	 * Returns the number of variables added so far.
	 *
	 * @return surprise! ;)
	 */
    private int getNumVars() {
		return next_var_id;
	}

	/**
	 * Returns the number of functions added so far.
	 *
	 * @return surprise! ;)
	 */
	public int getNumFunctions() {
		return next_fkt_id;
	}

	/**
	 * Returns the number of factors added so far.
	 *
	 * @return surprise! ;)
	 */
	public int getNumFactors() {
		return next_fac_id;
	}

	/**
	 * Returns the number of constraints added so far.
	 *
	 * @return surprise! ;)
	 */
	public int getNumConstraints() {
		return next_con_id;
	}

	/**
	 * Adds a comment line in the variable-section.
	 *
	 * @param comment
	 *            the String that should be added as a comment.
	 */
	public void addVarComment( final String comment ) {
		var_comment_lines.add( "# " + comment );
	}

	/**
	 * Adds a variable.
	 *
	 * @param cardinality
	 *            number of states this discrete variable can have.
	 * @return the id of the variable just added.
	 */
	public int addVar( final int cardinality ) {
		var_line += cardinality + " ";
		return next_var_id++;
	}

	/**
	 * Adds a comment line in the function-section.
	 *
	 * @param comment
	 *            the String that should be added as a comment.
	 */
	public void addFktComment( final String comment ) {
		fkt_lines.add( "# " + comment );
	}

	/**
	 * Adds a pre-assembled String that fully describes a function.
	 *
	 * @param line
	 *            the string to be added.
	 * @return the id of the function just added.
	 */
    private int addFkt(final String line) {
		fkt_lines.add( line );
		return next_fkt_id++;
	}

	/**
	 * Adds a function given a list of variable indices.
	 *
	 * @param varIdx
	 */
	public void addFkt(final int... varIdx ) {
		StringBuilder line = new StringBuilder("" + varIdx.length + " ");
		for ( final int idx : varIdx ) {
			line.append("").append(idx).append(" ");
		}
		addFkt(line.toString());
	}

	/**
	 * Adds a comment line in the factor-section.
	 *
	 * @param comment
	 *            the String that should be added as a comment.
	 */
	public void addFactorComment( final String comment ) {
		fac_lines.add( "# " + comment );
	}

	/**
	 * Adds a pre-assembled String that fully describes a factor.
	 *
	 * @param line
	 *            the string to be added.
	 * @return the id of the factor just added.
	 */
    private int addFactor(final String line) {
		fac_lines.add( line );
		return next_fac_id++;
	}

	/**
	 * Adds a unary factor given by a list of tensor values.
	 *
	 * @param unaries
	 */
	public void addFactor(final float... unaries ) {
		StringBuilder line = new StringBuilder("" + unaries.length + "\n\t");
		for ( final float c : unaries ) {
			line.append("").append(c).append(" ");
		}
		addFactor(line.toString());
	}

	/**
	 * Adds a comment line in a constraint.
	 *
	 * @param comment
	 *            the String that should be added as a comment.
	 */
	public void addConstraintComment( final String comment ) {
		constraint_lines.add( "# " + comment );
	}

	/**
	 * Adds a pre-assembled String that fully describes a factor.
	 *
	 * @param line
	 *            the string to be added.
	 * @return the id of the factor just added.
	 */
    private int addConstraint(final String line) {
		constraint_lines.add( line );
		return next_con_id++;
	}

	/**
	 * Adds a pre-assembled list of Strings that fully describe some
	 * constraints.
	 *
	 * @param lines
	 *            the strings to be added.
	 */
	public void addConstraints(final List< String > lines ) {
		for ( final String line : lines ) {
			addConstraint( line );
		}
	}

	/**
	 * @param file
	 */
	public void write( final File file ) {
		BufferedWriter out;
		try {
			out = new BufferedWriter( new FileWriter( file ) );
			out.write( "# EXPORTED MM-TRACKING WITH CONSTRAINTS (jug@mpi-cbg.de)" );
			out.newLine();
			out.write( "MARKOV" );
			out.newLine();
			out.newLine();

			out.write( "# #### VARIABLE SECTION ###################################" );
			out.newLine();
			for ( final String line : var_comment_lines ) {
				out.write( line );
				out.newLine();
			}
			out.write( "" + getNumVars() );
			out.newLine();
			out.write( var_line );
			out.newLine();

			out.write( "# #### FUNCTION SECTION ###################################" );
			out.newLine();
			for ( final String line : fkt_lines ) {
				out.write( line );
				out.newLine();
			}
			out.write( "# #### FACTOR SECTION #####################################" );
			out.newLine();
			for ( final String line : fac_lines ) {
				out.write( line );
				out.newLine();
			}
			out.write( "# #### CONSTRAINT SECTION #################################" );
			out.newLine();
			for ( final String line : constraint_lines ) {
				out.write( line );
				out.newLine();
			}
			out.close();
		}
		catch ( final IOException e ) {
			e.printStackTrace();
		}
	}
}
