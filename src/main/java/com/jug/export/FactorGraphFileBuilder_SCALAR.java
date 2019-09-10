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
public class FactorGraphFileBuilder_SCALAR {

	private int next_var_id = 0;
	private int next_fkt_id = 0;
	private int next_fac_id = 0;

	private final List< String > var_lines = new ArrayList<>();
	private final List< String > fkt_lines = new ArrayList<>();
	private final List< String > fac_lines = new ArrayList<>();

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
    private int getNumFunctions() {
		return next_fkt_id;
	}

	/**
	 * Returns the number of factors added so far.
	 *
	 * @return surprise! ;)
	 */
    private int getNumFactors() {
		return next_fac_id;
	}

	/**
	 * Adds a comment line in the variable-section.
	 *
	 * @param comment
	 *            the String that should be added as a comment.
	 */
	public void addVarComment( final String comment ) {
		var_lines.add( "# " + comment );
	}

	/**
	 * Adds a variable.
	 *
	 * @param numVals
	 *            number of states this discrete variable can have.
	 * @return the id of the variable just added.
	 */
	public int addVar( final int numVals ) {
		var_lines.add( Integer.toString( numVals ) );
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
	public int addFkt( final String line ) {
		fkt_lines.add( line );
		return next_fkt_id++;
	}

	/**
	 * Builds a constraint string and adds it as a function.
	 *
	 * @param coeffs
	 *            all coefficients needed for this function
	 * @param comp
	 *            must be one of: "<=", "==", or ">="
	 * @param rhs
	 *            the right hand side of the inequality (equality) constraint
	 * @return the id of the function just added.
	 */
	public int addConstraintFkt( final List< Integer > coeffs, final String comp, final int rhs ) {
		StringBuilder str = new StringBuilder("constraint " + coeffs.size() + " ");
		for ( final int i : coeffs ) {
			str.append(i).append(" ");
		}
		str.append(" ").append(comp).append(" ").append(rhs);
		return addFkt(str.toString());
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
	 */
    private void addFactor(final String line) {
		fac_lines.add( line );
		next_fac_id++;
	}

	/**
	 * Builds a unary factor-string and adds it.
	 *
	 * @param functionId
	 *            id of the function this factor utilizes.
	 * @param varId
	 *            the variable id for this unary factor.
	 */
	public void addFactor(final int functionId, final int varId, final int regionId ) {
		final List< Integer > varIds = new ArrayList<>();
		varIds.add(varId);
		final List< Integer > regionIds = new ArrayList<>();
		regionIds.add(regionId);
		addFactor(functionId, varIds, regionIds);
	}

	/**
	 * Builds a factor-string and adds it.
	 *
	 * @param functionId
	 *            id of the function this factor utilizes.
	 * @param varIds
	 *            list of variables connected with this factor
	 */
	public void addFactor(final int functionId, final List< Integer > varIds, final int regionId ) {
		final List< Integer > regionIds = new ArrayList<>();
		regionIds.add(regionId);
		addFactor(functionId, varIds, regionIds);
	}

	/**
	 * Builds a factor-string and adds it.
	 *
	 * @param functionId
	 *            id of the function this factor utilizes.
	 * @param varIds
	 *            list of variables connected with this factor
	 */
	public void addFactor(final int functionId, final List< Integer > varIds, final List< Integer > regionIds ) {

		StringBuilder str = new StringBuilder(functionId + " ");
		if ( varIds.size() == 0 ) {
			System.err.println( "No varIds!!!!!!" );
		}
		for ( final int i : varIds ) {
			str.append(i).append(" ");
		}
		if ( regionIds.size() == 0 ) {
			System.err.println( "No regionIds!!!!!!" );
		}
		for ( final int i : regionIds ) {
			str.append(i).append(" ");
		}
		addFactor(str.toString());
	}

	/**
	 * @param file
	 */
	public void write( final File file ) {
		BufferedWriter out;
		try {
			out = new BufferedWriter( new FileWriter( file ) );
			out.write( "# variables functions factors" );
			out.newLine();

			out.write( "" + getNumVars() + " " + getNumFunctions() + " " + getNumFactors() );
			out.newLine();

			out.write( "# #### VARIABLE SECTION ###################################" );
			out.newLine();
			for ( final String line : var_lines ) {
				out.write( line );
				out.newLine();
			}
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
			out.close();
		}
		catch ( final IOException e ) {
			e.printStackTrace();
		}
	}
}
