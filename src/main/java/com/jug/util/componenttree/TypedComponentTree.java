package com.jug.util.componenttree;

import java.util.Set;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.algorithm.componenttree.ComponentTree;
import net.imglib2.algorithm.componenttree.pixellist.PixelListComponentTree;
import net.imglib2.type.numeric.real.DoubleType;

class TypedComponentTree< T, C extends Component< T, C > > implements ComponentTree< C >
{
	private final ComponentTree< C > tree;

	private TypedComponentTree(final ComponentTree<C> f)
	{
		this.tree = f;
	}

	private static < T, C extends Component< T, C > > void create(final ComponentTree<C> f)
	{
        new TypedComponentTree<>(f);
    }

	/**
	 * Get the set of root nodes of this component forest.
	 *
	 * @return set of roots.
	 */
	@Override
	public Set< C > roots()
	{
		return tree.roots();
	}

	@Override
	public C root()
	{
		return tree.root();
	}

	public static void main( final String[] args )
	{
		final RandomAccessibleInterval< DoubleType > input = null;
		TypedComponentTree< DoubleType, ? extends Component< DoubleType, ? > > tree;
		TypedComponentTree.create( PixelListComponentTree.buildComponentTree( input, new DoubleType(), true ) );
	}
}