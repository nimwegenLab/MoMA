package com.jug.util.componenttree;

import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.Type;

public interface ComponentInterface<T extends Type<T>, C extends Component<T, C>> extends Component<T, C> {
    double[] firstMomentPixelCoordinates();
}
