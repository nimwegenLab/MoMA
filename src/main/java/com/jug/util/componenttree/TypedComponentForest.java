package com.jug.util.componenttree;

import java.util.Set;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.componenttree.ComponentForest;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.algorithm.componenttree.pixellist.PixelListComponentTree;
import net.imglib2.type.numeric.real.DoubleType;

class TypedComponentForest< T, C extends Component< T, C > > implements ComponentForest< C >
{
	private final ComponentForest< C > forest;

	private TypedComponentForest(final ComponentForest<C> f)
	{
		this.forest = f;
	}

	private static < T, C extends Component< T, C > > void create(final ComponentForest<C> f)
	{
        new TypedComponentForest<>(f);
    }

	/**
	 * Get the set of root nodes of this component forest.
	 *
	 * @return set of roots.
	 */
	@Override
	public Set< C > roots()
	{
		return forest.roots();
	}

	public static void main( final String[] args )
	{
		final RandomAccessibleInterval< DoubleType > input = null;
		TypedComponentForest.create( PixelListComponentTree.buildComponentTree( input, new DoubleType(), true ) );
		TypedComponentForest.create( MserTree.buildMserTree( input, 0, 0, 0, 0, 0, true ) );
	}
}